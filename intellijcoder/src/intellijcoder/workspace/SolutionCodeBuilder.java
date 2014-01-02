package intellijcoder.workspace;

import intellijcoder.model.Problem;
import intellijcoder.model.SolutionCfg;

/**
* @author Konstantin Fadeyev
*         21.01.11
*/
public class SolutionCodeBuilder extends CodeBuilder {

    private SolutionCfg cfg;

    public SolutionCodeBuilder(SolutionCfg cfg) {
        this.cfg = cfg;
    }

    @Override
    protected void doBuild(Problem problem) {
        addRow(cfg.imports);
        addRow("");
        startBlock("public class " + problem.getClassName());
            for (String s : cfg.helperCode.split("\n"))
                addRow(s);
            startBlock("public " + problem.getReturnType() + " " + problem.getMethodName() + "("+ getParamList(problem) + ")");
                addRow("return " + getReturnValue(problem.getReturnType()) + ";");
            endBlock();
        endBlock();
    }

    private String getParamList(Problem problem) {
        StringBuilder result = new StringBuilder();
        String[] paramNames = problem.getParamNames();
        for (int i = 0; i < paramNames.length; i++) {
            if(i != 0) {
                result.append(", ");
            }
            result.append(problem.getParamTypes()[i]).append(' ').append(paramNames[i]);
        }
        return result.toString();
    }

    private String getReturnValue(String returnType) {
        if ("boolean".equals(returnType)) {
            return "false";
        }
        if (isPrimitiveNumber(returnType)) {
            return "0";
        } else {
            return "null";
        }
    }

    private boolean isPrimitiveNumber(String returnType) {
        return "byte".equals(returnType) || "short".equals(returnType) || "int".equals(returnType)
                || "long".equals(returnType) || "float".equals(returnType)
                || "double".equals(returnType) || "char".equals(returnType);
    }
}
