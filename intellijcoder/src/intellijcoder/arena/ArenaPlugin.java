package intellijcoder.arena;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.Renderer;

import javax.swing.*;


/**
 * Interface for TopCoder editor plugin as specified here:
 * http://www.topcoder.com/contest/classes/How%20to%20create%20an%20Editor%20Plugin%20v3.htm
 *
 * Date: 18.01.11
 *
 * @author Konstantin Fadeyev
 */
public interface ArenaPlugin {
    JPanel getEditorPanel();

    String getSource();

    @SuppressWarnings({"UnusedDeclaration"}) // Required by TopCoder Editor Plugin API
    void setSource(String source);

    void setProblemComponent(ProblemComponentModel componentModel, Language language, Renderer renderer);

    void startUsing();
}
