package intellijcoder.ipc;

import intellijcoder.ipc.IntelliJCoderClient;
import intellijcoder.model.Problem;
import intellijcoder.model.ProblemMaker;
import intellijcoder.os.Network;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.Socket;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.junit.Assert.fail;

/**
 * Date: 18.01.11
 *
 * @author Konstantin Fadeyev
 */
@RunWith(JMock.class)
public class IntelliJCoderClientTest {
    private Mockery context = new JUnit4Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private Network network = context.mock(Network.class);

    @Test
    public void throwsApplicationExceptionIfFailedToConnectToServer() throws Exception {
        final int port = somePort();
        IntelliJCoderClient client = new IntelliJCoderClient(network, port);
        context.checking(new Expectations(){{
            allowing(network).getLocalhostSocket(port); will(throwException(new IOException()));
        }});
        try {
            client.createProblemWorkspace(someProblem());
            fail("should throw an exception");
        } catch (Exception e) {
            assertExceptionMessage(e, IntelliJCoderClient.FAILED_TO_CONNECT_ERROR_MESSAGE);
        }
    }

    @Test
    public void throwsApplicationExceptionIfErrorOccuredDuringDataTransfer() throws Exception {
        final int port = somePort();
        IntelliJCoderClient client = new IntelliJCoderClient(network, port);
        final Socket socket = context.mock(Socket.class);
        context.checking(new Expectations(){{
            allowing(socket).getOutputStream(); will(throwException(new IOException()));
            oneOf(socket).close();
            allowing(network).getLocalhostSocket(port); will(returnValue(socket));
        }});
        try {
            client.createProblemWorkspace(someProblem());
            fail("should throw an exception");
        } catch (Exception e) {
            assertExceptionMessage(e, IntelliJCoderClient.SERVER_COMMUNICATION_ERROR_MESSAGE);
        }
    }

    private Problem someProblem() {
        return make(a(ProblemMaker.Problem));
    }

    private int somePort() {
        return 0;
    }
}
