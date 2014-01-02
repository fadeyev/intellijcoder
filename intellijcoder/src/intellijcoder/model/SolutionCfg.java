package intellijcoder.model;

import org.jetbrains.annotations.NotNull;

public class SolutionCfg {
    @NotNull public String imports =
            "import java.util.*;\n" +
            "import java.math.*;\n" +
            "import static java.lang.Math.*;";
    @NotNull public String helperCode = "";

    public SolutionCfg(String imports, String helperCode) {
        this.imports = imports;
        this.helperCode = helperCode;
    }
    public SolutionCfg() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SolutionCfg o = (SolutionCfg) obj;
        return o.imports.equals(imports) && o.helperCode.equals(helperCode);
    }
}
