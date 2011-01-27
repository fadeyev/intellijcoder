package intellijcoder.ipc;

import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.model.Problem;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Fake WorkspaceManager implementation to use in testing asynchronous code
 *
 * Date: 14.01.11
 *
 * @author Konstantin Fadeyev
 */
public class FakeWorkspaceManager implements WorkspaceManager {
    private static final int TIMEOUT = 5;
    private final BlockingQueue<Problem> receivingProblemQueue = new ArrayBlockingQueue<Problem>(1);

    public void createProblemWorkspace(Problem problem) {
        receivingProblemQueue.add(problem);
    }

    public String getSolutionSource(String className) {
        return null;
    }

    public void hasReceivedProblemEqualTo(Problem expectedProblem) throws InterruptedException {
        Problem receviedProblem = receivingProblemQueue.poll(TIMEOUT, SECONDS);
        assertNotNull("hasn't received problem data in "+TIMEOUT+" seconds", receviedProblem);
        assertEquals("Problem", expectedProblem, receviedProblem);
    }
}
