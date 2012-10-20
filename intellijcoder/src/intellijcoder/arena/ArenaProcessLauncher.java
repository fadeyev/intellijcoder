package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.ProcessLauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Starts TopCoder application in new process
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class ArenaProcessLauncher {
    public static final String INTELLIJCODER_PORT_PROPERTY = "intellijcoder.port";
    public static final String FAILED_TO_START_PROCESS_MESSAGE = "Failed to start TopCoder application process";

    private ProcessLauncher processLauncher;

    public ArenaProcessLauncher(ProcessLauncher processLauncher) {
        this.processLauncher = processLauncher;
    }

    public void launch(ArenaAppletInfo appletInfo, int port) throws IntelliJCoderException {
        try {
            String javaExecutable = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            List<String> arguments = new ArrayList<String>(asList(javaExecutable,
                                "-cp", appletInfo.getClassPath(),
                                "-D" + INTELLIJCODER_PORT_PROPERTY + "=" + port,
                                appletInfo.getMainClass()));
            arguments.addAll(appletInfo.getArguments());

            processLauncher.launch(arguments.toArray(new String[arguments.size()]));
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_START_PROCESS_MESSAGE, e);
        }
    }
}
