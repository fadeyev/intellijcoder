package intellijcoder.idea;

import com.intellij.openapi.application.ApplicationAdapter;
import intellijcoder.main.IntelliJCoderApplication;

/**
 * Shuts down server when IntelliJ IDEA is closing
 *
* @author Konstantin Fadeyev
*         23.01.11
*/
class IntelliJCoderFinalizer extends ApplicationAdapter {
    private IntelliJCoderApplication intelliJCoderApplication;

    IntelliJCoderFinalizer(IntelliJCoderApplication intelliJCoderApplication) {
        this.intelliJCoderApplication = intelliJCoderApplication;
    }

    @Override
    public void applicationExiting() {
        intelliJCoderApplication.shutdown();
    }
}
