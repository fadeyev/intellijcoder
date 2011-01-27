package intellijcoder.main;

import intellijcoder.arena.ArenaConfigManager;
import intellijcoder.arena.ArenaJarProvider;
import intellijcoder.arena.ArenaProcessLauncher;
import intellijcoder.main.IntelliJCoderApplication;
import intellijcoder.main.IntelliJCoderException;
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

    private ArenaJarProvider jarProvider = context.mock(ArenaJarProvider.class);
    private IntelliJCoderServer server = context.mock(IntelliJCoderServer.class);
    private ArenaConfigManager configManager = context.mock(ArenaConfigManager.class);
    private ArenaProcessLauncher arenaLauncher = context.mock(ArenaProcessLauncher.class);
    private IntelliJCoderApplication application = new IntelliJCoderApplication(jarProvider, arenaLauncher, server, configManager);


    @Test
    public void serverStartedAndConfigSavedBeforeApplicationStarted() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            oneOf(server).start();  inSequence(serverStartSequence);
            oneOf(configManager).setIntelliJCoderAsADefaultEditor(); inSequence(editConfigSequence);
            oneOf(arenaLauncher).launch(with(any(String.class)), with(any(Integer.class)));   inSequence(serverStartSequence); inSequence(editConfigSequence);
            ignoring(jarProvider);
        }});
        application.launch();
    }

    @Test
    public void arenaApplicationGetsTheSamePortValueAsServer() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            oneOf(server).start();  will(returnValue(1000));
            oneOf(arenaLauncher).launch(with(any(String.class)), with(equal(1000)));
            ignoring(jarProvider);
            ignoring(configManager);
        }});
        application.launch();
    }

    @Test
    public void startsApplicationProvidedByProvider() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            allowing(jarProvider).getJarFilePath(); will(returnValue("applet.jar"));
            oneOf(arenaLauncher).launch(with(equal("applet.jar")), with(any(Integer.class)));
            ignoring(configManager);
            ignoring(server);
        }});
        application.launch();
    }

    @Test
    public void forTwoLaunchesOnlyOneServerStartedButTwoArenaApplications() throws Exception {
        context.checking(new Expectations(){{
            oneOf(server).start(); will(returnValue(1000));
            exactly(2).of(arenaLauncher).launch(with(any(String.class)), with(equal(1000)));
            ignoring(jarProvider);
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
