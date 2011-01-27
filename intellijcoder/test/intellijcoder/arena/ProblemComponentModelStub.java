package intellijcoder.arena;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.client.contestant.ProblemModel;
import com.topcoder.netCommon.contestantMessages.response.data.ComponentChallengeData;
import com.topcoder.shared.problem.*;

/**
 * @author Konstantin Fadeyev
 *         22.01.11
 */
public class ProblemComponentModelStub implements ProblemComponentModel {
    public Long getID() {
        return null;
    }

    public Integer getComponentTypeID() {
        return null;
    }

    public ProblemModel getProblem() {
        return null;
    }

    public Double getPoints() {
        return null;
    }

    public ProblemComponent getComponent() {
        return null;
    }

    public boolean hasSignature() {
        return false;
    }

    public String getClassName() {
        return null;
    }

    public String getMethodName() {
        return null;
    }

    public DataType getReturnType() {
        return null;
    }

    public DataType[] getParamTypes() {
        return new DataType[0];
    }

    public String[] getParamNames() {
        return new String[0];
    }

    public ComponentChallengeData getComponentChallengeData() {
        return null;
    }

    public boolean hasStatement() {
        return false;
    }

    public boolean hasIntro() {
        return false;
    }

    public Element getIntro() {
        return null;
    }

    public boolean hasSpec() {
        return false;
    }

    public Element getSpec() {
        return null;
    }

    public boolean hasNotes() {
        return false;
    }

    public Element[] getNotes() {
        return new Element[0];
    }

    public boolean hasConstraints() {
        return false;
    }

    public Constraint[] getConstraints() {
        return new Constraint[0];
    }

    public boolean hasTestCases() {
        return false;
    }

    public TestCase[] getTestCases() {
        return new TestCase[0];
    }

    public boolean hasDefaultSolution() {
        return false;
    }

    public String getDefaultSolution() {
        return null;
    }
}
