package intellijcoder.idea;

import com.intellij.execution.*;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.execution.junit.TestClassConfigurationProducer;
import com.intellij.ide.DataManager;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
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
        ApplicationManager.getApplication().invokeLater(new Runnable() {
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
                        } catch(IntelliJCoderException e) {
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

        Module module = moduleCreator.create();
        PsiJavaFile classFile = moduleCreator.getClassFile();
        PsiJavaFile testFile = moduleCreator.getTestFile();

        selectFileInProjectView(project, classFile);

        //noinspection ConstantConditions
        FileEditorManager.getInstance(project).openFile(classFile.getVirtualFile(), true);

        createAndRunConfiguration(config, project, module, testFile, memLimit);
    }

    /**
     * WARNING!!! This method uses some proprietary IDEA classes,
     * hence vulnerable to IDEA version changes
     * @param project current project
     * @param classFile file to select
     */
    private void selectFileInProjectView(Project project, PsiJavaFile classFile) {
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

    /**
     * WARNING!!! This method uses some proprietary IDEA classes,
     * hence vulnerable to IDEA version changes
     * @param project current project
     * @param module module
     * @param testFile class file
     */
    private void createAndRunConfiguration(SolutionCfg config, Project project, Module module, PsiJavaFile testFile, int memLimit) {
        RunnerAndConfigurationSettings settings = createConfiguration(config, project, module, testFile, memLimit);
        setConfigurationAsCurrent(project, settings);
        executeConfiguration(project, settings);
    }

    private void executeConfiguration(Project project, RunnerAndConfigurationSettings settings) {
        Executor executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
        ProgramRunnerUtil.executeConfiguration(project, settings, executor);
    }

    private void setConfigurationAsCurrent(Project project, RunnerAndConfigurationSettings settings) {
        final RunManagerEx runManager = (RunManagerEx) RunManager.getInstance(project);
        runManager.setTemporaryConfiguration(settings);
    }

    private RunnerAndConfigurationSettings createConfiguration(SolutionCfg config, Project project, Module module, PsiJavaFile testClassFile, int memLimit) {
        RunConfigurationProducer producer = new TestClassConfigurationProducer();
        ConfigurationFactory configurationFactory = producer.getConfigurationType().getConfigurationFactories()[0];
        RunnerAndConfigurationSettings settings = RunManager.getInstance(project).createRunConfiguration("", configurationFactory);
        final JUnitConfiguration configuration = (JUnitConfiguration)settings.getConfiguration();
        configuration.setModule(module);
        PsiClass testClass = JUnitUtil.getTestClass(testClassFile);
        configuration.beClassConfiguration(testClass);
        configuration.restoreOriginalModule(module);
        String vmParameters = configuration.getVMParameters();
        if (!vmParameters.isEmpty()) vmParameters += " ";
        configuration.setVMParameters(vmParameters + "-Xmx" + memLimit + "m");
        return settings;
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
        PsiJavaFile classFile;
        PsiJavaFile testFile;
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

        public PsiJavaFile getClassFile() {
            return classFile;
        }

        public PsiJavaFile getTestFile() {
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

        /**
         * A module will be created if it doesn't already exist.
         * Source, test, and resource folders will be created and added to the module if they don't already exist.
         *
         * @return The module
         */
        public Module create() {
            PsiDirectory projectRoot = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
            assert projectRoot != null;
            module = ModuleManager.getInstance(project).findModuleByName(moduleName);
            if (module == null) {
                PsiDirectory moduleRoot = projectRoot.createSubdirectory(moduleName);
                module = ModuleManager.getInstance(project).newModule(getModuleFilePath(moduleRoot), StdModuleTypes.JAVA.getId());
                ModifiableRootModel moduleRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
                setSdk(moduleRootModel);
                addJUnitLibraryDependency(moduleRootModel);
                moduleRootModel.commit();
            }
            PsiDirectory moduleRoot = projectRoot.findSubdirectory(moduleName);
            PsiDirectory sourceRoot = moduleRoot.findSubdirectory(config.sourceFolderName);
            if (sourceRoot == null) {
                sourceRoot = createSourceFolder(moduleRoot, config.sourceFolderName, false);
            }
            PsiDirectory testRoot = moduleRoot.findSubdirectory(config.testFolderName);
            if (testRoot == null) {
                testRoot = createSourceFolder(moduleRoot, config.testFolderName, true);
            }
            PsiDirectory resourceRoot = moduleRoot.findSubdirectory(config.resourceFolderName);
            if (resourceRoot == null) {
                resourceRoot = createSourceFolder(moduleRoot, config.resourceFolderName, false);
            }
            classFile = (PsiJavaFile) sourceRoot.findFile(classFileName(className));
            if (classFile == null) {
                classFile = createJavaFile(project, sourceRoot, classFileName(className), classSource);
            }
            testFile = (PsiJavaFile) testRoot.findFile(testClassFileName(className));
            if (testFile == null) {
                testFile = createJavaFile(project, testRoot, testClassFileName(className), testSource);
            }
            htmlFile = (PsiFile) resourceRoot.findFile(htmlFileName(className));
            if (htmlFile == null) {
                htmlFile = createHtmlFile(project, resourceRoot, htmlFileName(className), htmlSource);
            }
            return module;
        }

        private PsiDirectory createSourceFolder(PsiDirectory moduleRoot, String folderName, boolean test) {
            PsiDirectory resourceRoot;ModifiableRootModel moduleRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
            ContentEntry contentEntry = moduleRootModel.addContentEntry(moduleRoot.getVirtualFile());
            // add resource folder
            resourceRoot = moduleRoot.createSubdirectory(folderName);
            contentEntry.addSourceFolder(resourceRoot.getVirtualFile(), test);
            moduleRootModel.commit();
            return resourceRoot;
        }

        private PsiJavaFile createJavaFile(Project project, PsiDirectory directory, String fileName, String source) {
            PsiJavaFile classFile = (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText(fileName, StdFileTypes.JAVA, source);
            classFile = (PsiJavaFile)directory.add(classFile);
            return classFile;
        }

        private PsiFile createHtmlFile(Project project, PsiDirectory directory, String fileName, String source) {
            PsiFile htmlFile = (PsiFile) PsiFileFactory.getInstance(project).createFileFromText(fileName, StdFileTypes.HTML, source);
            htmlFile = (PsiFile)directory.add(htmlFile);
            return htmlFile;
        }

        private void setSdk(ModifiableRootModel moduleRootModel) {
            Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
            if(sdks.length > 0) {
                moduleRootModel.setSdk(sdks[0]);
            }
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
