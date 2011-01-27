package intellijcoder.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Konstantin Fadeyev
 *         22.01.11
 */
public class DebugProcessLauncher extends ProcessLauncher {
    @Override
    public void launch(String... arguments) throws IOException {
        final Process process = new ProcessBuilder(arguments).start();
        startThreadPrintingFromAStream(process.getInputStream());
        startThreadPrintingFromAStream(process.getErrorStream());
    }

    private void startThreadPrintingFromAStream(InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        new Thread(new Runnable() {
            public void run() {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
