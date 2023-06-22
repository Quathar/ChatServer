package com.quathar.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <h1>ChatClient</h1>
 * <br>
 * This is a chat client that connects to a chat server and creates 2 threads to:
 * <ul>
 *     <li>Write from the standard input</li>
 *     <li>Read from the chat server</li>
 * </ul>
 *
 * @since 2022-11-15
 * @version 3.0
 * @author Q
 */
public class ChatClient {

    // <<-CONSTANTS->>
    private static final int MIN_PORT_NUMBER = 1;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final String ERROR = "E R R O R: ";
    private static final String SYSTEM = "S Y S T E M: ";

    // <<-FIELDS->>
    /**
     * Socket
     */
    private Socket _socket;
    /**
     * Socket Output
     */
    private PrintWriter _socketOut;
    /**
     * Socket Input
     */
    private BufferedReader _socketIn;
    /**
     * Standard Input (keyboard)
     */
    private BufferedReader _stdIn;

    // <<-CONSTRUCTOR->>
    /**
     * Constructs a new ChatClient object that connects to the specified server
     *
     * @param inetAddress the IP address of the server to connect to
     * @param portNumber the port number of the server to connect to
     */
    public ChatClient(InetAddress inetAddress, int portNumber) {
        try {
            _socket    = new Socket(inetAddress, portNumber);
            _socketOut = new PrintWriter(_socket.getOutputStream(), true);
            _socketIn  = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            _stdIn     = new BufferedReader(new InputStreamReader(System.in));
            start();
        } catch(IOException e) {
            System.err.println(ERROR + "ChatClient() -> IOException");
        } catch (InterruptedException e) {
            System.err.println(ERROR + "ChatClient() -> InterruptedException");
        }
    }

    // <<-METHODS->>
    /**
     * Starts the ChatClient by creating and starting two separate threads:<br>
     * <ul>
     *     <li><b>Standard Thread:</b> Reads user input from standard input (console) and sends it to the server.</li>
     *     <li><b>Network Thread:</b> Receives messages from the server and displays them to the user.</li>
     * @throws IOException if an I/O error occurs while reading or writing data
     * @throws InterruptedException if any thread interrupts the current thread
     */
    private void start() throws IOException, InterruptedException {
        // Standard Thread
        // This thread receives the standard input (user) and sends the message to the server
        Thread stdThread = new Thread(() -> {
            try {
                String userInput;
                while (!"/exit".equalsIgnoreCase(userInput = _stdIn.readLine()))
                    _socketOut.println(userInput);
            } catch (IOException e) {
                System.err.println(ERROR + "stdThread()");
            }
        });

        // Network Thread
        // This thread receives messages from the server and displays them to the user
        Thread nwkThread = new Thread(() -> {
            try {
                String socketInput;
                while ((socketInput = _socketIn.readLine()) != null)
                    System.out.println(socketInput);
            } catch (IOException e) {
                System.out.println(SYSTEM + "bye!");
            }
        });

        stdThread.start();
        nwkThread.start();

        // The Client waits here until the client close the connection (which leads to thread death)
        stdThread.join();

        // When the socket is closed it triggers the 'nwkThread' exception (IOException)
        // When closing the 'socket', 'socketOut' (PrintWriter) y 'socketIn' (BufferedReader)
        _socket.close();
        // Then finally we close the 'stdIn' (BufferedReader)
        _stdIn.close();
    }

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // To start the client is necessary 2 arguments
        // the <IP> and the <Port> of the server you want to connect to
        if (args.length > 2) {
            System.err.println("Usage: java EchoClient <ip address> <port number>");
            System.exit(1);
        }

        // Checks for 'IP Address'
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(args[0]);
            // If the application doesn't run, comment this 'if'
            if (!inetAddress.isReachable(10000)) {
                System.err.printf("Usage: can't reach <ip address> %s%n", args[0]);
                System.exit(1);
            }
        } catch (UnknownHostException e) {
            System.err.printf("Usage: <ip address> %s is invalid%n", args[0]);
            System.exit(1);
        }

        // Checks for 'port number'
        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(args[1]);
            if (portNumber < MIN_PORT_NUMBER || portNumber > MAX_PORT_NUMBER) {
                System.err.printf("Usage: <port number> must be an integer value between %d and %d%n", MIN_PORT_NUMBER, MAX_PORT_NUMBER);
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Usage: <port number> must be an integer value");
            System.exit(1);
        }

        // Init ChatClient
        new ChatClient(inetAddress, portNumber);
    }

}