package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.FileSystem;
import intellijcoder.os.Network;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.Arrays;

import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.*;

/**
 * @author Konstantin Fadeyev
 *         12.01.11
 */
@RunWith(JMock.class)
public class ArenaJarProviderTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private FileSystem fileSystem = context.mock(FileSystem.class);
    private Network network = context.mock(Network.class);
    private ArenaJarProvider jarProvider = new ArenaJarProvider(network, fileSystem);


    @Test
    public void downloadsFileWithMixedContent() throws Exception {
        byte[] inputArray = {0, 1, 2, 3, 4};
        contentOfDowloadedFileEqualsToDownloadable(inputArray);
    }

    @Test
    public void downloadsLargeFile() throws Exception {
        byte[] inputArray = new byte[100000];
        Arrays.fill(inputArray, (byte) 42);
        contentOfDowloadedFileEqualsToDownloadable(inputArray);
    }

    @Test
    public void downloadReturnsValidFileName() throws Exception {
        context.checking(new Expectations() {{
            allowing(network).getUrlInputStream(with(any(String.class)));   will(returnValue(someInputStream()));
            ignoring(fileSystem);
        }});
        assertThat("file name", jarProvider.getJarFilePath(), endsWith(".jar"));
    }

    @Test
    public void throwsApplicationExceptionWhenFailedToDownloadFile() throws IOException, IntelliJCoderException {
        context.checking(new Expectations() {{
            allowing(network).getUrlInputStream(with(any(String.class))); will(throwException(new IOException()));
            ignoring(fileSystem);
        }});
        try {
            jarProvider.getJarFilePath();
            fail("should throw " + IntelliJCoderException.class.getSimpleName());
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, ArenaJarProvider.FAILED_TO_GET_JAR_MESSAGE);
        }
    }

    @Test
    public void subsequentCallsToDownloadSavesNewFileEveryTime() throws Exception {
        context.checking(new Expectations() {{
            allowing(network).getUrlInputStream(with(any(String.class)));   will(returnValue(someInputStream()));
            allowing(fileSystem);
        }});
        String file1 = jarProvider.getJarFilePath();
        String file2 = jarProvider.getJarFilePath();
        assertFalse("files should not be equal", file1.equals(file2));
    }

    private void contentOfDowloadedFileEqualsToDownloadable(byte[] inputArray) throws Exception {
        final InputStream inputStream = new ByteArrayInputStream(inputArray);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final String expectedAppletUrl = "http://www.topcoder.com/contest/classes/ContestApplet.jar";

        context.checking(new Expectations() {{
            atLeast(1).of(network).getUrlInputStream(expectedAppletUrl); will(returnValue(inputStream));
            atLeast(1).of(fileSystem).getFileOutputStream(with(any(String.class)));  will(returnValue(outputStream));
        }});
        jarProvider.getJarFilePath();
        assertArrayEquals(inputArray, outputStream.toByteArray());
    }

    private OutputStream someOutputStream() {
        return new ByteArrayOutputStream();
    }

    private InputStream someInputStream() {
        return new ByteArrayInputStream(new byte[]{});
    }
}

