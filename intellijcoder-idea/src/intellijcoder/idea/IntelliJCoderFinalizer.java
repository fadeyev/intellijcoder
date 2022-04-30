package intellijcoder.idea;

import com.intellij.ide.AppLifecycleListener;
import intellijcoder.main.IntelliJCoderApplication;

/**
 * Shuts down server when IntelliJ IDEA is closing
 *
* @author Konstantin Fadeyev
*         23.01.11
*/
class IntelliJCoderFinalizer implements AppLifecycleListener {
    private IntelliJCoderApplication intelliJCoderApplication;

    IntelliJCoderFinalizer(IntelliJCoderApplication intelliJCoderApplication) {
        this.intelliJCoderApplication = intelliJCoderApplication;
    }

    @Override
    public void appWillBeClosed(boolean isRestart) {
        intelliJCoderApplication.shutdown();;
    }
}
