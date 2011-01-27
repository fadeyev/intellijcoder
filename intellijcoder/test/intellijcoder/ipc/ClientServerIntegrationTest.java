package intellijcoder.ipc;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.JavaLanguage;
import com.topcoder.shared.problem.DataType;
import intellijcoder.arena.ArenaProcessLauncher;
import intellijcoder.arena.IntelliJCoderArenaPlugin;
import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;
import intellijcoder.model.TestCase;
import intellijcoder.os.Network;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static intellijcoder.model.ProblemMaker.*;
import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Date: 14.01.11
 *
 * @author Konstantin Fadeyev
 */
@RunWith(JMock.class)
public class ClientServerIntegrationTest {
    public static final int TIMEOUT = 1000;
    private Mockery context = new JUnit4Mockery();


    @Test
    public void testProblemTransferFromClientToServer() throws Exception {
        FakeWorkspaceManager workspaceManager = new FakeWorkspaceManager();
        IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, new Network());

        int port = server.start();
        IntelliJCoderClient client = new IntelliJCoderClient(new Network(), port);

        Problem problem = sampleProgram();
        client.createProblemWorkspace(problem);

        workspaceManager.hasReceivedProblemEqualTo(problem);
    }

    @Test(timeout = TIMEOUT)
    public void testSourceTransferFromServerToClient() throws Exception {
        final WorkspaceManager workspaceManager = context.mock(WorkspaceManager.class);
        IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, new Network());

        int port = server.start();
        IntelliJCoderClient client = new IntelliJCoderClient(new Network(), port);

        context.checking(new Expectations(){{
            allowing(workspaceManager).getSolutionSource("className"); will(returnValue("solution source"));
        }});
        assertEquals("solution class source", "solution source", client.getSolutionSource("className"));
    }

    @Test(timeout = TIMEOUT)
    public void clientRethrowsWorkspaceCreationExceptionOccuredOnServer() throws Exception {
        final WorkspaceManager workspaceManager = context.mock(WorkspaceManager.class);

        IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, new Network());
        int port = server.start();
        IntelliJCoderClient client = new IntelliJCoderClient(new Network(), port);

        context.checking(new Expectations(){{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
            will(throwException(new IntelliJCoderException("big error", null)));
        }});
        try {
            client.createProblemWorkspace(sampleProgram());
            fail("should rethrow exception");
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, "big error");
        }
    }

    @Test(timeout = TIMEOUT)
    public void clientRethrowsGettingSourceExceptionOccuredOnServer() throws Exception {
        final WorkspaceManager workspaceManager = context.mock(WorkspaceManager.class);

        IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, new Network());
        int port = server.start();
        IntelliJCoderClient client = new IntelliJCoderClient(new Network(), port);

        context.checking(new Expectations(){{
            allowing(workspaceManager).getSolutionSource(with(any(String.class)));
            will(throwException(new IntelliJCoderException("big error", null)));
        }});
        try {
            client.getSolutionSource(someClassName());
            fail("should rethrow exception");
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, "big error");
        }
    }

    @Test(timeout = TIMEOUT)
    public void severalRequestsToServer() throws Exception {
        final WorkspaceManager workspaceManager = context.mock(WorkspaceManager.class);
        IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, new Network());

        int port = server.start();
        IntelliJCoderClient client = new IntelliJCoderClient(new Network(), port);

        context.checking(new Expectations(){{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
            allowing(workspaceManager).getSolutionSource("className");    will(returnValue("solution source"));
        }});
        client.createProblemWorkspace(sampleProgram());
        assertEquals("solution class source", "solution source", client.getSolutionSource("className"));
        assertEquals("solution class source requested 2nd time", "solution source", client.getSolutionSource("className"));
    }

    @Test
    public void testProblemTransferFromArenaPluginToIntelliJCoderServer() throws Exception {
        FakeWorkspaceManager workspaceManager = new FakeWorkspaceManager();
        IntelliJCoderServer server = new IntelliJCoderServer(workspaceManager, new Network());

        int port = server.start();

        setSystemProperty(ArenaProcessLauncher.INTELLIJCODER_PORT_PROPERTY, port);
        IntelliJCoderArenaPlugin plugin = new IntelliJCoderArenaPlugin();
        plugin.startUsing();

        final ProblemComponentModel inputComponentModel = context.mock(ProblemComponentModel.class);
        context.checking(new Expectations(){{
            allowing(inputComponentModel).getClassName(); will(returnValue("BinaryCode"));
            allowing(inputComponentModel).getReturnType();   will(returnValue(new DataType("int")));
            allowing(inputComponentModel).getMethodName();   will(returnValue("multiply"));
            allowing(inputComponentModel).getParamTypes();   will(returnValue(new DataType[0]));
            allowing(inputComponentModel).getParamNames();   will(returnValue(new String[0]));
            allowing(inputComponentModel).getTestCases();    will(returnValue(new com.topcoder.shared.problem.TestCase[] {new com.topcoder.shared.problem.TestCase(new String[0], "1", false)}));
        }});
        TestCase testCase = make(a(TestCase, with(input, new String[0]), with(output, "1")));
        Problem expectedProblem = make(a(Problem,
                with(className, "BinaryCode"),
                with(returnType, "int"),
                with(methodName, "multiply"),
                with(testCases, new TestCase[]{testCase})));

        plugin.setProblemComponent(inputComponentModel, JavaLanguage.JAVA_LANGUAGE, null);
        workspaceManager.hasReceivedProblemEqualTo(expectedProblem);
    }


    private void setSystemProperty(String property, int value) {
        Properties properties = System.getProperties();
        properties.put(property, Integer.toString(value));
        System.setProperties(properties);
    }

    private Problem sampleProgram() {
        return make(a(Problem));
    }

    private String someClassName() {
        return "";
    }
}
