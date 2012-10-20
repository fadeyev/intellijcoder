package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.Network;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(JMock.class)
public class ArenaAppletProviderTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    Network network =  context.mock(Network.class);
    ArenaFileParser fileParser = context.mock(ArenaFileParser.class);
    ArenaJarDownloader jarDownloader = context.mock(ArenaJarDownloader.class);
    ArenaAppletProvider appletProvider = new ArenaAppletProvider(network, fileParser, jarDownloader);

    @Test
    public void parsesLoadedFile() throws IOException, IntelliJCoderException {
        final InputStream jnlpFileInputStream = new ByteArrayInputStream(new byte[]{});

        context.checking(new Expectations(){{
            atLeast(1).of(network).getUrlInputStream("http://www.topcoder.com/contest/arena/ContestAppletProd.jnlp");
                will(returnValue(jnlpFileInputStream));
            atLeast(1).of(fileParser).parse(with(equal(jnlpFileInputStream)));
            ignoring(jarDownloader);
        }});
        appletProvider.getApplet();
    }

    @Test
    public void downloadsJarFiles() throws IntelliJCoderException {
        final ArenaAppletInfo appletInfoWithRemoteJars = new ArenaAppletInfo();
        appletInfoWithRemoteJars.addClassPathItem("http://somewhere.in.the.far.far.galaxy.jar");

        final ArenaAppletInfo appletInfoWithLocalJars = new ArenaAppletInfo();
        appletInfoWithLocalJars.addClassPathItem("c:\\local.jar");

        context.checking(new Expectations(){{
            allowing(fileParser).parse(with(any(InputStream.class)));
                    will(returnValue(appletInfoWithRemoteJars));
            allowing(jarDownloader).loadArenaJars(appletInfoWithRemoteJars);
                    will(returnValue(appletInfoWithLocalJars));
            ignoring(network);
        }});

        appletProvider.getApplet();
    }
}
