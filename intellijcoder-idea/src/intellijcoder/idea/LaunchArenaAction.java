package intellijcoder.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import intellijcoder.main.*;
import intellijcoder.main.IntelliJCoderApplication;

/**
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class LaunchArenaAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        IntelliJCoderApplication application = Injector.injectIntelliJCoderApplication();
        try {
            application.launch();
        } catch (IntelliJCoderException e) {
            IntelliJIDEA.showErrorMessage("Failed to start Competition Arena. " + e.getMessage());
        }
    }
}
