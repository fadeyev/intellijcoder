package intellijcoder.ipc;

import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.Network;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.junit.Assert.fail;

/**
 * Date: 17.01.11
 *
 * @author Konstantin Fadeyev
 */
@RunWith(JMock.class)
public class IntelliJCoderServerTest {
    private Mockery context = new JUnit4Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceManager workspaceManager = context.mock(WorkspaceManager.class);
    private Network network = context.mock(Network.class);
    IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, network);

    @Test
    public void throwsApplicationExceptionWhenFailedToBindServerSocket() throws Exception {
        context.checking(new Expectations(){{
            allowing(network).bindServerSocket(with(any(Integer.class))); will(throwException(new IOException()));
        }});
        try {
            server.start();
            fail("should throw " + IntelliJCoderException.class.getSimpleName());
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, IntelliJCoderServer.FAILED_TO_START_SERVER_MESSAGE);
        }
    }
}
