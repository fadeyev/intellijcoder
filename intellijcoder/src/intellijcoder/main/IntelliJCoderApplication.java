package intellijcoder.main;

import intellijcoder.arena.ArenaConfigManager;
import intellijcoder.arena.ArenaJarProvider;
import intellijcoder.arena.ArenaProcessLauncher;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.ipc.IntelliJCoderServer;
import net.jcip.annotations.ThreadSafe;

/**
 * Main application, starts server and everything
 *
 * @author Konstantin Fadeyev
 *         22.01.11
 */
@ThreadSafe
public class IntelliJCoderApplication {
    private ArenaJarProvider jarProvider;
    private ArenaProcessLauncher arenaLauncher;
    private IntelliJCoderServer server;
    private ArenaConfigManager configManager;
    private int serverPort = 0;

    public IntelliJCoderApplication(ArenaJarProvider jarProvider, ArenaProcessLauncher arenaLauncher,
                                    IntelliJCoderServer server, ArenaConfigManager configManager) {
        this.jarProvider = jarProvider;
        this.arenaLauncher = arenaLauncher;
        this.server = server;
        this.configManager = configManager;
    }

    public synchronized void launch() throws IntelliJCoderException {
        String appletFileName = jarProvider.getJarFilePath();
        if(!serverStarted()) {
            serverPort = server.start();
        }
        configManager.setIntelliJCoderAsADefaultEditor();
        arenaLauncher.launch(appletFileName, serverPort);
    }

    public synchronized void shutdown() {
        server.stop();
    }

    private boolean serverStarted() {
        return serverPort > 0;
    }
}
