package com.quathar.chatserver;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
public class ChatClient extends Application {

    // <<-CONSTANTS->>
    /**
     * Minimum valid port number.
     */
    private static final int MIN_PORT_NUMBER = 1;
    /**
     * Maximum valid port number.
     */
    private static final int MAX_PORT_NUMBER = 65535;
    /**
     * Error prefix for error messages.
     */
    private static final String ERROR = "E R R O R: ";
    /**
     * System prefix for error messages.
     */
    private static final String SYSTEM = "S Y S T E M: ";
    /**
     * The frame title.
     */
    private static final String FRAME_TITLE = "Chat Server";

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

    // <<-METHODS->>
    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        // Checks for 'IP Address'
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(args[0]);
            // If the application doesn't run, comment this 'if'
            // If the application doesn't run, comment this 'if'
            // If the application doesn't run, comment this 'if'
            if (!inetAddress.isReachable(10000)) {
                System.err.printf("Usage: can't reach <ip address> %s%n", args[0]);
                System.exit(1);
            }
        } catch (UnknownHostException e) {
            System.err.printf("Usage: <ip address> %s is invalid%n", args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("<ip address> %s is not reachable%n", args[0]);
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

        try {
            _socket    = new Socket(inetAddress, portNumber);
            _socketOut = new PrintWriter(_socket.getOutputStream(), true);
            _socketIn  = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        } catch(IOException e) {
            System.err.println(ERROR + "ChatClient() -> IOException");
        }
    }

    private void sendMessage(TextField input, TextArea display) {
        String message = input.getText();
        display.appendText(message + System.lineSeparator());
        _socketOut.println(message);
        input.clear();
    }

    @Override
    public void start(Stage primaryStage) {
        // Here we build the interface
        primaryStage.setTitle(FRAME_TITLE);

        TextArea panelTA = new TextArea();
        panelTA.setEditable(false);
        TextField inputTF = new TextField();
        inputTF.setOnAction(event -> sendMessage(inputTF, panelTA));
        Button btnSend = new Button("Send");
        btnSend.setOnAction(event -> sendMessage(inputTF, panelTA));

        // Layouts
        VBox verticalLayout = new VBox(panelTA);
        verticalLayout.setPadding(new Insets(10));

        HBox horizontalLayout = new HBox(inputTF, btnSend);
        horizontalLayout.setSpacing(10);
        horizontalLayout.setPadding(new Insets(10));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(verticalLayout);
        borderPane.setBottom(horizontalLayout);

        // Set scene and show the stage
        Scene scene = new Scene(borderPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Network Thread
        // This thread receives messages from the server and displays them to the user
        new Thread(() -> {
            try {
                String socketInput;
                while ((socketInput = _socketIn.readLine()) != null)
                    panelTA.appendText(socketInput + System.lineSeparator());
            } catch (IOException e) {
                panelTA.appendText(SYSTEM + "bye!");
            }
        }).start();
    }

    @Override
    public void stop() {
        try {
            // When the socket is closed it triggers the 'nwkThread' exception (IOException)
            // When closing the 'socket', 'socketOut' (PrintWriter) y 'socketIn' (BufferedReader)
            _socket.close();
        } catch (IOException ioE) {
            System.err.println(ERROR + "ChatClient() -> stop() -> IOException");
        }
    }

    /**
     * The main method of the EchoClient class.<br>
     * <br>
     * The first argument should be the IP address of the server<br>
     * to connect to, and the second argument should be the port number.
     *
     * @param args command-line arguments passed to the program.
     */
    public static void main(String[] args) {
        // To start the client is necessary 2 arguments
        // the <IP> and the <Port> of the server you want to connect to
        if (args.length != 2) {
            System.err.println("Usage: java EchoClient <ip address> <port number>");
            System.exit(1);
        }

        // Launch UI
        launch(args);
    }

}