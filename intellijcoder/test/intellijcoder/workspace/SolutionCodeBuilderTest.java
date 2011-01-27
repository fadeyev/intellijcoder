package intellijcoder.workspace;

import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;
import intellijcoder.model.ProblemMaker;
import intellijcoder.workspace.CodeBuilder;
import intellijcoder.workspace.SolutionCodeBuilder;
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
public class SolutionCodeBuilderTest {
    private CodeBuilder codeBuilder = new SolutionCodeBuilder();

    @Test
    public void testBasicClassTemplateStructure() throws Exception {
        final Problem problem = make(a(ProblemMaker.Problem, with(className, "Lottery"),
                with(returnType, "String[]"), with(methodName, "sortByOdds"),
                with(paramTypes, new String[]{"String[]"}), with(paramNames, new String[] {"rules"})));
        final String[] expectedTemplate = {"public class Lottery","{","public String[]","sortByOdds","(","String[]","rules",")","{","}","}"};
        verifyGeneratedClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void methodReturnsFalseForPrimitiveBooleanReturnType() throws Exception {
        final Problem problem = make(a(Problem, with(returnType, "boolean")));
        final String[] expectedTemplate = {"boolean","{","return false", "}"};
        verifyGeneratedClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void methodReturnsZeroForPrimitiveNumberReturnType() throws Exception {
        final Problem problem = make(a(Problem, with(returnType, "double")));
        final String[] expectedTemplate = {"double","{","return 0", "}"};
        verifyGeneratedClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void methodReturnsNullForObjectReturnType() throws Exception {
        final Problem problem = make(a(Problem, with(returnType, "String[]")));
        final String[] expectedTemplate = {"String[]","{","return null", "}"};
        verifyGeneratedClassTemplate(problem, expectedTemplate);
    }

    @Test
    public void repeatingCallsToBuildGenerateTheSameCode() throws Exception {
        Problem problem = make(a(Problem));
        String code1 = codeBuilder.build(problem);
        String code2 = codeBuilder.build(problem);
        assertEquals("generated code", code1, code2);
    }

    private void verifyGeneratedClassTemplate(Problem problem, final String[] expectedTemplate) throws IntelliJCoderException {
        assertThat("class template", codeBuilder.build(problem), hasTemplate(expectedTemplate));
    }
}
