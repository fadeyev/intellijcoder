package intellijcoder.model;

import com.natpryce.makeiteasy.Maker;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static intellijcoder.model.ProblemMaker.Problem;
import static intellijcoder.model.ProblemMaker.TestCase;
import static intellijcoder.model.ProblemMaker.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Date: 14.01.11
 *
 * @author Konstantin Fadeyev
 */
public class ProblemTest {

    @Test
    public void valueObject() {
        Maker<TestCase> defaultTestCase = a(TestCase,
                with(input, new String[]{"11111"}), with(output, "00000"));
        Maker<Problem> defaultProblem = a(Problem,
                with(className, "BinaryCode"), with(methodName, "decode"), with(returnType, "String[]"),
                with(paramTypes, new String[]{"String"}), with(paramNames, new String[]{"message"}),
                with(testCases, new TestCase[] { make(defaultTestCase) }));

        Problem problem = make(defaultProblem);
        Problem problemCopy = make(defaultProblem);
        Problem problemWithDifferentClassName = make(defaultProblem.but(with(className, "Lottery")));
        Problem problemWithDifferentReturnType = make(defaultProblem.but(with(returnType, "float")));
        Problem problemWithDifferentMethodName = make(defaultProblem.but(with(methodName, "calculate")));
        Problem problemWithDifferentParamType = make(defaultProblem.but(with(paramTypes, new String[]{"int"})));
        Problem problemWithDifferentParamName = make(defaultProblem.but(with(paramNames, new String[]{"m"})));
        Problem problemWithDifferentTestCase = make(defaultProblem.but(
                with(testCases, new TestCase[] { make(defaultTestCase.but(with(output, "2"))) })));

        assertTrue("problems with the same fields should be equal", problem.equals(problemCopy));
        assertTrue("hash codes of problems with the same fields should be equal", problem.hashCode() == problemCopy.hashCode());
        assertFalse("problems with different class names should not be equal", problem.equals(problemWithDifferentClassName));
        assertFalse("problems with different return types should not be equal", problem.equals(problemWithDifferentReturnType));
        assertFalse("problems with different method names should not be equal", problem.equals(problemWithDifferentMethodName));
        assertFalse("problems with different param types should not be equal", problem.equals(problemWithDifferentParamType));
        assertFalse("problems with different param names should not be equal", problem.equals(problemWithDifferentParamName));
        assertFalse("problems with different test cases should not be equal", problem.equals(problemWithDifferentTestCase));
    }
}
