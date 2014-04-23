package intellijcoder.model;

import org.jetbrains.annotations.NotNull;

public class SolutionCfg {
    // Settings tab
    @NotNull public ModuleNamingConvention moduleNamingConvention = ModuleNamingConvention.BY_CONTEST_NAME;
    @NotNull public String sourceFolderName = "src";
    @NotNull public String testFolderName = "test";
    @NotNull public String resourceFolderName = "resource";
    @NotNull public Boolean useTimeLimit = Boolean.TRUE;
    // Code tab
    @NotNull public String imports =
            "import java.util.*;\n" +
            "import java.math.*;\n" +
            "import static java.lang.Math.*;";
    @NotNull public String helperCode = "";

    // NOTE:  The order of these conventions must match the order of the conventions as defined in the form
    public enum ModuleNamingConvention {
        BY_CLASS_NAME,
        BY_CONTEST_NAME
    }

    public SolutionCfg(ModuleNamingConvention moduleNamingConvention, String sourceFolderName, String testFolderName, String resourceFolderName, Boolean useTimeLimit, String imports, String helperCode) {
        this.moduleNamingConvention = moduleNamingConvention;
        this.sourceFolderName = sourceFolderName;
        this.testFolderName = testFolderName;
        this.resourceFolderName = resourceFolderName;
        this.useTimeLimit = useTimeLimit;
        this.imports = imports;
        this.helperCode = helperCode;
    }
    public SolutionCfg() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SolutionCfg o = (SolutionCfg) obj;
        return o.moduleNamingConvention.equals(moduleNamingConvention) &&
                o.sourceFolderName.equals(sourceFolderName) &&
                o.testFolderName.equals(testFolderName) &&
                o.resourceFolderName.equals(resourceFolderName) &&
                o.useTimeLimit.equals(useTimeLimit) &&
                o.imports.equals(imports) &&
                o.helperCode.equals(helperCode);
    }
}
