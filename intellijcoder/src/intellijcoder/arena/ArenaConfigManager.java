package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.os.FileSystem;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages Competition Arena config settings
 *
 * Date: 17.01.11
 *
 * @author Konstantin Fadeyev
 */
public class ArenaConfigManager {
    public static final String TOP_CODER_EDITOR_NAME = "IntelliJCoder";
    public static final String TOP_CODER_SETTINGS_FILE = System.getProperty("user.home") + File.separator + "contestapplet.conf";
    public static final String FAILED_TO_SAVE_SETTINGS_MESSAGE = "Failed to save Competition Arena settings";
    public static final String FAILED_TO_LOAD_SETTINGS_MESSAGE = "Failed to load Competition Arena settings";

    private FileSystem fileSystem;
    private String intelliJCoderClassPath;

    public ArenaConfigManager(FileSystem fileSystem, String intelliJCoderClassPath) {
        this.fileSystem = fileSystem;
        this.intelliJCoderClassPath = intelliJCoderClassPath;
    }

    public void setIntelliJCoderAsADefaultEditor() throws IntelliJCoderException {
        Properties properties = loadSettings();
        setEditorInSettings(properties);
        saveSettings(properties);
    }

    private void setEditorInSettings(Properties properties) {
        properties.put("editor.defaultname", TOP_CODER_EDITOR_NAME);
        int pluginsCount = 0;
        if(properties.containsKey("editor.numplugins")) {
            pluginsCount = Integer.parseInt(properties.getProperty("editor.numplugins"));
        }
        int intellijCoderIndex = 1;
        for (; intellijCoderIndex <= pluginsCount; intellijCoderIndex++) {
             if(TOP_CODER_EDITOR_NAME.equals(properties.getProperty("editor." + intellijCoderIndex + ".name"))) {
                 break;
             }
        }
        pluginsCount = Math.max(pluginsCount, intellijCoderIndex);
        properties.put("editor.numplugins", Integer.toString(pluginsCount));
        properties.put("editor."+intellijCoderIndex+".name", TOP_CODER_EDITOR_NAME);
        properties.put("editor."+intellijCoderIndex+".entrypoint", IntelliJCoderArenaPlugin.class.getName());
        properties.put("editor."+intellijCoderIndex+".classpath", intelliJCoderClassPath);
        properties.put("editor."+intellijCoderIndex+".eager", "0");
    }

    private void saveSettings(Properties properties) throws IntelliJCoderException {
        try {
            properties.store(fileSystem.getFileOutputStream(TOP_CODER_SETTINGS_FILE), "");
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_SAVE_SETTINGS_MESSAGE, e);
        }
    }

    private Properties loadSettings() throws IntelliJCoderException {
        Properties properties = new Properties();
        if(fileSystem.isFileExists(TOP_CODER_SETTINGS_FILE)) {
            try {
                properties.load(fileSystem.getFileInputStream(TOP_CODER_SETTINGS_FILE));
            } catch (IOException e) {
                throw new IntelliJCoderException(FAILED_TO_LOAD_SETTINGS_MESSAGE, e);
            }
        }
        return properties;
    }
}
