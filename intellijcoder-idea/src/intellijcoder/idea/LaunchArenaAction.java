package intellijcoder.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import intellijcoder.main.*;
import intellijcoder.main.IntelliJCoderApplication;

/**
 * @author Konstantin Fadeyev
 *         12.01.11
 */
public class LaunchArenaAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        try {
            Project project = DataKeys.PROJECT.getData(event.getDataContext());
            IntelliJCoderApplication application = Injector.injectIntelliJCoderApplication(project);
            application.launch();
        } catch (IntelliJCoderException e) {
            IntelliJIDEA.showErrorMessage("Failed to start Competition Arena. " + e.getMessage());
        }
    }
}
