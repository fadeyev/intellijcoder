package intellijcoder.main;

import intellijcoder.arena.ArenaAppletInfo;
import intellijcoder.arena.ArenaAppletProvider;
import intellijcoder.arena.ArenaConfigManager;
import intellijcoder.arena.ArenaProcessLauncher;
import intellijcoder.ipc.IntelliJCoderServer;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author Konstantin Fadeyev
 *         22.01.11
 */
@RunWith(JMock.class)
public class IntelliJCoderApplicationTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private Sequence serverStartSequence = context.sequence("startServer");
    private Sequence editConfigSequence = context.sequence("editConfig");

    private ArenaAppletProvider appletProvider = context.mock(ArenaAppletProvider.class);
    private IntelliJCoderServer server = context.mock(IntelliJCoderServer.class);
    private ArenaConfigManager configManager = context.mock(ArenaConfigManager.class);
    private ArenaProcessLauncher arenaLauncher = context.mock(ArenaProcessLauncher.class);
    private IntelliJCoderApplication application = new IntelliJCoderApplication(appletProvider, arenaLauncher, server, configManager);


    @Test
    public void serverStartedAndConfigSavedBeforeApplicationStarted() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            oneOf(server).start();  inSequence(serverStartSequence);
            oneOf(configManager).setIntelliJCoderAsADefaultEditor(); inSequence(editConfigSequence);
            oneOf(arenaLauncher).launch(with(any(ArenaAppletInfo.class)), with(any(Integer.class)));   inSequence(serverStartSequence); inSequence(editConfigSequence);
            ignoring(appletProvider);
        }});
        application.launch();
    }

    @Test
    public void arenaApplicationGetsTheSamePortValueAsServer() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            oneOf(server).start();  will(returnValue(1000));
            oneOf(arenaLauncher).launch(with(any(ArenaAppletInfo.class)), with(equal(1000)));
            ignoring(appletProvider);
            ignoring(configManager);
        }});
        application.launch();
    }

    @Test
    public void startsApplicationProvidedByProvider() throws IOException, IntelliJCoderException {
        final ArenaAppletInfo arenaAppletInfo = new ArenaAppletInfo();

        context.checking(new Expectations() {{
            allowing(appletProvider).getApplet(); will(returnValue(arenaAppletInfo));
            oneOf(arenaLauncher).launch(with(equal(arenaAppletInfo)), with(any(Integer.class)));
            ignoring(configManager);
            ignoring(server);
        }});
        application.launch();
    }

    @Test
    public void forTwoLaunchesOnlyOneServerStartedButTwoArenaApplications() throws Exception {
        context.checking(new Expectations(){{
            oneOf(server).start(); will(returnValue(1000));
            exactly(2).of(arenaLauncher).launch(with(any(ArenaAppletInfo.class)), with(equal(1000)));
            ignoring(appletProvider);
            ignoring(configManager);
        }});
        application.launch();
        application.launch();
    }

    @Test
    public void shutdownStopsServer() throws Exception {
        context.checking(new Expectations(){{
            oneOf(server).stop();
        }});
        application.shutdown();
    }
}
