package intellijcoder.arena;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.JavaLanguage;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.Renderer;
import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.ipc.IntelliJCoderClient;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;
import intellijcoder.model.TestCase;
import intellijcoder.os.Network;

import javax.swing.*;

import static intellijcoder.arena.ArenaProcessLauncher.INTELLIJCODER_PORT_PROPERTY;


/**
 * TopCoder editor plugin implementation.
 *
 * @author Konstantin Fadeyev
 *         10.01.11
 */
public class IntelliJCoderArenaPlugin implements ArenaPlugin {
    public static final String PORT_PROPERTY_NOT_SPECIFIED_MESSAGE = INTELLIJCODER_PORT_PROPERTY + " property not specified. "
                                            + "Check if you started Competition Arena from inside of IntelliJ IDEA.";
    public static final String WORKSPACE_CREATED_MESSAGE = "Look for generated solution stub in your IntelliJ IDEA";
    public static final String LANGUAGE_NOT_SUPPORTED_MESSAGE = "This language is not supported";

    private WorkspaceManager workspaceManager;
    private MessagePanel messagePanel;
    private Problem currentProblem;

    public IntelliJCoderArenaPlugin() {
        this(null, null);
    }

    public IntelliJCoderArenaPlugin(WorkspaceManager workspaceManager, MessagePanel messagePanel) {
        this.workspaceManager = workspaceManager;
        this.messagePanel = messagePanel;
    }

    public JPanel getEditorPanel() {
        return messagePanel;
    }

    public void startUsing() {
        if(messagePanel == null) {
            this.messagePanel = new MessagePanel();
        }
        if(!workspaceManagerInitialized()) {
            if(portPropertyDefined()) {
                this.workspaceManager = new IntelliJCoderClient(new Network(), getPortFromProperty());
            } else {
                messagePanel.showErrorMessage(PORT_PROPERTY_NOT_SPECIFIED_MESSAGE);
            }
        }
    }

    public String getSource() {
        if(!workspaceManagerInitialized() || !isProblemOpened()) {
            return "";
        }

        try {
            return workspaceManager.getSolutionSource(currentProblem.getClassName());
        } catch (IntelliJCoderException e) {
            messagePanel.showErrorMessage(e.getMessage());
        }
        return "";
    }

    public void setSource(String source) {
        // do nothing - not interested in existing source
    }

    public void setProblemComponent(ProblemComponentModel componentModel, Language language, Renderer renderer) {
        if(!workspaceManagerInitialized()) {
            return;
        }
        if(!(language instanceof JavaLanguage)) {
            messagePanel.showErrorMessage(LANGUAGE_NOT_SUPPORTED_MESSAGE);
            clearCurrentProblem();
            return;
        }
        if(theSameProblemWasJustOpened(componentModel)) {
            return;
        }
        try {
            Problem problem = extractProblem(componentModel);
            workspaceManager.createProblemWorkspace(problem);
            setCurrentProblem(problem);
            messagePanel.showInfoMessage(WORKSPACE_CREATED_MESSAGE);
        } catch (IntelliJCoderException e) {
            messagePanel.showErrorMessage(e.getMessage());
        }
    }

    private boolean theSameProblemWasJustOpened(ProblemComponentModel componentModel) {
        return isProblemOpened() && currentProblem.getClassName().equals(componentModel.getClassName());
    }

    private boolean workspaceManagerInitialized() {
        return workspaceManager != null;
    }

    private boolean isProblemOpened() {
        return currentProblem != null;
    }

    private void clearCurrentProblem() {
        currentProblem = null;
    }

    private void setCurrentProblem(Problem problem) {
        currentProblem = problem;
    }

    private Problem extractProblem(ProblemComponentModel componentModel) {
        String className = componentModel.getClassName();
        String returnType = componentModel.getReturnType().getDescriptor(JavaLanguage.JAVA_LANGUAGE);
        String methodName = componentModel.getMethodName();
        String[] paramTypes = extractTypes(componentModel.getParamTypes());
        String[] paramNames = componentModel.getParamNames();
        TestCase[] testCases = extractTestCases(componentModel.getTestCases());
        return new Problem(className, returnType, methodName, paramTypes, paramNames, testCases);
    }

    private TestCase[] extractTestCases(com.topcoder.shared.problem.TestCase[] testCases) {
        TestCase[] result = new TestCase[testCases.length];
        for (int i = 0; i < testCases.length; i++) {
            result[i] = new TestCase(testCases[i].getInput(), testCases[i].getOutput());
        }
        return result;
    }

    private String[] extractTypes(DataType[] dataTypes) {
        String[] result = new String[dataTypes.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = dataTypes[i].getDescriptor(JavaLanguage.JAVA_LANGUAGE);
        }
        return result;
    }


    private boolean portPropertyDefined() {
        return System.getProperty(INTELLIJCODER_PORT_PROPERTY) != null;
    }

    private int getPortFromProperty() throws IllegalStateException {
        return Integer.parseInt(System.getProperty(INTELLIJCODER_PORT_PROPERTY));
    }
}