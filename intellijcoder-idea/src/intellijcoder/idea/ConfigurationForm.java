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
    private SolutionCfg initialCfg;

    @Nls
    public String getDisplayName() {
        return "IntelliJCoder";
    }

    private SolutionCfg getConfig() {
        return new SolutionCfg(taImports.getText(), taHelperCode.getText());
    }

    void setConfig(SolutionCfg cfg) {
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
