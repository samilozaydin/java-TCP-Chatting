/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chatting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Bilal
 */
public class Client implements Runnable {

    private ArrayList<PrivateRoom> privateRooms;
    private ArrayList<GroupRoom> groupRooms;
    private String nick;
    private short readBuffer = 1024;
    private int ID;

    Socket client;
    InputStream in;
    OutputStream out;

    private HashMap<String, Consumer> functions;

    public Client(Socket socket, int id) throws IOException {
        this.client = socket;
        this.ID = id;
        this.in = this.client.getInputStream();
        this.out = this.client.getOutputStream();
        this.privateRooms = new ArrayList<>();
        this.groupRooms = new ArrayList<>();
        initFunctions();
    }

    @Override
    public void run() {
        while (!this.client.isClosed()) {
            String receivedData = read();
            if (receivedData.isEmpty()) {
                endConnection();
            }

            receivedData = receivedData.trim();
            JSONObject jsonObject = new JSONObject(receivedData);

            Consumer consumer = functions.get(jsonObject.getString("processType"));
            if (consumer != null) {
                consumer.accept(jsonObject);
            }
        }
    }

    private void getNickName(Object json) {
        System.out.println("waiting for nickname");
        JSONObject data = (JSONObject) json;
        this.nick = data.getString("nick");
        System.out.println("nickName is = " + this.getNick());
    }

    private void updateGlobalScreen(Object json) {
        Server.broadcastSendGlobal();
    }

    public void sendGlobal() {
        JSONObject send = new JSONObject();
        send.put("processType", "2");

        ArrayList<Client> clients = Server.getGlobalRoom().getClients();
        JSONArray clientsSend = new JSONArray();
        for (Client client : clients) {
            clientsSend.put(client.getNick());
        }
        ArrayList<Message> messages = Server.getGlobalRoom().getMessages();
        JSONArray messagesSend = new JSONArray();
        for (Message message : messages) {
            JSONObject elements = new JSONObject();
            elements.put("sender", message.getSender());
            elements.put("message", message.getMessage());
            elements.put("time", message.getTime());
            messagesSend.put(elements);
        }
        ArrayList<File> files = Server.getGlobalRoom().getFiles();
        JSONArray filesSend = new JSONArray();
        for (File file : files) {
            JSONObject elements = new JSONObject();
            elements.put("fileName", file.getName());
            filesSend.put(elements);
        }

        JSONArray clientPrivateRoomSend = new JSONArray();
        for (PrivateRoom room : this.privateRooms) {
            if (room.getFirst_client().equals(this)) {
                clientPrivateRoomSend.put(room.getSecond_client().getNick());
            } else if (room.getSecond_client().equals(this)) {
                clientPrivateRoomSend.put(room.getFirst_client().getNick());
            }
        }

        JSONArray clientGroups = new JSONArray();
        for (GroupRoom room : this.groupRooms) {
            clientGroups.put(room.getGroupName());
            System.out.println(room);
            System.out.println(room.toString());
        }

        send.put("clients", clientsSend);
        send.put("privateRooms", clientPrivateRoomSend);
        send.put("groupRooms", clientGroups);
        send.put("messages", messagesSend);
        send.put("files", filesSend);

        write(send);
    }

    private void getMessage(Object json) {
        Message NewMessage = returnMessage(json);
        Server.getGlobalRoom().addMessage(NewMessage);
        Server.broadcastSendGlobal();
    }

    private Message returnMessage(Object json) {
        JSONObject data = (JSONObject) json;
        String sender = data.getString("sender");
        String time = data.getString("time");
        String message = data.getString("message");

        System.out.println(sender + "   " + time + "    " + message);

        return new Message(sender, time, message);
    }

    private void createGroupRoom(Object json) {
        JSONObject data = (JSONObject) json;
        String roomName = data.getString("roomName");

        JSONArray usersNick = data.getJSONArray("users");
        ArrayList<String> users = new ArrayList<>();
        for (Object element : usersNick) {
            String nickname = (String) element;
            users.add(nickname);
        }
        //System.out.println(users.toString());
        Server.createGroupRoom(users, roomName);
    }

    public void addGroupRoom(GroupRoom room) {
        this.groupRooms.add(room);
    }

    private void getGroupRoom(Object json) {

        JSONObject data = (JSONObject) json;

        String roomName = data.getString("roomName");
        GroupRoom room = new GroupRoom(null, null);
        for (GroupRoom element : this.groupRooms) {
            if (element.getGroupName().equals(roomName)) {
                room = element;
            }
        }

        room.broadcastMessage();

        /*  JSONObject send = new JSONObject();
        JSONArray clientsSend = new JSONArray();
        for (Client client : room.getClients()) {
            clientsSend.put(client.getNick());
        }
        JSONArray messagesSend = new JSONArray();
        for (Message message : room.getMessages()) {
            JSONObject elements = new JSONObject();
            elements.put("sender", message.getSender());
            elements.put("message", message.getMessage());
            elements.put("time", message.getTime());
            messagesSend.put(elements);
        }

        send.put("users", clientsSend);
        send.put("messages", messagesSend);
        send.put("processType", "5");

        this.write(send);*/
    }

    private void getMessageRoom(Object json) {
        Message NewMessage = returnMessage(json);

        JSONObject data = (JSONObject) json;
        String roomName = data.getString("roomName");
        GroupRoom room = new GroupRoom(null, null);
        for (GroupRoom element : this.groupRooms) {
            if (element.getGroupName().equals(roomName)) {
                room = element;
            }
        }
        room.addMessage(NewMessage);
        room.broadcastMessage();

    }

    private void createPrivateRoom(Object json) {
        JSONObject data = (JSONObject) json;
        String directMessageTo = data.getString("directMessageTo");

        Server.createPrivateRoom(directMessageTo, this);
    }

    public void addPrivateRoom(PrivateRoom room) {
        this.privateRooms.add(room);
    }

    private void getPrivateRoom(Object json) {
        JSONObject data = (JSONObject) json;
        String directMessageTo = data.getString("directMessageTo");

        PrivateRoom room = new PrivateRoom(null, null);
        for (PrivateRoom element : this.privateRooms) {
            if (element.getFirst_client().getNick().equals(directMessageTo)
                    || element.getSecond_client().getNick().equals(directMessageTo)) {
                room = element;
            }
        }
        room.broadcastMessage();

    }

    private void getMessagePrivateRoom(Object json) {
        Message NewMessage = returnMessage(json);

        JSONObject data = (JSONObject) json;
        String directMessageTo = data.getString("directMessageTo");
        PrivateRoom room = new PrivateRoom(null, null);
        for (PrivateRoom element : this.privateRooms) {
            if (element.getFirst_client().getNick().equals(directMessageTo)
                    || element.getSecond_client().getNick().equals(directMessageTo)) {
                room = element;
            }
        }
        room.addMessage(NewMessage);
        room.broadcastMessage();

    }

    private void acceptFile(Object json) {
        JSONObject jsonObject = (JSONObject) json;
        write(json);
        Consumer consumer = functions.get(jsonObject.getString("fileWhere"));
        if (consumer != null) {
            consumer.accept(jsonObject);
        }

    }

    private void receiveFileGlobal(Object json) {
        File file = receiveFile(json);
        Server.addFile(file);

    }

    private File receiveFile(Object json) {
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            dataInputStream = new DataInputStream(this.client.getInputStream());
            dataOutputStream = new DataOutputStream(this.client.getOutputStream());

            JSONObject data = (JSONObject) json;
            String fileName = data.getString("fileName");
            File newFile = new File(fileName);

            int fileContentlength = dataInputStream.readInt();
            if (fileContentlength > 0) {

                byte[] fileContentBytes = new byte[fileContentlength];
                dataInputStream.readFully(fileContentBytes, 0, fileContentlength);
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                fileOutputStream.write(fileContentBytes, 0, fileContentBytes.length);

            } else {
                System.out.println("THERE IS A PROBLEM AGAIN");
            }

            /*  int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);

            long size = dataInputStream.readLong(); // read file size
            byte[] buffer = new byte[4 * 1024];
            while (size > 0
                    && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size)))
                    != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;
            }*/
            return newFile;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void sendFileGlobal(Object json) {
        JSONObject data = (JSONObject) json;
        String fileName = data.getString("fileName");
        File file = getFile(fileName);

        sendFile(/*json,*/file);
    }

    private void sendFile(/*Object json,*/File file) {
        //JSONObject data = (JSONObject) json;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            //String fileName = data.getString("fileName");
            dataInputStream = new DataInputStream(this.client.getInputStream());
            dataOutputStream = new DataOutputStream(this.client.getOutputStream());

            ////////   /* File file = getFile(fileName);*////////
            //System.out.println("----------FILE INFO-----------");

            /*System.out.println(file);
            System.out.println(file.getAbsoluteFile());
            System.out.println(file.getName());
            System.out.println(file.getTotalSpace());*/
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] fileContentBytes = new byte[(int) file.length()];
            fileInputStream.read(fileContentBytes);

            dataOutputStream.writeInt(fileContentBytes.length);
            dataOutputStream.write(fileContentBytes);

            //dataOutputStream.writeLong(file.length());

            /*  byte[] buffer = new byte[4 * 1024];
            int bytes;
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }*/
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private File getFile(String fileName) {
        return Server.getGlobalRoom().getFile(fileName);
    }

    private void receiveFileRoom(Object json) {
        File file = receiveFile(json);

        JSONObject data = (JSONObject) json;
        String roomName = data.getString("roomName");
        GroupRoom room = new GroupRoom(null, null);
        for (GroupRoom element : this.groupRooms) {
            if (element.getGroupName().equals(roomName)) {
                room = element;
            }
        }
        room.addFile(file);
        room.broadcastMessage();

    }

    private void sendFileRoom(Object json) {
        JSONObject data = (JSONObject) json;
        String fileName = data.getString("fileName");
        String roomName = data.getString("roomName");

        GroupRoom room = new GroupRoom(null, null);
        for (GroupRoom element : this.groupRooms) {
            if (element.getGroupName().equals(roomName)) {
                room = element;
            }
        }
        File file = room.getFile(fileName);
        sendFile(/*json,*/file);
    }

    private void receiveFilePrivate(Object json) {
        File file = receiveFile(json);

        JSONObject data = (JSONObject) json;
        String directMessageTo = data.getString("directMessageTo");
        PrivateRoom room = new PrivateRoom(null, null);
        for (PrivateRoom element : this.privateRooms) {
            if (element.getFirst_client().getNick().equals(directMessageTo)
                    || element.getSecond_client().getNick().equals(directMessageTo)) {
                room = element;
            }
        }
        room.addFile(file);
        room.broadcastMessage();

    }
    private void sendFilePrivate(Object json) {
        JSONObject data = (JSONObject) json;
        String fileName = data.getString("fileName");
        String directMessageTo = data.getString("directMessageTo");

        PrivateRoom room = new PrivateRoom(null, null);
        for (PrivateRoom element : this.privateRooms) {
            if (element.getFirst_client().getNick().equals(directMessageTo)
                    || element.getSecond_client().getNick().equals(directMessageTo)) {
                room = element;
            }
        }
        File file = room.getFile(fileName);
        
        sendFile(/*json,*/file);
    }
    private void endConnection() {
        for (GroupRoom room : this.groupRooms) {
            room.getClients().remove(this);
        }
        for (PrivateRoom room : this.privateRooms) {
            if (room.getFirst_client().equals(this)) {
                room.getSecond_client().privateRooms.remove(room);
            } else if (room.getSecond_client().equals(this)) {
                room.getFirst_client().privateRooms.remove(room);
            }
        }
        Server.getGlobalRoom().getClients().remove(this);
        Server.broadcastSendGlobal();
        try {
            this.client.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String read() {
        String output = null;
        String result = "";
        try {
            do {
                byte[] data = new byte[readBuffer];
                in.read(data);
                output = new String(data, StandardCharsets.US_ASCII).replaceAll("\u0000", "");
                // System.out.println("received data == " + output);
                result = result.concat(output);
            } while (in.available() > 0);

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void write(Object json) {
        try {

            JSONObject jsonObject = (JSONObject) json;
            //jsonObject.put("name", "John");
            String stringfiedJSON = jsonObject.toString();

            byte[] sentData = stringfiedJSON.getBytes();
            System.out.println("itself: " + json);
            System.out.println("send datta " + sentData + "  " + sentData[0]);
            out.write(sentData);
            //System.out.println("Message is sent");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initFunctions() {
        this.functions = new HashMap<>();
        functions.put("1", this::getNickName);
        functions.put("2", this::updateGlobalScreen);
        functions.put("3", this::getMessage);
        functions.put("4", this::createGroupRoom);
        functions.put("5", this::getGroupRoom);
        functions.put("6", this::getMessageRoom);
        functions.put("7", this::createPrivateRoom);
        functions.put("8", this::getPrivateRoom);
        functions.put("9", this::getMessagePrivateRoom);
        functions.put("10", this::acceptFile);
        functions.put("11", this::receiveFileGlobal);
        functions.put("12", this::sendFileGlobal);
        functions.put("13", this::receiveFileRoom);
        functions.put("14", this::sendFileRoom);
        functions.put("15", this::receiveFilePrivate);
        functions.put("16", this::sendFilePrivate);

    }
// Message Types:
// "1" : getNickName for server
// "2" : getGlobal for client // for server sendGlobal
// "3" : getMessage for server
// "4" : createRoom at server
// "5" : get the speficy room
// "6" : getMessage from room at server// send message from room at client
// "7" : create the specific person message from server
// "8" : call private room from client // send from server
// "9" : send message from client// store message at server
// "10": call for storing file

    /**
     * @return the nick
     */
    public String getNick() {
        return nick;
    }
}
