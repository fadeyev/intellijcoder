package intellijcoder.workspace;

import intellijcoder.model.Problem;
import intellijcoder.model.TestCase;
import intellijcoder.workspace.TestCodeBuilder;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static intellijcoder.model.ProblemMaker.*;
import static intellijcoder.util.TestUtil.hasTemplate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Konstantin Fadeyev
 *         21.01.11
 */
public class TestCodeBuilderTest {
    private TestCodeBuilder testCodeBuilder = new TestCodeBuilder();

    @Test
    public void basicTestClassTemplateStructure() throws Exception {
        TestCase testCase = make(a(TestCase, with(input, new String[]{"\"00:30:00\""}), with(output, "99")));
        final Problem problem = make(a(Problem, with(className, "ExerciseMachine"),
                with(returnType, "int"), with(methodName, "getPercentages"),
                with(paramTypes, new String[]{"String"}), with(paramNames, new String[] {"time"}),
                with(testCases, new TestCase[]{testCase})));
        final String[] expectedTemplate = {
                "import org.junit.Test;",
                "import static org.junit.Assert.*;",
                "public class ExerciseMachineTest",
                    "{", "@Test", "public void test0()","{",
                        "String", "time", "=", "\"00:30:00\"", ";",
                        "assertEquals(99, new ExerciseMachine().getPercentages(time));",
                    "}","}"};
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void twoTestCases() throws Exception {
        TestCase testCase1 = make(a(TestCase, with(input, new String[] {"input1"}), with(output, "output1")));
        TestCase testCase2 = make(a(TestCase, with(input, new String[] {"input2"}), with(output, "output2")));
        Problem problem = make(a(Problem,
                with(paramTypes, new String[] {someType()}), with(paramNames, new String[]{someName()}),
                with(testCases, new TestCase[]{testCase1, testCase2})));
        final String[] expectedTemplate = {
                "public void test0()", "{", "input1", "output1", "}",
                "public void test1()", "{", "input2", "output2", "}",
        };
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void usesAssertsArrayWhenReturnTypeIsArray() throws Exception {
        Problem problem = make(a(Problem, with(returnType, "String[]"),
                with(testCases, new TestCase[]{ make(a(TestCase)) })));
        final String[] expectedTemplate = {
                "public void test0()", "{", "assertArrayEquals(",")" , "}"
        };
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void usesInexactAssertWhenReturnTypeIsDouble() throws Exception {
        Problem problem = make(a(Problem, with(returnType, "double"),
                with(testCases, new TestCase[]{ make(a(TestCase)) })));
        final String[] expectedTemplate = {
                "public void test0()", "{", "assertEquals(", ", 1e-9)" , "}"
        };
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void usesInexactArrayAssertWhenReturnTypeIsArrayOfDoubles() throws Exception {
        Problem problem = make(a(Problem, with(returnType, "double[]"),
                with(testCases, new TestCase[]{ make(a(TestCase)) })));
        final String[] expectedTemplate = {
                "public void test0()", "{", "assertArrayEquals(", ", 1e-9)" , "}"
        };
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void usesArrayCreationSyntaxWhenParamTypeIsArray() throws Exception {
        TestCase testCase = make(a(TestCase, with(input, new String[] {"{1, 2}"}), with(output, someValue())));
        Problem problem = make(a(Problem,
                with(paramTypes, new String[] {"int[]"}), with(paramNames, new String[]{"intervals"}),
                with(testCases, new TestCase[]{ testCase })));
        final String[] expectedTemplate = {
                "public void test0()", "{", "intervals", "=", "new int[]", "{1, 2}" , "}"
        };
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void usesArrayCreationSyntaxOnOutputWhenReturnTypeIsArray() throws Exception {
        TestCase testCase = make(a(TestCase, with(input, new String[] {someValue()}), with(output, "{1, 2}")));
        Problem problem = make(a(Problem, with(returnType, "int[]"),
                with(testCases, new TestCase[]{ testCase })));
        final String[] expectedTemplate = {
                "public void test0()", "{", "assertArrayEquals(new int[]", "{1, 2}", ")" , "}"
        };
        verifyGeneratedTestClassTemplate(problem, expectedTemplate);
    }


    @Test
    public void repeatingCallsToBuildGenerateTheSameCode() throws Exception {
        Problem problem = make(a(Problem));
        String code1 = testCodeBuilder.build(problem);
        String code2 = testCodeBuilder.build(problem);
        assertEquals("generated code", code1, code2);
    }

    private void verifyGeneratedTestClassTemplate(Problem problem, final String[] expectedTemplate) {
        assertThat("test class template", testCodeBuilder.build(problem), hasTemplate(expectedTemplate));
    }

    private String someName() {
        return "someName";
    }

    private String someType() {
        return "some type";
    }

    private String someValue() {
        return "output1";
    }
}
