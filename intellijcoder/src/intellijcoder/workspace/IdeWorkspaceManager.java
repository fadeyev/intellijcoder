package intellijcoder.workspace;

import intellijcoder.idea.Ide;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;

/**
 * Manages ide workspace for given problem: creates workspace, provides source for the problem solution on demand.
 *
 * @author Konstantin Fadeyev
 *         15.01.11
 */
public class IdeWorkspaceManager implements WorkspaceManager {
    private Ide ide;
    private CodeBuilder solutionBuilder;
    private CodeBuilder testBuilder;

    public IdeWorkspaceManager(Ide ide, CodeBuilder solutionBuilder, CodeBuilder testBuilder) {
        this.ide = ide;
        this.solutionBuilder = solutionBuilder;
        this.testBuilder = testBuilder;
    }

    public void createProblemWorkspace(Problem problem) throws IntelliJCoderException {
        ide.createModule(problem.getClassName(), solutionBuilder.build(problem), testBuilder.build(problem));
    }

    public String getSolutionSource(String className) throws IntelliJCoderException {
        return ide.getClassSource(className);
    }
}
