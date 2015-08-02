package intellijcoder.idea;

import com.intellij.execution.*;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.execution.junit.TestClassConfigurationProducer;
import com.intellij.ide.DataManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.util.PathUtil;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.SolutionCfg;

import java.io.File;

/**
 * Do all the hard work in IDEA UI, creates modules, files, run configurations, etc.
 *
 * @author Konstantin Fadeyev
 *         15.01.11
 */
public class IntelliJIDEA implements Ide {
    private static final String MESSAGE_BOXES_TITLE = "IntelliJCoder";
    private Project project;

    public IntelliJIDEA(Project project) {
        this.project = project;
    }

    public void createModule(final String moduleName, final String className, final String classSource, final String testSource, final String htmlSource, final int memLimit) {
        //We run it in the event thread, so the DataContext would have current Project data;
        DumbService.getInstance(project).smartInvokeLater(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        try {
                            IntelliJIDEA.this.createModule(getCurrentProject(), moduleName, className, classSource, testSource, htmlSource, memLimit);
                        } catch (IntelliJCoderException e) {
                            showErrorMessage("Failed to create problem workspace. " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    static void showErrorMessage(String errorMessage) {
        Messages.showErrorDialog(errorMessage, MESSAGE_BOXES_TITLE);
    }

    public String getClassSource(final String className) {
        final String[] result = new String[1];
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    public void run() {
                        try {
                            result[0] = IntelliJIDEA.this.getClassSource(getCurrentProject(), className);
                        } catch (IntelliJCoderException e) {
                            showErrorMessage(e.getMessage());
                        }
                    }
                });
            }
        }, ModalityState.NON_MODAL);
        return result[0];
    }

    private Project getCurrentProject() throws IntelliJCoderException {
        //if project was closed
        if(!project.isInitialized()) {
            // we try to locate project by currently focused component
            @SuppressWarnings({"deprecation"})
            DataContext dataContext = DataManager.getInstance().getDataContext();
            project = DataKeys.PROJECT.getData(dataContext);
        }
        if(project == null) {
            throw new IntelliJCoderException("There is no opened project.");
        }
        return project;
    }

    private String getClassSource(Project project, String className) throws IntelliJCoderException {
        SolutionCfg config = ConfigurationService.getInstance().getState();
        String fileName = classFileName(className);
        VirtualFile source = null;
        // find the desired class name across any modules in the project
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module currentModule : modules) {
            source = findFileInModule(currentModule, fileName);
            if (source != null) {
                // found file, stop looking
                break;
            }
        }
        if (source == null) {
            throw new IntelliJCoderException("Cannot find file '" + fileName + "'.");
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(source);
        assert psiFile != null;
        return psiFile.getText();
    }

    private VirtualFile findFileInModule(Module module, String fileName) {
        // module name matches class, so we can look at a specific module
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        if (sourceRoots.length == 0) {
            return null;
        }
        for (VirtualFile root : sourceRoots) {
            VirtualFile file = root.findChild(fileName);
            if (file != null) return file;
        }
        return null;
    }

    private void createModule(final Project project, String moduleName, String className, String classSource, String testSource, String htmlSource, int memLimit) throws IntelliJCoderException {
        SolutionCfg config = ConfigurationService.getInstance().getState();
        String finalModuleName = "";
        ModuleCreator moduleCreator = null;
        switch (config.moduleNamingConvention) {
            case BY_CLASS_NAME: // this option creates a module per problem
                moduleCreator = new ModuleCreator(config, project, className, className, classSource, testSource, htmlSource, memLimit);
                break;
            case BY_CONTEST_NAME: // this option creates a module per contest
                moduleCreator = new ModuleCreator(config, project, moduleName, className, classSource, testSource, htmlSource, memLimit);
                break;
            default:
                throw new IntelliJCoderException("Configuration broken.  Module naming convention value is not valid.");
        }

        moduleCreator.create();
    }

    /**
     * WARNING!!! This method uses some proprietary IDEA classes,
     * hence vulnerable to IDEA version changes
     * @param project current project
     * @param classFile file to select
     */
    private void selectFileInProjectView(Project project, PsiFile classFile) {
        ProjectView.getInstance(project).selectPsiElement(classFile, false);
    }

    private void checkIfModuleRootDirectoryAlreadyExists(Project project, String moduleName) throws IntelliJCoderException {
        @SuppressWarnings({"ConstantConditions"})
        PsiDirectory projectRoot = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if(hasSubdirectory(projectRoot, moduleName)) {
            throw new IntelliJCoderException("Directory '" + moduleName + "' already exists.");
        }
    }

    private void checkModuleStructure(Module m, String moduleName) throws IntelliJCoderException {
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(m).getSourceRoots();
        if(sourceRoots.length == 0) {
            throw new IntelliJCoderException("Module '" + moduleName + "' exists but doesn't have a source root.");
        }
        VirtualFile source = sourceRoots[0].findChild(classFileName(moduleName));
        if(source == null) {
            throw new IntelliJCoderException("Module '" + moduleName + "' exists but doesn't have '" + moduleName + "' class.");
        }
    }

    private static boolean hasSubdirectory(PsiDirectory psiDirectory, String name) {
        for (PsiDirectory directory : psiDirectory.getSubdirectories()) {
            if(name.equals(directory.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String classFileName(String className) {
        return className + ".java";
    }

    private static String testClassFileName(String className) {
        return className + "Test.java";
    }

    private static String htmlFileName(String className) {
        return className + ".html";
    }

    private static class ModuleCreator {
        private Project project;
        private String moduleName;
        private String className;
        private String classSource;
        private String testSource;
        private String htmlSource;
        PsiFile classFile;
        PsiFile testFile;
        PsiFile htmlFile;
        private Module module;
        private SolutionCfg config;
        private int memLimit;


        public ModuleCreator(SolutionCfg config, Project project, String moduleName, String className, String classSource, String testSource, String htmlSource, int memLimit) {
            this.config = config;
            this.project = project;
            this.moduleName = moduleName;
            this.className = className;
            this.classSource = classSource;
            this.testSource = testSource;
            this.htmlSource = htmlSource;
            this.memLimit = memLimit;
        }

        public PsiFile getClassFile() {
            return classFile;
        }

        public PsiFile getTestFile() {
            return testFile;
        }

        public PsiFile getHtmlFile() {
            return htmlFile;
        }

        public Module getModule() {
            return module;
        }

        public int getMemLimit() {
            return memLimit;
        }

        public PsiDirectory getOrCreateModuleFolder(PsiDirectory projectRoot, String moduleName) {
            PsiDirectory result = projectRoot.findSubdirectory(moduleName);
            if (result == null) {
                result = projectRoot.createSubdirectory(moduleName);
            }
            return result;
        }

        /**
         * A module will be created if it doesn't already exist.
         * Source, test, and resource folders will be created and added to the module if they don't already exist.
         *
         * @return The module
         */
        public void create() {
            PsiDirectory projectRoot = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
            assert projectRoot != null;
            final PsiDirectory moduleRoot = getOrCreateModuleFolder(projectRoot, moduleName);
            boolean newModule = false;
            module = ModuleManager.getInstance(project).findModuleByName(moduleName);
            // if module doesn't exist, it must be created
            if (module == null) {
                DumbService.getInstance(project).runWhenSmart(new Runnable() {
                    public void run() {
                        module = ModuleManager.getInstance(project).newModule(getModuleFilePath(moduleRoot), StdModuleTypes.JAVA.getId());
                        ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
                        modifiableRootModel.inheritSdk();
                        addJUnitLibraryDependency(modifiableRootModel);
                        modifiableRootModel.commit();
                        PsiDirectory sourceRoot = findOrCreateSourceFolder(moduleRoot, config.sourceFolderName, false);
                        PsiDirectory testRoot = findOrCreateSourceFolder(moduleRoot, config.testFolderName, true);
                        PsiDirectory resourceRoot = findOrCreateSourceFolder(moduleRoot, config.resourceFolderName, false);
                    }
                });
            }
            // if relevant files (html,class,test) don't exist, they must be created
            DumbService.getInstance(project).runWhenSmart(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            htmlFile = findOrCreateFile(project, moduleRoot.findSubdirectory(config.resourceFolderName), htmlFileName(className), StdFileTypes.HTML, htmlSource);
                            classFile = findOrCreateFile(project, moduleRoot.findSubdirectory(config.sourceFolderName), classFileName(className), JavaFileType.INSTANCE, classSource);
                            testFile = findOrCreateFile(project, moduleRoot.findSubdirectory(config.testFolderName), testClassFileName(className), JavaFileType.INSTANCE, testSource);
                        }
                    });
                }
            });
            // prepare a run configuration for current test, and run it once
            DumbService.getInstance(project).runWhenSmart(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            ProjectView.getInstance(project).selectPsiElement(classFile, false);
                            FileEditorManager.getInstance(project).openFile(classFile.getVirtualFile(), true);
                            RunConfigurationProducer producer = new TestClassConfigurationProducer();
                            ConfigurationFactory configurationFactory = producer.getConfigurationType().getConfigurationFactories()[0];
                            RunnerAndConfigurationSettings settings = RunManager.getInstance(project).createRunConfiguration("", configurationFactory);
                            final JUnitConfiguration configuration = (JUnitConfiguration) settings.getConfiguration();
                            configuration.setModule(module);
                            PsiClass testClass = JUnitUtil.getTestClass(testFile);
                            configuration.beClassConfiguration(testClass);
                            configuration.restoreOriginalModule(module);
                            String vmParameters = configuration.getVMParameters();
                            if (!vmParameters.isEmpty()) vmParameters += " ";
                            configuration.setVMParameters(vmParameters + "-Xmx" + memLimit + "m");
                            final RunManagerEx runManager = (RunManagerEx) RunManager.getInstance(project);
                            runManager.setTemporaryConfiguration(settings);
                            Executor executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
                            ProgramRunnerUtil.executeConfiguration(project, settings, executor);
                        }
                    });
                }
            });
        }

        private PsiDirectory findOrCreateSourceFolder(PsiDirectory moduleRoot, String folderName, boolean test) {
            PsiDirectory directory = moduleRoot.findSubdirectory(folderName);
            if (directory != null) return directory;
            // folder not found, must create it
            ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
            ContentEntry contentEntry = modifiableRootModel.addContentEntry(moduleRoot.getVirtualFile());
            directory = moduleRoot.createSubdirectory(folderName);
            contentEntry.addSourceFolder(directory.getVirtualFile(), test);
            modifiableRootModel.commit();
            return directory;
        }

        private PsiFile findOrCreateFile(Project project, PsiDirectory directory, String fileName, FileType type, String source) {
            PsiFile file = directory.findFile(fileName);
            if (file != null) return file;
            file = PsiFileFactory.getInstance(project).createFileFromText(fileName, type, source);
                        file = (PsiFile)directory.add(file);
                        return file;
        }

        private String getModuleFilePath(PsiDirectory moduleRoot) {
            return moduleRoot.getVirtualFile().getPath() + File.separator + moduleName + ".iml";
        }

        private void addJUnitLibraryDependency(ModifiableRootModel moduleRootModel) {
            // add junit library
            String junitJarPath = PathUtil.getJarPathForClass(org.junit.Test.class);
            addLibraryDependency(moduleRootModel, junitJarPath);
            // if the hamcrest classes are in a separate jar, add it too (as of JUnit 4.11, they are separate)
            String hamcrestJarPath = PathUtil.getJarPathForClass(org.hamcrest.SelfDescribing.class);
            if (!hamcrestJarPath.equals(junitJarPath)) {
                addLibraryDependency(moduleRootModel, hamcrestJarPath);
            }
        }

        private void addLibraryDependency(ModifiableRootModel moduleRootModel, String libPath) {
            String url = VfsUtil.getUrlForLibraryRoot(new File(libPath));
            VirtualFile libVirtFile = VirtualFileManager.getInstance().findFileByUrl(url);

            final Library jarLibrary = moduleRootModel.getModuleLibraryTable().createLibrary();
            final Library.ModifiableModel libraryModel = jarLibrary.getModifiableModel();
            assert libVirtFile != null;
            libraryModel.addRoot(libVirtFile, OrderRootType.CLASSES);
            libraryModel.commit();

            final LibraryOrderEntry orderEntry = moduleRootModel.findLibraryOrderEntry(jarLibrary);
            orderEntry.setScope(DependencyScope.TEST);
        }

    }
}
