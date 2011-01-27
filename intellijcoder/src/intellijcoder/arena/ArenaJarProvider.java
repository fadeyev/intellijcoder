package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.FileSystem;
import intellijcoder.os.Network;
import net.jcip.annotations.ThreadSafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Retrieves Arena jar as a unique file every time {@link #getJarFilePath()} called.
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
@ThreadSafe
public class ArenaJarProvider {
    public static final int BUFFER_SIZE = 1024;
    public static final String ARENA_APPLET_URL = "http://www.topcoder.com/contest/classes/ContestApplet.jar";
    public static final String FAILED_TO_GET_JAR_MESSAGE = "Failed to retrieve Competition Arena applet";
    private FileSystem fileSystem;
    private Network network;

    public ArenaJarProvider(Network network, FileSystem fileSystem) {
        this.network = network;
        this.fileSystem = fileSystem;
    }

    public String getJarFilePath() throws IntelliJCoderException {
        InputStream urlInputStream = null;
        OutputStream fileOutputStream = null;
        String filename;
        try {
            File tempFile = File.createTempFile("ContestApplet", ".jar");
            tempFile.deleteOnExit();
            filename = tempFile.getAbsolutePath();
            try {
                urlInputStream = network.getUrlInputStream(ARENA_APPLET_URL);
                fileOutputStream = fileSystem.getFileOutputStream(filename);
                copyStream(urlInputStream, fileOutputStream);
            } finally {
                if (urlInputStream != null) {
                    urlInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_GET_JAR_MESSAGE, e);
        }
        return filename;
    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }
}
