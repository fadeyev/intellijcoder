package intellijcoder.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Konstantin Fadeyev
 *         13.01.11
 */
public class Problem implements Serializable {
    private String contestName;
    private String className;
    private String returnType;
    private String methodName;
    private String[] paramTypes;
    private String[] paramNames;
    private TestCase[] testCases;
    private String htmlDescription;
    private int timeLimit;  // in milliseconds
    private int memLimit;   // in megabytes

    public Problem(String contestName, String className, String returnType, String methodName, String[] paramTypes, String[] paramNames, TestCase[] testCases, String htmlDescription, int timeLimit, int memLimit) {
        this.contestName = contestName;
        this.className = className;
        this.returnType = returnType;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
        this.testCases = testCases;
        this.htmlDescription = htmlDescription;
        this.timeLimit = timeLimit;
        this.memLimit = memLimit;
    }

    public String getContestName() {
        return contestName;
    }

    public String getHtmlDescription() {
        return htmlDescription;
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

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getMemLimit() {
        return memLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Problem that = (Problem) o;

        if (contestName != null ? !contestName.equals(that.contestName) : that.contestName != null) return false;
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
        int result = contestName != null ? contestName.hashCode() : 0;
        result = 31 * result + (className != null ? className.hashCode() : 0);
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
                "contestName='" + contestName + '\'' +
                ", className='" + className + '\'' +
                ", returnType='" + returnType + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + (paramTypes == null ? null : Arrays.asList(paramTypes)) +
                ", paramNames=" + (paramNames == null ? null : Arrays.asList(paramNames)) +
                ", testCases=" + (testCases == null ? null : Arrays.asList(testCases)) +
                ", timeLimit=" + timeLimit +
                ", memLimit=" + memLimit +
                '}';
    }
}
