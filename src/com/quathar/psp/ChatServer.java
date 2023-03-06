package com.quathar.psp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.HashMap;

/**
 * <h1>ChatServer</h1>
 *
 * This is a chat server that manages the users that are
 *
 * @since 2022-11-15
 * @author Q
 * @version 3.0
 */
public class ChatServer {

    // <<-CONSTANTS->>
    private static final int MIN_PORT_NUMBER = 1;
    private static final int MAX_PORT_NUMBER = 65535;

    // <<-FIELDS->>
    private final ServerSocket _server;
    private final Map<String, ChatPeer> _chatPeers;

    // <<-CONSTRUCTOR->>
    public ChatServer(int portNumber) {
        try {
            _server = new ServerSocket(portNumber);
            _chatPeers = new HashMap<>();
            start();
            _server.close(); // De momento lo dejamos así, pero hay que cerrarlo bien
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void start() throws IOException {
        try {
            // The server only accept connections again and again
            while (true) {
                Socket socket = _server.accept();
                new Thread(() -> {
                    ChatPeer chatPeer = new ChatPeer(this, socket);
                    synchronized (this) {
                        _chatPeers.put(chatPeer.getNickname(), chatPeer);
                    }
                    chatPeer.start();
                    System.out.printf("%sS E R V E R --> Total clients: %d%s%n", Prompt.ANSI_GREEN, _chatPeers.size(), Prompt.ANSI_RESET);
                }).start();
            }
        } catch (SocketException e) {
            System.err.println("E R R O R: The connection can't be made");
        }
    }

    // <<-METHODS->>
    public boolean nicknameExists(String nickname) {
        // Case-sensitive
        return _chatPeers.keySet()
                .stream()
                .anyMatch(nick -> nick.equalsIgnoreCase(nickname));
        // Works but it doesn't detect between upper and lower case
//        return _chatPeers.containsKey(nickname);
    }

    public synchronized int nicknameInspection(String nickname) {
        if (nickname.isBlank()) return 1;
        if (nickname.contains(" ")) return 2;
        if (nicknameExists(nickname)) return 3;
        return 0;
    }

    public void privateMessage(String srcNickname, String dstNickname, String msg) {
        StringBuilder sb = new StringBuilder(Prompt.ANSI_PURPLE);

        ChatPeer chatPeer = _chatPeers.get(dstNickname);
        if (chatPeer != null)
            chatPeer.send(sb.append(msg).toString());
        else _chatPeers
                .get(srcNickname)
                .send(sb
                        .replace(0, sb.length(), Prompt.ANSI_RED)
                        .append("E R R O R: That user doesn't exist").toString()
                );
    }

    public synchronized void changeNickname(String newNickname, String oldNickname) {
        _chatPeers.put(newNickname, _chatPeers.remove(oldNickname));
    }

    private String commandList(String command, String nickname) {
        StringBuilder sb = new StringBuilder(Prompt.ANSI_YELLOW);
        switch (command) {
            case "/help" -> sb.append("Commands:").append(System.getProperty("line.separator"))
                    .append("/help   -> Shows a list of the server commands"    ).append(System.getProperty("line.separator"))
                    .append("/ip     -> Displays the server IP address"         ).append(System.getProperty("line.separator"))
                    .append("/me     -> Shows your nickname"                    ).append(System.getProperty("line.separator"))
                    .append("/nick   -> Allows to change the name"              ).append(System.getProperty("line.separator"))
                    .append("/people -> Users currently connected to the server").append(System.getProperty("line.separator"))
                    .append("/port   -> Displays the server's Port"             ).append(System.getProperty("line.separator"))
                    .append("/exit   -> Exits the server");
            case "/ip" -> sb.append(_server.getInetAddress());
            case "/me" -> sb.append("You are ").append(nickname);
            case "/people" -> {
                sb.append("People connected:").append(System.getProperty("line.separator"));
                _chatPeers.keySet()
                        .forEach(nick -> sb.append("> ").append(nick).append(System.getProperty("line.separator")));
            }
            case "/port" -> sb.append(_server.getLocalPort());
            default -> sb.append("That command doesn't exist, try '/help' :(((");
        }
        return sb.append(Prompt.ANSI_RESET).toString();
    }

    public void serverMessage(String nickname, String command) {
        _chatPeers.get(nickname).send(commandList(command, nickname));
    }

    public void broadcast(String srcNickname, String msg) {
        _chatPeers.keySet()
                .stream()
                .filter(nickname -> !nickname.equals(srcNickname))
                .forEach(nickname -> _chatPeers.get(nickname).send(Prompt.ANSI_CYAN + msg + Prompt.ANSI_RESET));
    }

    public synchronized void remove(String nickname) {
        _chatPeers.remove(nickname);
        System.out.printf("%sS E R V E R --> Total clients: %d%s%n", Prompt.ANSI_RED, _chatPeers.size(), Prompt.ANSI_RESET);
        if (_chatPeers.size() == 1) {
            _chatPeers
                    .get(_chatPeers.keySet().iterator().next())
                    .send(Prompt.ANSI_YELLOW + "Now you are alone on the server");
        } else if (_chatPeers.size() == 0)
            // Remove this 'else if' if you dont want the server to close when there are no ChatPeers in it
            System.exit(0);
    }

    // <<-MAIN METHOD->>
    public static void main(String[] args) {
        // Si el servidor no estuviera alojado en nuestra máquina habría que cambiar este 'if' (ip address, port number)
        if (args.length != 1) {
            System.err.println("Usage: java Client <ip address> <port number>");
            System.exit(1);
        }

        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(args[0]);
            if (portNumber < MIN_PORT_NUMBER || portNumber > MAX_PORT_NUMBER) {
                System.err.printf("Usage: <port number> must be an integer value between %d and %d%n", MIN_PORT_NUMBER, MAX_PORT_NUMBER);
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Usage: <port number> must be an integer value");
            System.exit(1);
        }

        // Init ChatServer
        new ChatServer(portNumber);
    }

}
