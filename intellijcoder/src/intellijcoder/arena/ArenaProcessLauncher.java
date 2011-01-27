package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.ProcessLauncher;

import java.io.File;
import java.io.IOException;

/**
 * Starts TopCoder application in new process
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class ArenaProcessLauncher {
    public static final String ARENA_APPLET_MAIN_CLASS = "com.topcoder.client.contestApplet.runner.generic";
    public static final String ARENA_APPLET_MAGIC_ARGUMENT_1 = "www.topcoder.com";
    public static final String ARENA_APPLET_MAGIC_ARGUMENT_2 = "5001";
    public static final String ARENA_APPLET_MAGIC_ARGUMENT_3 = "http://tunnel1.topcoder.com/tunnel?dummy";
    public static final String ARENA_APPLET_MAGIC_ARGUMENT_4 = "TopCoder";

    public static final String INTELLIJCODER_PORT_PROPERTY = "intellijcoder.port";
    public static final String FAILED_TO_START_PROCESS_MESSAGE = "Failed to start TopCoder application process";

    private ProcessLauncher processLauncher;

    public ArenaProcessLauncher(ProcessLauncher processLauncher) {
        this.processLauncher = processLauncher;
    }

    public void launch(String fileName, int port) throws IntelliJCoderException {
        try {
            String javaExecutable = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            processLauncher.launch(javaExecutable,
                    "-cp", fileName,
                    "-D" + INTELLIJCODER_PORT_PROPERTY + "=" + port,
                    ARENA_APPLET_MAIN_CLASS,
                    ARENA_APPLET_MAGIC_ARGUMENT_1,
                    ARENA_APPLET_MAGIC_ARGUMENT_2,
                    ARENA_APPLET_MAGIC_ARGUMENT_3,
                    ARENA_APPLET_MAGIC_ARGUMENT_4);
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_START_PROCESS_MESSAGE, e);
        }
    }
}
