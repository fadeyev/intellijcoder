package intellijcoder.os;

import java.io.*;

/**
 * Wrapper around file system classes
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class FileSystem {
    public OutputStream getFileOutputStream(String fileName) throws FileNotFoundException {
        return new FileOutputStream(fileName);
    }

    public InputStream getFileInputStream(String fileName) throws FileNotFoundException {
        return new FileInputStream(fileName);
    }

    public boolean isFileExists(String fileName) {
        return new File(fileName).exists();
    }
}
