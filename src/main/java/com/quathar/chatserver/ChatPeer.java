package com.quathar.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * <h1>ChatPeer</h1>
 * <br>
 * This is a chat peer, this thread is created by the chat server to comunicate whit the others users.
 *
 * @since 2022-11-15
 * @version 3.0
 * @author Q
 */
public class ChatPeer extends Thread {

    // <<-CONSTANTS->>
    private static final String SYSTEM = "S Y S T E M: ";
    private static final String ERROR = "E R R O R: ";

    // <<-FIELDS->>
    private Socket _socket;
    private ChatServer _chatServer;
    private PrintWriter _socketOut;
    private BufferedReader _socketIn;
    private String _nickname;
    private boolean _available;

    // <<-CONSTRUCTOR->>
    public ChatPeer(ChatServer chatServer, Socket socket) {
        try {
            _chatServer = chatServer;
            _socket     = socket;
            _socketOut  = new PrintWriter(_socket.getOutputStream(), true);
            _socketIn   = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            checkNickname();
            _available  = true;
        } catch (IOException e) {
            System.err.println("ERROR: ChatPeer --> constructor");
        }
    }

    // <<-METHOD->>
    private String statusMessage(int status) {
        StringBuilder sb = new StringBuilder(Prompt.ANSI_RED.getCode());

        return switch (status) {
            case 0  -> sb.replace(0, 10, Prompt.ANSI_RESET.getCode())
                         .append("=".repeat(50))
                         .toString();
            case 1  -> sb.append(ERROR + "Nickname is blank").toString();
            case 2  -> sb.append(ERROR + "Nickname contains spaces").toString();
            case 3  -> sb.append(ERROR + "Nickname already exists").toString();
            default -> sb.append(ERROR + "Unexpected status").toString();
        };
    }

    private void checkNickname() throws IOException {
        int status = -1;
        while (status != 0) {
            _socketOut.printf("%sEnter your nickname:%n", Prompt.ANSI_CYAN.getCode());
            _nickname = _socketIn.readLine();
            status = _chatServer.nicknameInspection(_nickname);
            _socketOut.println(statusMessage(status));
        }
        _socketOut.println(SYSTEM + "Welcome, you are connected to the server");
        System.out.printf("Client << %s >> has entered the chat%n", _nickname);
    }

    private void changeNickname() throws IOException {
        _available = false;
        _socketOut.printf("%sChanging nickname...%n", Prompt.ANSI_YELLOW.getCode());

        int status = -1;
        StringBuilder nickname = new StringBuilder();
        while (status != 0) {
            _socketOut.printf("%sEnter your nickname: %n", Prompt.ANSI_CYAN.getCode());
            nickname.replace(0, nickname.length(), _socketIn.readLine());
            if (_nickname.contentEquals(nickname)) break;
            status = _chatServer.nicknameInspection(nickname.toString());
            _socketOut.println(statusMessage(status));
        }

        if (!_nickname.contentEquals(nickname)) {
            _chatServer.changeNickname(nickname.toString(), _nickname);
            _nickname = nickname.toString();
            _socketOut.printf("%sS Y S T E M: The nickname was successfully changed ;)%n", Prompt.ANSI_YELLOW.getCode());
        } else _socketOut.printf("%sS Y S T E M: The nick wasn't changed%n", Prompt.ANSI_YELLOW.getCode());

        _available = true;
    }

    public void send(String msg) {
        if (_available)
            _socketOut.println(msg);
    }

    // <<-OVERRIDE->>
    @Override
    public void run() {
        try {
            while (true) {
                String stdInput = _socketIn.readLine();
                if (stdInput == null) break;
                else if (stdInput.startsWith("@") && stdInput.contains(" ")) // To send private messages
                    _chatServer.privateMessage(
                            _nickname,
                            stdInput.substring(1, stdInput.indexOf(" ")),
                            String.format("[PM] %s: %s",
                                    _nickname,
                                    stdInput.substring(stdInput.indexOf(" ") + 1)
                            )
                    );
                else if (stdInput.startsWith("/")) // To use commands
                    if (stdInput.equals("/nick"))
                        changeNickname();
                    else _chatServer.serverMessage(_nickname, stdInput);
                else _chatServer.broadcast( // To send a message to all the users in the chat server
                        _nickname,
                        String.format("%s: %s", _nickname, stdInput));
            }
        } catch (IOException e) {
            System.err.println(ERROR + "ChatPeer --> run()");
        } finally {
            try {
                System.out.printf("Client << %s >> has left the chat%n", _nickname);
                _chatServer.remove(_nickname);
                _socket.close();
            } catch (IOException e) {
                System.err.println(ERROR + "ChatPeer --> _socket.close()");
            }
        }
    }

    // <<-GETTER->>
    public String getNickname() {
        return _nickname;
    }

}