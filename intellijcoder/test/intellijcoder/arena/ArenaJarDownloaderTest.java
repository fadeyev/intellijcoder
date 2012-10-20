package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.FileSystem;
import intellijcoder.os.Network;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.CustomAction;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Konstantin Fadeyev
 *         12.01.11
 */
@RunWith(JMock.class)
public class ArenaJarDownloaderTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private FileSystem fileSystem = context.mock(FileSystem.class);
    private Network network = context.mock(Network.class);
    private ArenaJarDownloader jarDownloader = new ArenaJarDownloader(network, fileSystem);
    private String localFileName;

    @Test
    public void returnsTheSameAppletInfoButWithLocalJarReferences() throws Exception {
        ArenaAppletInfo appletInfo = new ArenaAppletInfo();
        appletInfo.setMainClass("main");
        appletInfo.addArgument("arg1");
        appletInfo.addClassPathItem("http://somewhere/arena.jar");

        context.checking(new Expectations() {{
            allowing(network).getUrlInputStream("http://somewhere/arena.jar");
            will(returnValue(someInputStream()));
            atLeast(1).of(fileSystem).getFileOutputStream(with(endsWith("arena.jar")));
            will(saveLocalFileName());

            ignoring(fileSystem);
        }});
        ArenaAppletInfo localAppletInfo = jarDownloader.loadArenaJars(appletInfo);
        assertThat("class path", localAppletInfo.getClassPath(), equalTo(localFileName));
        assertThat("main class", localAppletInfo.getMainClass(), equalTo("main"));
        assertThat("arguments", localAppletInfo.getArguments(), contains("arg1"));
    }

    private CustomAction saveLocalFileName() {
        return new CustomAction("file") {
            public Object invoke(Invocation invocation) throws Throwable {
                localFileName = (String) invocation.getParameter(0);
                return new ByteArrayOutputStream();
            }
        };
    }

    @Test
    public void throwsApplicationExceptionWhenFailedToDownloadFile() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            allowing(network).getUrlInputStream(with(any(String.class)));
            will(throwException(new IOException()));
            ignoring(fileSystem);
        }});
        try {
            jarDownloader.loadArenaJars(someAppletInfo());
            fail("should throw " + IntelliJCoderException.class.getSimpleName());
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, ArenaJarDownloader.FAILED_TO_GET_JAR_MESSAGE);
        }
    }


    @Test
    public void subsequentCallsToDownloadSaveNewFilesEveryTime() throws Exception {
        ArenaAppletInfo appletInfo = someAppletInfo();

        context.checking(new Expectations() {{
            allowing(network).getUrlInputStream(with(any(String.class)));
                will(returnValue(someInputStream()));
            allowing(fileSystem);
        }});
        String cp1 = jarDownloader.loadArenaJars(appletInfo).getClassPath();
        String cp2 = jarDownloader.loadArenaJars(appletInfo).getClassPath();
        assertFalse("class pathes should not be equal", cp1.equals(cp2));
    }

    @Test
    public void contentOfDowloadedFileEqualsToDownloadable() throws Exception {
        byte[] inputArray = {1, 2};
        final InputStream inputStream = new ByteArrayInputStream(inputArray);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        context.checking(new Expectations() {{
            atLeast(1).of(network).getUrlInputStream(with(any(String.class)));
                will(returnValue(inputStream));
            atLeast(1).of(fileSystem).getFileOutputStream(with(any(String.class)));
                will(returnValue(outputStream));
        }});
        jarDownloader.loadArenaJars(someAppletInfo());
        assertArrayEquals(inputArray, outputStream.toByteArray());
    }

    private static ArenaAppletInfo someAppletInfo() {
        ArenaAppletInfo appletInfo = new ArenaAppletInfo();
        appletInfo.addClassPathItem("http://some/some.jar");
        return appletInfo;
    }

    private InputStream someInputStream() {
        return new ByteArrayInputStream(new byte[]{});
    }
}

