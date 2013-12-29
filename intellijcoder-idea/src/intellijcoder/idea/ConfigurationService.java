package intellijcoder.idea;

import com.intellij.openapi.components.*;
import intellijcoder.model.SolutionCfg;
import org.jetbrains.annotations.NotNull;

@State(
        name="IntelliJCoderConfiguration",
        storages = {
                @Storage(file = StoragePathMacros.APP_CONFIG + "/IntelliJCoder.xml"),
        }
)
public class ConfigurationService implements PersistentStateComponent<SolutionCfg>, ApplicationComponent {
    public static ConfigurationService getInstance() {
        return ServiceManager.getService(ConfigurationService.class);
    }

    private SolutionCfg state = new SolutionCfg();

    @NotNull
    public SolutionCfg getState() {
        return state;
    }

    public void loadState(SolutionCfg state) {
        this.state.imports = state.imports;
        this.state.helperCode = state.helperCode;
    }

    @NotNull
    public String getComponentName() {
        return "IntellijCoder.ConfigurationService";
    }

    public void initComponent() {}

    public void disposeComponent() {}
}
