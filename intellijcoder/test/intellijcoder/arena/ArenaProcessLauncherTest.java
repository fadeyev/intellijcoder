package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.ProcessLauncher;
import intellijcoder.util.TestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

/**
 * @author Konstantin Fadeyev
 *         12.01.11
 */
@RunWith(JMock.class)
public class ArenaProcessLauncherTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private ProcessLauncher processLauncher = context.mock(ProcessLauncher.class);
    private ArenaProcessLauncher arenaLauncher = new ArenaProcessLauncher(processLauncher);

    @Test
    public void jarLaunchedInNewProcessWithNecessaryParameters() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            oneOf(processLauncher).launch(
                    with(TestUtil.hasItemsInArray(
                            containsString("java"),
                            equal("-cp"), equal("contestApplet.jar"),
                            equal("com.topcoder.client.contestApplet.runner.generic"),
                            equal("-Dintellijcoder.port=9999"),
                            equal("www.topcoder.com"),
                            equal("5001"),
                            equal("http://tunnel1.topcoder.com/tunnel?dummy"),
                            equal("TopCoder"))));
        }});
        arenaLauncher.launch("contestApplet.jar", 9999);
    }

    @Test
    public void throwsApplicationExceptionWhenFailedToLaunchProcess() throws Exception {
        context.checking(new Expectations() {{
            oneOf(processLauncher).launch(with(any(String[].class))); will(throwException(new IOException()));
        }});
        try {
            arenaLauncher.launch(someJarPath(), somePort());
            fail("launch() should have thrown " + IntelliJCoderException.class.getName());
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, ArenaProcessLauncher.FAILED_TO_START_PROCESS_MESSAGE);
        }
    }

    private int somePort() {
        return 9999;
    }

    private String someJarPath() {
        return "ContestApplet.jar";
    }
}
