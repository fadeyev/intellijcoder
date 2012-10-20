package intellijcoder.main;

import intellijcoder.arena.ArenaAppletInfo;
import intellijcoder.arena.ArenaAppletProvider;
import intellijcoder.arena.ArenaConfigManager;
import intellijcoder.arena.ArenaProcessLauncher;
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
    private ArenaAppletProvider arenaAppletProvider;
    private ArenaProcessLauncher arenaLauncher;
    private IntelliJCoderServer server;
    private ArenaConfigManager configManager;
    private int serverPort = 0;

    public IntelliJCoderApplication(ArenaAppletProvider arenaAppletProvider, ArenaProcessLauncher arenaLauncher,
                                    IntelliJCoderServer server, ArenaConfigManager configManager) {
        this.arenaAppletProvider = arenaAppletProvider;
        this.arenaLauncher = arenaLauncher;
        this.server = server;
        this.configManager = configManager;
    }

    public synchronized void launch() throws IntelliJCoderException {
        ArenaAppletInfo appletInfo = arenaAppletProvider.getApplet();
        if(!serverStarted()) {
            serverPort = server.start();
        }
        configManager.setIntelliJCoderAsADefaultEditor();
        arenaLauncher.launch(appletInfo, serverPort);
    }

    public synchronized void shutdown() {
        server.stop();
    }

    private boolean serverStarted() {
        return serverPort > 0;
    }
}
