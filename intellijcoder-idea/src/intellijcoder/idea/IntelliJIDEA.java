package intellijcoder.idea;

import com.intellij.execution.*;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.execution.junit.TestInClassConfigurationProducer;
import com.intellij.ide.browsers.OpenInBrowserRequest;
import com.intellij.ide.browsers.WebBrowserService;
import com.intellij.ide.browsers.WebBrowserUrlProvider;
import com.intellij.ide.browsers.actions.WebPreviewVirtualFile;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.util.PathUtil;
import com.intellij.util.Url;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.SolutionCfg;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;

import static com.intellij.ide.browsers.OpenInBrowserRequestKt.createOpenInBrowserRequest;

/**
 * Do all the hard work in IDEA UI, creates modules, files, run configurations, etc.
 *
 * @author Konstantin Fadeyev
 * 15.01.11
 */
public class IntelliJIDEA implements Ide {
    private static final Logger logger = Logger.getInstance(IntelliJIDEA.class);

    private static final String MESSAGE_BOXES_TITLE = "IntelliJCoder";
    private final Project project;

    public IntelliJIDEA(Project project) {
        this.project = project;
    }

    public void createModule(final String moduleName, final String className, final String classSource, final String testSource, final String htmlSource, final int memLimit) throws IntelliJCoderException {
        DumbService.getInstance(getCurrentProject()).smartInvokeLater(() ->
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        IntelliJIDEA.this.createModule(getCurrentProject(), moduleName, className, classSource, testSource, htmlSource, memLimit);
                    } catch (IntelliJCoderException e) {
                        showErrorMessage("Failed to create problem workspace. " + e.getMessage());
                    }
                }));
    }

    static void showErrorMessage(String errorMessage) {
        Messages.showErrorDialog(errorMessage, MESSAGE_BOXES_TITLE);
    }

    public String getClassSource(final String className) {
        final String[] result = new String[1];
        ApplicationManager.getApplication().invokeAndWait(() ->
                ApplicationManager.getApplication().runReadAction(() -> {
                    try {
                        result[0] = IntelliJIDEA.this.getClassSource(getCurrentProject(), className);
                    } catch (IntelliJCoderException e) {
                        showErrorMessage(e.getMessage());
                    }
                }), ModalityState.NON_MODAL);
        return result[0];
    }

    private Project getCurrentProject() throws IntelliJCoderException {
        if (!project.isInitialized()) {
            throw new IntelliJCoderException("InteliJ IDEA project '" + project.getName() + "' was closed while TopCoder Arena was running. Please restart TopCoder Arena.");
        }
        return project;
    }

    private String getClassSource(Project project, String className) throws IntelliJCoderException {
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
        ModuleCreator moduleCreator;
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
        private final Project project;
        private final String moduleName;
        private final String className;
        private final String classSource;
        private final String testSource;
        private final String htmlSource;
        PsiFile classFile;
        PsiFile testFile;
        PsiFile htmlFile;
        private Module module;
        private final SolutionCfg config;
        private final int memLimit;


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
         */
        public void create() {
            Path rootPath = FileSystems.getDefault().getPath(project.getBasePath());
            VirtualFile projectRootVf = VirtualFileManager.getInstance().findFileByNioPath(rootPath);
            PsiDirectory projectRoot = PsiManager.getInstance(project).findDirectory(projectRootVf);
            assert projectRoot != null;
            final PsiDirectory moduleRoot = getOrCreateModuleFolder(projectRoot, moduleName);
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
                        findOrCreateSourceFolder(moduleRoot, config.sourceFolderName, false);
                        findOrCreateSourceFolder(moduleRoot, config.testFolderName, true);
                        findOrCreateSourceFolder(moduleRoot, config.resourceFolderName, false);
                    }
                });
            }
            // if relevant files (html,class,test) don't exist, they must be created
            DumbService.getInstance(project).runWhenSmart(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            htmlFile = findOrCreateFile(project, moduleRoot.findSubdirectory(config.resourceFolderName), htmlFileName(className), HtmlFileType.INSTANCE, htmlSource);
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
                            ProjectView.getInstance(project).selectPsiElement(testFile, false);
                            FileEditorManager.getInstance(project).openFile(testFile.getVirtualFile(), true);
                            ProjectView.getInstance(project).selectPsiElement(classFile, false);
                            FileEditorManager.getInstance(project).openFile(classFile.getVirtualFile(), true);
                            createAndRunConfiguration();
                            openHtmlFilePreview();
                        }
                    });
                }
            });
        }

        private void createAndRunConfiguration() {
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (projectSdk != null) {
                RunConfigurationProducer producer = new TestInClassConfigurationProducer();
                ConfigurationFactory configurationFactory = producer.getConfigurationType().getConfigurationFactories()[0];
                RunnerAndConfigurationSettings settings = RunManager.getInstance(project).createConfiguration("", configurationFactory);
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
                ProgramRunnerUtil.executeConfiguration(settings, executor);
            }
        }

        private void openHtmlFilePreview() {
            try {
                OpenInBrowserRequest browserRequest = createOpenInBrowserRequest(htmlFile, false);
                Collection<Url> urls = WebBrowserService.getInstance().getUrlsToOpen(browserRequest, true);
                if (!urls.isEmpty()) {
                    WebPreviewVirtualFile file = new WebPreviewVirtualFile(htmlFile.getVirtualFile(), urls.iterator().next());
                    FileEditorManagerEx.getInstanceEx(project).openFileWithProviders(file, false, true);
                }
            } catch (WebBrowserUrlProvider.BrowserException e) {
                logger.error("Failed to open browser to preview file " + htmlFile);
            }
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
            file = (PsiFile) directory.add(file);
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
            if (orderEntry != null) {
                orderEntry.setScope(DependencyScope.TEST);
            }
        }
    }
}
