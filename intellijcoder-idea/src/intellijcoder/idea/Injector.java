package intellijcoder.idea;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import intellijcoder.arena.*;
import intellijcoder.ipc.IntelliJCoderServer;
import intellijcoder.main.IntelliJCoderApplication;
import intellijcoder.os.DebugProcessLauncher;
import intellijcoder.os.FileSystem;
import intellijcoder.os.Network;
import intellijcoder.os.ProcessLauncher;
import intellijcoder.workspace.IdeWorkspaceManager;
import intellijcoder.workspace.SolutionCodeBuilder;
import intellijcoder.workspace.TestCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Konstantin Fadeyev
 * 21.01.11
 */
public class Injector {

    private static final Map<String, IntelliJCoderApplication> pluginInstances = new HashMap<>();

    static synchronized IntelliJCoderApplication injectIntelliJCoderApplication(Project project) {
        return pluginInstances.computeIfAbsent(project.getBasePath(), (basePath) -> {
            IntelliJCoderApplication plugin = new IntelliJCoderApplication(
                    injectArenaAppletProvider(),
                    injectArenaProcessLauncher(),
                    injectIntelliJCoderServer(project),
                    injectArenaConfigManager()
            );
            Application ide = injectIntelliJIDEAApplication();
            ide.getMessageBus().connect(ide).subscribe(AppLifecycleListener.TOPIC, injectIntelliJCoderFinalizer(plugin));
            return plugin;
        });
    }

    private static Application injectIntelliJIDEAApplication() {
        return ApplicationManager.getApplication();
    }

    private static IntelliJCoderFinalizer injectIntelliJCoderFinalizer(IntelliJCoderApplication intelliJCoderApplication) {
        return new IntelliJCoderFinalizer(intelliJCoderApplication);
    }

    static ArenaProcessLauncher injectArenaProcessLauncher() {
        return new ArenaProcessLauncher(injectProcessLauncher());
    }

    private static ArenaConfigManager injectArenaConfigManager() {
        return new ArenaConfigManager(injectFileSystem(), PathUtil.getJarPathForClass(IntelliJCoderArenaPlugin.class));
    }

    private static ArenaAppletProvider injectArenaAppletProvider() {
        return new ArenaAppletProvider(injectNetwork(), injectArenaFileParser(), injectArenaJarDownloader());
    }

    private static ArenaFileParser injectArenaFileParser() {
        return new ArenaFileParser();
    }

    private static ArenaJarDownloader injectArenaJarDownloader() {
        return new ArenaJarDownloader(injectNetwork(), injectFileSystem());
    }

    private static ProcessLauncher injectProcessLauncher() {
        return new DebugProcessLauncher();
    }

    private static IntelliJCoderServer injectIntelliJCoderServer(Project project) {
        return new IntelliJCoderServer(
                injectWorkspaceManager(project),
                injectNetwork());
    }

    private static IdeWorkspaceManager injectWorkspaceManager(Project project) {
        return new IdeWorkspaceManager(
                injectIDE(project),
                injectSolutionCodeBuilder(),
                injectTestCodeBuilder());
    }

    private static TestCodeBuilder injectTestCodeBuilder() {
        return new TestCodeBuilder(injectConfigurationService().getState());
    }

    private static SolutionCodeBuilder injectSolutionCodeBuilder() {
        return new SolutionCodeBuilder(injectConfigurationService().getState());
    }

    private static ConfigurationService injectConfigurationService() {
        return ConfigurationService.getInstance();
    }

    private static IntelliJIDEA injectIDE(Project project) {
        return new IntelliJIDEA(project);
    }

    private static FileSystem injectFileSystem() {
        return new FileSystem();
    }

    private static Network injectNetwork() {
        return new Network();
    }

}
