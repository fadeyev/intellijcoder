package intellijcoder.ipc;

import intellijcoder.workspace.WorkspaceManager;
import intellijcoder.main.IntelliJCoderException;
import intellijcoder.model.Problem;
import intellijcoder.os.Network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server, listening for events from TopCoder Competition Arena
 *
 * Since Arena and IDEA plugin run in different processes,
 * there is a client and a server for their inter-process communication.
 *
 * @author Konstantin Fadeyev
 *         16.01.11
 */
public class IntelliJCoderServer {
    public static final String FAILED_TO_START_SERVER_MESSAGE = "Failed to start a server for listening for TopCoder events";
    private WorkspaceManager workspaceManager;
    private Network network;
    private volatile boolean stopped;

    public IntelliJCoderServer(WorkspaceManager workspaceManager, Network network) {
        this.workspaceManager = workspaceManager;
        this.network = network;
    }

    public int start() throws IntelliJCoderException {
        final ServerSocket serverSocket;
        try {
            serverSocket = network.bindServerSocket(0);
        } catch (IOException e) {
            throw new IntelliJCoderException(FAILED_TO_START_SERVER_MESSAGE, e);
        }
        new Thread(new ServerCycle(serverSocket)).start();
        return serverSocket.getLocalPort();
    }

    public void stop() {
        stopped = true;
    }

    private class ServerCycle implements Runnable {
        private final ServerSocket serverSocket;

        public ServerCycle(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            try {
                while(!stopped) {
                    try {
                        Socket socket = serverSocket.accept();
                        processClient(socket);
                    } catch (IOException e) {
                        //Actually I don't know what to do with this exception. My guess is if an IO exception occured on server
                        //then a corresponding IO exception occured on client. So we'll catch and process it on client
                    }
                }
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    //close silently
                }
            }
        }

        private void processClient(Socket socket) throws IOException {
            try {
                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                try {
                    Request request = (Request)objectInputStream.readObject();
                    processRequest(request, objectInputStream, objectOutputStream);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IntelliJCoderException e) {
                    objectOutputStream.writeObject(Response.ERROR);
                    objectOutputStream.writeObject(e);
                }
            } finally {
                socket.close();
            }
        }

        private void processRequest(Request request, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException, IntelliJCoderException {
            switch (request) {
                case CREATE_PROBLEM_WORKSPACE: {
                    Problem problem = (Problem) objectInputStream.readObject();
                    workspaceManager.createProblemWorkspace(problem);
                    objectOutputStream.writeObject(Response.OK);
                }
                break;
                case GET_SOLUTION_SOURCE: {
                    String className = (String)objectInputStream.readObject();
                    String source = workspaceManager.getSolutionSource(className);
                    objectOutputStream.writeObject(Response.OK);
                    objectOutputStream.writeObject(source);
                }
                break;
                default:
                    throw new RuntimeException("Unknown request type");
            }
        }
    }
}
