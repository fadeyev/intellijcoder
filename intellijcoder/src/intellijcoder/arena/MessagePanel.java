package intellijcoder.arena;

import javax.swing.*;
import java.awt.*;

/**
 * Date: 18.01.11
 *
 * @author Konstantin Fadeyev
 */
public class MessagePanel extends JPanel {
    public static final Color INFO_COLOR = Color.WHITE;
    public static final Color ERROR_COLOR = Color.PINK;

    private JLabel messageLabel;

    public MessagePanel() {
        messageLabel = new JLabel();
        add(messageLabel);
    }

    public void showErrorMessage(final String message) {
        setMessage(message, ERROR_COLOR);
    }

    public void showInfoMessage(final String message) {
        setMessage(message, INFO_COLOR);
    }

    private void setMessage(final String message, final Color color) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageLabel.setForeground(color);
                messageLabel.setText(message);
            }
        });
    }
}
