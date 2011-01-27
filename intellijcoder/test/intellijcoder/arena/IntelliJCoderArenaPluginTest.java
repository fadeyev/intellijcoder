package intellijcoder.arena;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.CPPLanguage;
import com.topcoder.shared.language.JavaLanguage;
import com.topcoder.shared.problem.DataType;
import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;
import intellijcoder.model.TestCase;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static intellijcoder.arena.ProblemComponentModelMaker.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Konstantin Fadeyev
 *         13.01.11
 */
@RunWith(JMock.class)
public class IntelliJCoderArenaPluginTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceManager workspaceManager = context.mock(WorkspaceManager.class);
    private MessagePanel messagePanel = context.mock(MessagePanel.class);
    private ArenaPlugin arenaPlugin = new IntelliJCoderArenaPlugin(workspaceManager, messagePanel);


    @Test
    public void extractsClassNameFromProblemModel() throws IntelliJCoderException {
        ProblemComponentModel problemComponentModel = make(a(ProblemComponentModel, with(className, "BinaryCode")));
        context.checking(new Expectations(){{
            oneOf(workspaceManager).createProblemWorkspace(with(hasClassName("BinaryCode")));
            ignoring(messagePanel);
        }});
        arenaPlugin.setProblemComponent(problemComponentModel, JavaLanguage.JAVA_LANGUAGE, null);
    }

    @Test
    public void extractsMethodSignatureFromProblemModel() throws IntelliJCoderException {
        ProblemComponentModel problemComponentModel = make(a(ProblemComponentModel,
                with(returnType, dataType("int[]")), with(methodName, "decode"),
                with(paramTypes, new DataType[]{dataType("String[]")}), with(paramNames, new String[]{"a"})));
        context.checking(new Expectations(){{
            oneOf(workspaceManager).createProblemWorkspace(with(hasMethodSignature("int[]", "decode", "String[]", "a")));
            ignoring(messagePanel);
        }});
        arenaPlugin.setProblemComponent(problemComponentModel, JavaLanguage.JAVA_LANGUAGE, null);
    }

    @Test
    public void extractsTestCaseFromProblemModel() throws IntelliJCoderException {
        final String[] testInput = {"{\"-10 0 10 0\",\"0 -10 0 10\"}", "1"};
        final String testOutput = "1";
        final ProblemComponentModel problemComponentModel =
                make(a(ProblemComponentModel, with(testCase, testCase(testInput, testOutput))));
        context.checking(new Expectations() {{
            oneOf(workspaceManager).createProblemWorkspace(with(hasTestCase(testInput, testOutput)));
            ignoring(messagePanel);
        }});
        arenaPlugin.setProblemComponent(problemComponentModel, JavaLanguage.JAVA_LANGUAGE, null);
    }

    @Test
    public void showsErrorMessageIfErrorOccuredWhileCreatingProblemWorkspace() throws Exception {
        context.checking(new Expectations(){{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
                    will(throwException(new IntelliJCoderException("exception message", null)));
            oneOf(messagePanel).showErrorMessage("exception message");
        }});
        arenaPlugin.setProblemComponent(someProblemComponentModel(), JavaLanguage.JAVA_LANGUAGE, null);
    }

    @Test
    public void showsErrorMessageAndReturnsEmptyStringIfErrorOccuredWhileGettingSource() throws Exception {
        context.checking(new Expectations(){{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
            allowing(workspaceManager).getSolutionSource(with(any(String.class)));
                    will(throwException(new IntelliJCoderException("exception message", null)));
            oneOf(messagePanel).showInfoMessage(with(any(String.class)));
            oneOf(messagePanel).showErrorMessage("exception message");
        }});
        arenaPlugin.setProblemComponent(someProblemComponentModel(), JavaLanguage.JAVA_LANGUAGE, null);
        String source = arenaPlugin.getSource();
        assertEquals("solution source", "", source);
    }

    @Test
    public void showsErrorMessageOnStartUsingIfPortPropertyNotSpecified() throws Exception {
        IntelliJCoderArenaPlugin arenaPlugin = new IntelliJCoderArenaPlugin(null, messagePanel);
        context.checking(new Expectations(){{
            oneOf(messagePanel).showErrorMessage(IntelliJCoderArenaPlugin.PORT_PROPERTY_NOT_SPECIFIED_MESSAGE);
        }});
        arenaPlugin.startUsing();
    }

    @Test
    public void pluginMethodsBehaveSilentlyIfPortPropertyNotSpecified() throws Exception {
        IntelliJCoderArenaPlugin arenaPlugin = new IntelliJCoderArenaPlugin();
        arenaPlugin.startUsing();

        // shouldn't throw exceptions
        arenaPlugin.setProblemComponent(null, null, null);
        String source = arenaPlugin.getSource();
        assertEquals("solution source", "", source);
    }

    @Test
    public void showsOkMessageIfProblemWorkspaceSucessfullyCreated() throws Exception {
        context.checking(new Expectations() {{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class))); //no exception - all ok
            oneOf(messagePanel).showInfoMessage(IntelliJCoderArenaPlugin.WORKSPACE_CREATED_MESSAGE);
        }});
        arenaPlugin.setProblemComponent(someProblemComponentModel(), JavaLanguage.JAVA_LANGUAGE, null);
    }
    @Test
    public void messagePanelServersAsAnEditorPanel() throws Exception {
        assertSame(arenaPlugin.getEditorPanel(), messagePanel);
    }

    @Test
    public void getsSourceFromTheWorkspaceManager() throws Exception {
        context.checking(new Expectations(){{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
            allowing(workspaceManager).getSolutionSource(with(any(String.class))); will(returnValue("class Lottery{}"));
            ignoring(messagePanel);
        }});
        arenaPlugin.setProblemComponent(someProblemComponentModel(), JavaLanguage.JAVA_LANGUAGE, null);
        assertEquals("solution class source", "class Lottery{}", arenaPlugin.getSource());
    }

    @Test
    public void alwaysGetsSourceForTheLastOpenedProblem() throws Exception {
        final ProblemComponentModel problemComponentModel1 = make(a(ProblemComponentModel, with(className, "class1")));
        final ProblemComponentModel problemComponentModel2 = make(a(ProblemComponentModel, with(className, "class2")));
        context.checking(new Expectations(){{
            allowing(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
            oneOf(workspaceManager).getSolutionSource("class1");
            oneOf(workspaceManager).getSolutionSource("class2");
            ignoring(messagePanel);
        }});
        arenaPlugin.setProblemComponent(problemComponentModel1, JavaLanguage.JAVA_LANGUAGE, null);
        arenaPlugin.getSource();
        arenaPlugin.setProblemComponent(problemComponentModel2, JavaLanguage.JAVA_LANGUAGE, null);
        arenaPlugin.getSource();
    }

    @Test
    public void showsErrorMessageIfLanguageIsNotJava() throws Exception {
        context.checking(new Expectations(){{
            oneOf(messagePanel).showErrorMessage(IntelliJCoderArenaPlugin.LANGUAGE_NOT_SUPPORTED_MESSAGE);
        }});
        arenaPlugin.setProblemComponent(someProblemComponentModel(), CPPLanguage.CPP_LANGUAGE, null);
    }

    @Test
    public void pluginMethodsBehaveSilentlyIfNotSupportedLanguageSelected() throws Exception {
        context.checking(new Expectations(){{
            oneOf(messagePanel).showErrorMessage("This language is not supported");
            never(workspaceManager);
        }});
        arenaPlugin.setProblemComponent(someProblemComponentModel(), CPPLanguage.CPP_LANGUAGE, null);
        String source = arenaPlugin.getSource();
        assertEquals("solution source", "", source);
    }

    /*
       Strange thing, but TopCoder applet sometimes calls setProblemComponentModel() twice for the same problem and language,
       but for different renderers.
     */
    @Test
    public void onlyOneWorkspaceCreatedForTwoSubsequentCallsToSetProblemComponentForTheSameProblem() throws Exception {
        ProblemComponentModel componentModel = someProblemComponentModel();
        context.checking(new Expectations(){{
            oneOf(workspaceManager).createProblemWorkspace(with(any(Problem.class)));
            ignoring(messagePanel);
        }});
        arenaPlugin.setProblemComponent(componentModel, JavaLanguage.JAVA_LANGUAGE, null);
        arenaPlugin.setProblemComponent(componentModel, JavaLanguage.JAVA_LANGUAGE, null);
    }

    private ProblemComponentModel someProblemComponentModel() {
        return make(a(ProblemComponentModel));
    }

    private Matcher<Problem> hasMethodSignature(final String returnType, final String methodName, final String paramType, final String paramName) {
        return new BaseMatcher<Problem>() {
            public boolean matches(Object o) {
                Problem problem = (Problem) o;
                return problem.getReturnType().equals(returnType)
                        && problem.getMethodName().equals(methodName)
                        && problem.getParamTypes()[0].equals(paramType)
                        && problem.getParamNames()[0].equals(paramName);
            }

            public void describeTo(Description description) {
                description.appendText("returnType=" + returnType + ", methodName=" + methodName + ", parameter=" + paramType + " " + paramName);
            }
        };
    }

    private Matcher<Problem> hasTestCase(final String[] testInput, final String testOutput) {
        return new BaseMatcher<Problem>() {
            public boolean matches(Object o) {
                TestCase testCase = ((Problem) o).getTestCases()[0];
                return Arrays.equals(testCase.getInput(), testInput)
                        && testCase.getOutput().equals(testOutput);
            }

            public void describeTo(Description description) {
                description.appendText("testInput=" + Arrays.toString(testInput) + ", testOutput=" + testOutput);
            }
        };
    }


    private Matcher<Problem> hasClassName(final String className) {
        return new BaseMatcher<Problem>() {
            public boolean matches(Object o) {
                return ((Problem)o).getClassName().equals(className);
            }

            public void describeTo(Description description) {
                description.appendText("className=" + className);
            }
        };
    }
}
