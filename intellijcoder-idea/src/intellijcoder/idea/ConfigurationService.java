package intellijcoder.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import intellijcoder.model.SolutionCfg;
import org.jetbrains.annotations.NotNull;

@State(
        name="IntelliJCoderConfiguration",
        storages = {
                @Storage(value="$APP_CONFIG$/IntelliJCoder.xml",
                        roamingType = RoamingType.DISABLED),
        }
)
@Service(value= {Service.Level.APP})
public final class ConfigurationService implements PersistentStateComponent<SolutionCfg> {
    public static ConfigurationService getInstance() {
        return ApplicationManager.getApplication().getService(ConfigurationService.class);
    }

    private SolutionCfg state = new SolutionCfg();

    @NotNull
    public SolutionCfg getState() {
        return state;
    }

    public void loadState(SolutionCfg state) {
        this.state.moduleNamingConvention = state.moduleNamingConvention;
        this.state.sourceFolderName = state. sourceFolderName;
        this.state.testFolderName = state.testFolderName;
        this.state.resourceFolderName = state.resourceFolderName;
        this.state.useTimeLimit = state.useTimeLimit;
        this.state.imports = state.imports;
        this.state.helperCode = state.helperCode;
    }

    @NotNull
    public String getComponentName() {
        return "IntellijCoder.ConfigurationService";
    }
}
