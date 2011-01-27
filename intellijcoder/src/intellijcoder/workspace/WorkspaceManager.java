package intellijcoder.workspace;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;

/**
 * Handler for TopCoder events on IDE side
 *
 * @author Konstantin Fadeyev
 *         13.01.11
 */
public interface WorkspaceManager {
    void createProblemWorkspace(Problem problem) throws IntelliJCoderException;

    String getSolutionSource(String className) throws IntelliJCoderException;
}
