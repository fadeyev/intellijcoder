package intellijcoder.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Konstantin Fadeyev
 *         13.01.11
 */
public class Problem implements Serializable {
    private String className;
    private String returnType;
    private String methodName;
    private String[] paramTypes;
    private String[] paramNames;
    private TestCase[] testCases;

    public Problem(String className, String returnType, String methodName, String[] paramTypes, String[] paramNames, TestCase[] testCases) {
        this.className = className;
        this.returnType = returnType;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
        this.testCases = testCases;
    }

    public String getClassName() {
        return className;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }
    public String[] getParamTypes() {
        return paramTypes;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public TestCase[] getTestCases() {
        return testCases;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Problem that = (Problem) o;

        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (!Arrays.equals(paramNames, that.paramNames)) return false;
        if (!Arrays.equals(paramTypes, that.paramTypes)) return false;
        if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) return false;
        //noinspection RedundantIfStatement
        if (!Arrays.equals(testCases, that.testCases)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (paramTypes != null ? Arrays.hashCode(paramTypes) : 0);
        result = 31 * result + (paramNames != null ? Arrays.hashCode(paramNames) : 0);
        result = 31 * result + (testCases != null ? Arrays.hashCode(testCases) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "className='" + className + '\'' +
                ", returnType='" + returnType + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + (paramTypes == null ? null : Arrays.asList(paramTypes)) +
                ", paramNames=" + (paramNames == null ? null : Arrays.asList(paramNames)) +
                ", testCases=" + (testCases == null ? null : Arrays.asList(testCases)) +
                '}';
    }
}
