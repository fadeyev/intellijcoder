package intellijcoder.workspace;

import intellijcoder.model.Problem;

/**
* @author Konstantin Fadeyev
*         21.01.11
*/
public class TestCodeBuilder extends CodeBuilder {
    @Override
    protected void doBuild(Problem problem) {
        addRow("import org.junit.Test;");
        addRow("import static org.junit.Assert.*;");
        addRow("");
        startBlock("public class " + problem.getClassName() + "Test");
            for(int i = 0; i < problem.getTestCases().length; i++) {
                addTestCase(problem, i);
            }
        endBlock();
    }


    private void addTestCase(Problem problem, int testIndex) {
        addRow("");
        addRow("@Test");
        startBlock("public void test"+testIndex+"()");
            addInputVariables(problem, testIndex);
        String assertStatement = getAssertStatement(problem, testIndex);
        addRow(assertStatement);
        endBlock();
    }

    private String getAssertStatement(Problem problem, int testIndex) {
        String expectedValue = problem.getTestCases()[testIndex].getOutput();
        String ending;
        String returnType = problem.getReturnType();
        if(returnType.contains("double")) {
            ending = ", 1e-9);";
        } else {
            ending = ");";
        }
        return getAssertMethodName(problem) + "(" + getValueLiteral(returnType, expectedValue) + ", "
                + getRunStatement(problem) + ending;
    }

    private String getAssertMethodName(Problem problem) {
        if(problem.getReturnType().contains("[")) {
            return "assertArrayEquals";
        }
        return "assertEquals";
    }

    private String getRunStatement(Problem problem) {
        return "new " + problem.getClassName() + "()." + problem.getMethodName() + "(" + getParamNamesList(problem) + ")";
    }

    private void addInputVariables(Problem problem, int testIndex) {
        for (int i = 0; i < problem.getParamNames().length; i++) {
            String type = problem.getParamTypes()[i];
            String paramName = problem.getParamNames()[i];
            String inputValue = problem.getTestCases()[testIndex].getInput()[i];
            addRow(type + " " + paramName + " = " + getValueLiteral(type, inputValue) + ";");
        }
    }

    private String getValueLiteral(String type, String value) {
        if(type.contains("[")) {
            return "new " + type + " " + value;
        }
        return value;
    }

    private String getParamNamesList(Problem problem) {
        StringBuilder result = new StringBuilder();
        String[] paramNames = problem.getParamNames();
        for (int i = 0; i < paramNames.length; i++) {
            if(i != 0) {
                result.append(", ");
            }
            result.append(paramNames[i]);
        }
        return result.toString();
    }
}
