package intellijcoder.idea;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.junit.TestClassConfigurationProducer;
import com.intellij.ide.DataManager;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
import com.intellij.util.PathUtil;
import intellijcoder.main.IntelliJCoderException;

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

    public void createModule(final String moduleName, final String classSource, final String testSource) {
        //We run it in the event thread, so the DataContext would have current Project data;
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        try {
                            IntelliJIDEA.this.createModule(getCurrentProject(), moduleName, classSource, testSource);
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
        Module module = ModuleManager.getInstance(project).findModuleByName(className);
        if(module == null) {
            throw new IntelliJCoderException("Cannot find module '" + className + "'.");
        }
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        if(sourceRoots.length == 0) {
            throw new IntelliJCoderException("Module '" + className + "' has no source roots.");
        }

        VirtualFile source = sourceRoots[0].findChild(classFileName(className));
        if(source == null) {
            throw new IntelliJCoderException("Cannot find file '" + classFileName(className) + "'.");
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(source);
        assert psiFile != null;
        return psiFile.getText();
    }

    private void createModule(final Project project, String moduleName, String classSource, String testSource) throws IntelliJCoderException {
        Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
        if(module != null) {
            checkModuleStructure(module, moduleName);
            Messages.showInfoMessage("Module '" + moduleName + "' already exists.", MESSAGE_BOXES_TITLE);
            return;
        }
        checkIfModuleRootDirectoryAlreadyExists(project, moduleName);

        ModuleCreator moduleCreator = new ModuleCreator(project, moduleName, classSource, testSource).create();
        module = moduleCreator.getModule();
        PsiJavaFile classFile = moduleCreator.getClassFile();
        PsiJavaFile testFile = moduleCreator.getTestFile();


        selectFileInProjectView(project, classFile);

        //noinspection ConstantConditions
        FileEditorManager.getInstance(project).openFile(classFile.getVirtualFile(), true);

        createAndRunConfiguration(project, module, testFile);
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
    private void createAndRunConfiguration(Project project, Module module, PsiJavaFile testFile) {
        RunnerAndConfigurationSettings settings = createConfiguration(project, module, testFile);
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

    private RunnerAndConfigurationSettings createConfiguration(Project project, Module module, PsiJavaFile testClassFile) {
        RuntimeConfigurationProducer producer = new TestClassConfigurationProducer();
        ConfigurationFactory configurationFactory = producer.getConfigurationType().getConfigurationFactories()[0];
        RunnerAndConfigurationSettings settings = RunManager.getInstance(project).createRunConfiguration("", configurationFactory);
        final JUnitConfiguration configuration = (JUnitConfiguration)settings.getConfiguration();
        configuration.setModule(module);
        PsiClass testClass = JUnitUtil.getTestClass(testClassFile);
        configuration.beClassConfiguration(testClass);
        configuration.restoreOriginalModule(module);
        return settings;
    }

    private static String classFileName(String className) {
        return className + ".java";
    }

    private static String testClassFileName(String className) {
        return className + "Test.java";
    }

    private static class ModuleCreator {
        private Project project;
        private String moduleName;
        private String classSource;
        private String testSource;
        PsiJavaFile classFile;
        PsiJavaFile testFile;
        private Module module;

        public ModuleCreator(Project project, String moduleName, String classSource, String testSource) {
            this.project = project;
            this.moduleName = moduleName;
            this.classSource = classSource;
            this.testSource = testSource;
        }

        public PsiJavaFile getClassFile() {
            return classFile;
        }

        public PsiJavaFile getTestFile() {
            return testFile;
        }

        public Module getModule() {
            return module;
        }

        public ModuleCreator create() {
            PsiDirectory projectRoot = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
            assert projectRoot != null;
            PsiDirectory moduleRoot = projectRoot.createSubdirectory(moduleName);
            PsiDirectory sourceRoot = moduleRoot.createSubdirectory("src");
            PsiDirectory testRoot = moduleRoot.createSubdirectory("test");

            module = ModuleManager.getInstance(project).newModule(getModuleFilePath(moduleRoot), StdModuleTypes.JAVA.getId());
            configureModule(moduleRoot, sourceRoot, testRoot);

            classFile = createFile(project, sourceRoot, classFileName(moduleName), classSource);
            testFile = createFile(project, testRoot, testClassFileName(moduleName), testSource);

            return this;
        }

        private PsiJavaFile createFile(Project project, PsiDirectory directory, String fileName, String source) {
            PsiJavaFile classFile = (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText(fileName, StdFileTypes.JAVA, source);
            classFile = (PsiJavaFile)directory.add(classFile);
            return classFile;
        }

        private void configureModule(PsiDirectory moduleRoot, PsiDirectory sourceRoot, PsiDirectory testRoot) {
            ModifiableRootModel moduleRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
            ContentEntry contentEntry = moduleRootModel.addContentEntry(moduleRoot.getVirtualFile());
            contentEntry.addSourceFolder(sourceRoot.getVirtualFile(), false);
            contentEntry.addSourceFolder(testRoot.getVirtualFile(), true);
            setSdk(moduleRootModel);
            addJUnitLibraryDependency(moduleRootModel);
            moduleRootModel.commit();
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
            String libPath = PathUtil.getJarPathForClass(org.junit.Test.class);

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
