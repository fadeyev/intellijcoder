package intellijcoder.os;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * Wrapper around network classes
 * Date: 13.01.11
 *
 * @author Konstantin Fadeyev
 */
public class Network {
    public InputStream getUrlInputStream(String url) throws IOException {
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Url syntax is incorrect: " + url);
        }
    }

    public ServerSocket bindServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    public Socket getLocalhostSocket(int port) throws IOException {
        return new Socket("localhost", port);
    }
}
