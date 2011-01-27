package intellijcoder.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Date: 14.01.11
 *
 * @author Konstantin Fadeyev
 */
public class TestCaseTest {
    @Test
    public void valueObject() {
        TestCase testCase = new TestCase(new String[]{"1"}, "1");
        TestCase testCaseCopy = new TestCase(new String[]{"1"}, "1");
        TestCase testCaseWithDifferentInput = new TestCase(new String[]{"2"}, "1");
        TestCase testCaseWithDifferentOutput = new TestCase(new String[]{"1"}, "2");

        assertTrue("test cases with the same fields should be equal", testCase.equals(testCaseCopy));
        assertTrue("test cases hash codes with the same fields should be equal", testCase.hashCode() == testCaseCopy.hashCode());
        assertFalse("test cases with different input should not be equal", testCase.equals(testCaseWithDifferentInput));
        assertFalse("test cases with different output should not be equal", testCase.equals(testCaseWithDifferentOutput));
    }
}
