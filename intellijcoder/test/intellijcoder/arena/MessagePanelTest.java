package intellijcoder.arena;

import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Date: 19.01.11
 *
 * @author Konstantin Fadeyev
 */
public class MessagePanelTest {
    private MessagePanel messagePanel = new MessagePanel();

    @Test
    public void testErrorMessage() throws Exception {
        messagePanel.showErrorMessage("big error");
        assertMessageLabelText(messagePanel, "big error");
        assertMessageLabelColor(messagePanel, MessagePanel.ERROR_COLOR);
    }

    @Test
    public void testInfoMessage() throws Exception {
        messagePanel.showInfoMessage("all ok");
        assertMessageLabelText(messagePanel, "all ok");
        assertMessageLabelColor(messagePanel, MessagePanel.INFO_COLOR);
    }

    private void assertMessageLabelText(final MessagePanel messagePanel, final String expectedMessage) throws InterruptedException, InvocationTargetException {
        final String[] shownMessage = new String[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                shownMessage[0] = getMessageLabel(messagePanel).getText();
            }
        });
        assertEquals("message label text", expectedMessage, shownMessage[0]);
    }

    private void assertMessageLabelColor(final MessagePanel messagePanel, final Color expectedColor) throws InterruptedException, InvocationTargetException {
        final Color[] shownColor = new Color[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                shownColor[0] = getMessageLabel(messagePanel).getForeground();
            }
        });
        assertEquals("message label color", expectedColor, shownColor[0]);
    }

    private JLabel getMessageLabel(MessagePanel messagePanel) {
        assertTrue("message panel should have message label", messagePanel.getComponents().length > 0);
        assertTrue("could not locate message label", messagePanel.getComponent(0) instanceof JLabel);
        return (JLabel) messagePanel.getComponent(0);
    }
}
