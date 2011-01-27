package intellijcoder.ipc;

import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;
import intellijcoder.os.Network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client, running in the same process with Arena application. Sends Arena application events to the IDE plugin
 *
 * @author Konstantin Fadeyev
 *         16.01.11
 */
public class IntelliJCoderClient implements WorkspaceManager {
    public static final String SERVER_COMMUNICATION_ERROR_MESSAGE = "I/O error occured. Try to reopen the editor.";
    public static final String FAILED_TO_CONNECT_ERROR_MESSAGE = "Failed to connect to IntelliJCoder.";
    private Network network;
    private int port;

    public IntelliJCoderClient(Network network, int port) {
        this.network = network;
        this.port = port;
    }

    public void createProblemWorkspace(final Problem problem) throws IntelliJCoderException {
        process(new Transmission() {
            @Override
            public void sendRequest(ObjectOutputStream objectOutputStream) throws IOException {
                objectOutputStream.writeObject(Request.CREATE_PROBLEM_WORKSPACE);
                objectOutputStream.writeObject(problem);
            }
        });
    }

    public String getSolutionSource(final String className) throws IntelliJCoderException {
        final String[] result = new String[1];
        process(new Transmission(){
            @Override
            public void sendRequest(ObjectOutputStream objectOutputStream) throws IOException {
                objectOutputStream.writeObject(Request.GET_SOLUTION_SOURCE);
                objectOutputStream.writeObject(className);
            }

            @Override
            public void readResponse(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
                result[0] = (String)objectInputStream.readObject();
            }
        });
        return result[0];
    }

    private void process(Transmission transmission) throws IntelliJCoderException {
        Socket socket = openSocket();
        try {
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                transmission.sendRequest(objectOutputStream);
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Response response = (Response)objectInputStream.readObject();
                if(response == Response.OK) {
                    transmission.readResponse(objectInputStream);
                } else {
                    throw (IntelliJCoderException)objectInputStream.readObject();
                }
            } finally {
                socket.close();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new IntelliJCoderException(SERVER_COMMUNICATION_ERROR_MESSAGE, e);
        }
    }

    private Socket openSocket() throws IntelliJCoderException {
        try {
            return network.getLocalhostSocket(port);
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_CONNECT_ERROR_MESSAGE, e);
        }
    }

    private abstract class Transmission {
        public abstract void sendRequest(ObjectOutputStream objectOutputStream) throws IOException;

        public void readResponse(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        }
    }
}
