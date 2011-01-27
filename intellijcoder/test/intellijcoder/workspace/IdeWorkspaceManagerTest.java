package intellijcoder.workspace;

import intellijcoder.idea.Ide;
import intellijcoder.model.Problem;
import intellijcoder.workspace.CodeBuilder;
import intellijcoder.workspace.IdeWorkspaceManager;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static intellijcoder.model.ProblemMaker.Problem;
import static intellijcoder.model.ProblemMaker.className;

/**
 * @author Konstantin Fadeyev
 *         15.01.11
 */
@RunWith(JMock.class)
public class IdeWorkspaceManagerTest {
    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private Ide ide = context.mock(Ide.class);
    private CodeBuilder solutionBuilder = context.mock(CodeBuilder.class, "solution");
    private CodeBuilder testBuilder = context.mock(CodeBuilder.class, "test");
    IdeWorkspaceManager workspaceManager = new IdeWorkspaceManager(ide, solutionBuilder, testBuilder);

    @Test
    public void projectModuleNamedAfterProblemClassName() throws Exception {
        final Problem problem = make(a(Problem, with(className, "Lottery")));
        context.checking(new Expectations(){{
            oneOf(ide).createModule(with(equal("Lottery")), with(any(String.class)), with(any(String.class)));
            ignoring(solutionBuilder);
            ignoring(testBuilder);
        }});
        workspaceManager.createProblemWorkspace(problem);
    }


    @Test
    public void getsSolutionSourceFromIde() throws Exception {
        final Problem problem = make(a(Problem));
        context.checking(new Expectations(){{
            atLeast(1).of(ide).getClassSource("Lottery");
            ignoring(ide).createModule(with(any(String.class)), with(any(String.class)), with(any(String.class)));
            ignoring(solutionBuilder);
            ignoring(testBuilder);
        }});
        workspaceManager.createProblemWorkspace(problem);
        workspaceManager.getSolutionSource("Lottery");
    }

    @Test
    public void usesCodeBuilderToGetSolutionClassTemplate() throws Exception {
        final Problem problem = make(a(Problem));
        context.checking(new Expectations(){{
            allowing(solutionBuilder).build(problem); will(returnValue("class template"));
            oneOf(ide).createModule(with(any(String.class)), with(equal("class template")), with(any(String.class)));
            ignoring(testBuilder);
        }});
        workspaceManager.createProblemWorkspace(problem);
    }

    @Test
    public void usesCodeBuilderToGetTestClassTemplate() throws Exception {
        final Problem problem = make(a(Problem));
        context.checking(new Expectations(){{
            allowing(testBuilder).build(problem); will(returnValue("test class template"));
            oneOf(ide).createModule(with(any(String.class)), with(any(String.class)), with(equal("test class template")));
            ignoring(solutionBuilder);
        }});
        workspaceManager.createProblemWorkspace(problem);
    }
}
