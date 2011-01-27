package intellijcoder.os;

import java.io.IOException;

/**
 * Wrapper around ProcessBuilder
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class ProcessLauncher {
    public void launch(String... arguments) throws IOException {
        new ProcessBuilder(arguments).start();
    }
}
