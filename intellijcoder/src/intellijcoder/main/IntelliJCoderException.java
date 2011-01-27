package intellijcoder.main;

/**
 * Application level exception
 *
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class IntelliJCoderException extends Exception {
    public IntelliJCoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntelliJCoderException(String message) {
        super(message);
    }
}
