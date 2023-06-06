/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chatting;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.codegen.CompilerConstants;

/**
 *
 * @author Bilal
 */
public class Server implements Runnable {

    private static GlobalRoom globalRoom;
    private ServerSocket serverSocket;
    private int port;
    private int clientID;

    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(this.port);
        this.globalRoom = new GlobalRoom();
    }

    @Override
    public void run() {
        while (!this.serverSocket.isClosed()) {
            try {
                System.out.println("Server waiting for client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " has come to server and is connected");
                System.out.println("----------------------------------------");
                Client NewClient = new Client(clientSocket, clientID);
                globalRoom.getClients().add(NewClient);
                new Thread(NewClient).start();
                clientID++;
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static void broadcastSendGlobal() {
        globalRoom.broadcastSendGlobal();
    }

    public static void createGroupRoom(ArrayList<String> usersNick, String groupName) {
        ArrayList<Client> users = getUsers(usersNick);
        GroupRoom groupRoom = new GroupRoom(users, groupName);
        System.out.println(groupRoom);
        for (Client user : users) {
            user.addGroupRoom(groupRoom);
            System.out.println(groupRoom.toString());

        }
        broadcastSendGlobal();
    }

    private static ArrayList<Client> getUsers(ArrayList<String> usersNick) {
        ArrayList<Client> users = new ArrayList<>();
        for (String element : usersNick) {
            for (Client client : Server.globalRoom.getClients()) {
                if (client.getNick().equals(element)) {
                    users.add(client);
                }
            }
        }
        return users;
    }

    private static Client getUser(String userNick) {

        for (Client client : Server.globalRoom.getClients()) {
            if (client.getNick().equals(userNick)) {
                return client;
            }
        }

        return null;
    }

    public static void createPrivateRoom(String directMessageTo, Client directMessageFrom) {
        Client user = getUser(directMessageTo);
        PrivateRoom privateRoom = new PrivateRoom(directMessageFrom, user);
        user.addPrivateRoom(privateRoom);
        directMessageFrom.addPrivateRoom(privateRoom);
        broadcastSendGlobal();

    }

    public static void addFile(File file) {
        globalRoom.addFile(file);
        broadcastSendGlobal();
    }

    /**
     * @return the clients
     */
    /**
     * @return the globalRoom
     */
    public static GlobalRoom getGlobalRoom() {
        return globalRoom;
    }
}
