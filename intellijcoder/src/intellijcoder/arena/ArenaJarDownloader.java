package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.FileSystem;
import intellijcoder.os.Network;
import intellijcoder.util.StringUtil;
import net.jcip.annotations.ThreadSafe;

import java.io.*;


/**
 * Retrieves Arena jar as a unique file every time {@link #loadArenaJars(ArenaAppletInfo)} called.
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
@ThreadSafe
public class ArenaJarDownloader {
    public static final int BUFFER_SIZE = 1024;
    public static final String FAILED_TO_GET_JAR_MESSAGE = "Failed to retrieve Competition Arena applet";
    private FileSystem fileSystem;
    private Network network;

    public ArenaJarDownloader(Network network, FileSystem fileSystem) {
        this.network = network;
        this.fileSystem = fileSystem;
    }

    public ArenaAppletInfo loadArenaJars(ArenaAppletInfo appletInfo) throws IntelliJCoderException {
        ArenaAppletInfo localAppletInfo = new ArenaAppletInfo();
        localAppletInfo.setMainClass(appletInfo.getMainClass());
        localAppletInfo.addArguments(appletInfo.getArguments());
        try {
            File tempDirectory = createTempDirectory();
            tempDirectory.deleteOnExit();

            for(String url: appletInfo.getClassPathItems()) {
                String filename = downloadFile(url, tempDirectory);
                localAppletInfo.addClassPathItem(filename);
            }
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_GET_JAR_MESSAGE, e);
        }
        return localAppletInfo;
    }

    private String downloadFile(String url, File tempDirectory) throws IOException {
        String filename;
        InputStream urlInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            File file = new File(tempDirectory, StringUtil.getFileName(url));
            file.deleteOnExit();
            filename = file.getAbsolutePath();
            urlInputStream = network.getUrlInputStream(url);
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
        return filename;
    }

    public static File createTempDirectory() throws IOException {
        final File temp;

        temp = File.createTempFile("intellij-coder", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
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
