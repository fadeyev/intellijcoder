package intellijcoder.idea;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import intellijcoder.model.SolutionCfg;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationForm implements Configurable {
    private JTextArea taImports;
    private JTextArea taHelperCode;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JTextField taSourceFolder;
    private JTextField taTestFolder;
    private JTextField taResourceFolder;
    private JComboBox taModuleNamingConvention;
    private JCheckBox taUseTimeLimit;
    private JCheckBox taUseMemoryLimit;
    private JTextPane NOTETheFollowingSettingsTextPane;
    private SolutionCfg initialCfg;

    @Nls
    public String getDisplayName() {
        return "IntelliJCoder";
    }

    private SolutionCfg getConfig() {
        return new SolutionCfg(
                SolutionCfg.ModuleNamingConvention.values()[taModuleNamingConvention.getSelectedIndex()],
                taSourceFolder.getText(),
                taTestFolder.getText(),
                taResourceFolder.getText(),
                taUseTimeLimit.isSelected(),
                taImports.getText(),
                taHelperCode.getText());
    }

    void setConfig(SolutionCfg cfg) {
        taModuleNamingConvention.setSelectedIndex(cfg.moduleNamingConvention.ordinal());
        taSourceFolder.setText(cfg.sourceFolderName);
        taTestFolder.setText(cfg.testFolderName);
        taResourceFolder.setText(cfg.resourceFolderName);
        taUseTimeLimit.setSelected(cfg.useTimeLimit);
        taImports.setText(cfg.imports);
        taHelperCode.setText(cfg.helperCode);
    }

    @Nullable
    public JComponent createComponent() {
        initialCfg = ConfigurationService.getInstance().getState();
        setConfig(initialCfg);
        return mainPanel;
    }

    public boolean isModified() {
        return !getConfig().equals(initialCfg);
    }

    public void apply() throws ConfigurationException {
        ConfigurationService.getInstance().loadState(getConfig());
    }

    public void reset() {
        setConfig(initialCfg);
    }

    @Nullable
    public String getHelpTopic() {
        return null;
    }

    public void disposeUIResources() {}
}
