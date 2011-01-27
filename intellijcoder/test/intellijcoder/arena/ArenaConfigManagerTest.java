package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.FileSystem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.regex.Pattern;

import static intellijcoder.arena.ArenaConfigManager.TOP_CODER_EDITOR_NAME;
import static intellijcoder.arena.ArenaConfigManager.TOP_CODER_SETTINGS_FILE;
import static intellijcoder.util.TestUtil.assertExceptionMessage;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Date: 17.01.11
 *
 * @author Konstantin Fadeyev
 */
@RunWith(JMock.class)
public class ArenaConfigManagerTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private FileSystem fileSystem = context.mock(FileSystem.class);
    private String intelliJCoderPath = "SOME_PATH";
    ArenaConfigManager configManager = new ArenaConfigManager(fileSystem, intelliJCoderPath);

    @Test
    public void setsIntelliJCoderAsADefaultEditorInArenaSettings() throws Exception {
        String outputSettings = processArenaSettings("");
        assertThat(outputSettings, containsString("editor.defaultname=IntelliJCoder"));
    }

    @Test
    public void addIntelliJCoderEditorToArenaSettings() throws Exception {
        String outputSettings = processArenaSettings("editor.numplugins=1");
        assertThat(outputSettings, containsString("editor.numplugins=2"));
        assertThat(outputSettings, containsString("editor.2.name="+ TOP_CODER_EDITOR_NAME));
        assertThat(outputSettings, containsString("editor.2.entrypoint="+ IntelliJCoderArenaPlugin.class.getName()));
        assertThat(outputSettings, containsString("editor.2.classpath=" + intelliJCoderPath));
    }


    @Test
    public void replacesEditorSettingIfItExistsButClassPathOrEntryPointDiffer() throws Exception {
        String outputSettings = processArenaSettings(
                        "editor.numplugins=3" + "\n" +
                        "editor.2.name=" + TOP_CODER_EDITOR_NAME + "\n" +
                        "editor.2.entrypoint=INVALID_ENTRY_POINT" + "\n" +
                        "editor.2.classpath=INVALID_CLASSPATH");
        assertThat(outputSettings, containsString("editor.numplugins=3"));
        assertThat(outputSettings, containsString("editor.2.name="+TOP_CODER_EDITOR_NAME));
        assertThat(outputSettings, containsString("editor.2.entrypoint=" + IntelliJCoderArenaPlugin.class.getName()));
        assertThat(outputSettings, containsString("editor.2.classpath=" + intelliJCoderPath));
    }

    @Test
    public void settingsSavedWhenThereIsNoSettingsFile() throws Exception {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.checking(new Expectations(){{
            allowing(fileSystem).isFileExists(with(any(String.class))); will(returnValue(false));
            never(fileSystem).getFileInputStream(TOP_CODER_SETTINGS_FILE);
            atLeast(1).of(fileSystem).getFileOutputStream(TOP_CODER_SETTINGS_FILE);    will(returnValue(outputStream));
        }});
        configManager.setIntelliJCoderAsADefaultEditor();

        assertThat(outputStream.toString(), containsString("editor.numplugins=1"));
        assertThat(outputStream.toString(), containsString("editor.1.name="+ TOP_CODER_EDITOR_NAME));
    }


    @Test
    public void throwsApplicationExceptionIfErrorOccuredWhileLoadingSettings() throws Exception {
        context.checking(new Expectations() {{
            allowing(fileSystem).isFileExists(with(any(String.class))); will(returnValue(true));
            allowing(fileSystem).getFileInputStream(with(any(String.class))); will(throwException(new FileNotFoundException()));
            allowing(fileSystem).getFileOutputStream(with(any(String.class)));
        }});
        try {
            configManager.setIntelliJCoderAsADefaultEditor();
            fail("should throw " + IntelliJCoderException.class.getName());
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, ArenaConfigManager.FAILED_TO_LOAD_SETTINGS_MESSAGE);
        }
    }


    @Test
    public void throwsApplicationExceptionWhenFailedToSaveSettings() throws Exception {
        context.checking(new Expectations() {{
            allowing(fileSystem).isFileExists(with(any(String.class)));
            allowing(fileSystem).getFileInputStream(with(any(String.class)));
            allowing(fileSystem).getFileOutputStream(with(any(String.class))); will(throwException(new FileNotFoundException()));
        }});
        try {
            configManager.setIntelliJCoderAsADefaultEditor();
            fail("should throw " + IntelliJCoderException.class.getName());
        } catch (IntelliJCoderException e) {
            assertExceptionMessage(e, ArenaConfigManager.FAILED_TO_SAVE_SETTINGS_MESSAGE);
        }
    }

    private String processArenaSettings(String sourceSettings) throws IntelliJCoderException, IOException {
        final InputStream inputStream = new ByteArrayInputStream(sourceSettings.getBytes());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        context.checking(new Expectations() {{
            allowing(fileSystem).isFileExists(with(any(String.class))); will(returnValue(true));
            allowing(fileSystem).getFileInputStream(TOP_CODER_SETTINGS_FILE);  will(returnValue(inputStream));
            atLeast(1).of(fileSystem).getFileOutputStream(TOP_CODER_SETTINGS_FILE);    will(returnValue(outputStream));
        }});
        configManager.setIntelliJCoderAsADefaultEditor();
        return outputStream.toString();
    }

    private static Matcher<String> containsInOneRow(String... rowParts) {
        StringBuilder row = new StringBuilder("(?s).*");
        for (int i = 0, rowPartsLength = rowParts.length; i < rowPartsLength; i++) {
            if(i!=0) {
                row.append("[^\\n]*");
            }
            row.append(Pattern.quote(rowParts[i]));
        }
        row.append(".*");
        return new RegexMatcher(row.toString());
    }

    public static class RegexMatcher extends BaseMatcher<String> {
        private final String regex;

        public RegexMatcher(String regex) {
            this.regex = regex;
        }

        public boolean matches(Object o) {
            return ((String) o).matches(regex);

        }

        public void describeTo(Description description) {
            description.appendText("matches regex=" + regex);
        }
    }
}
