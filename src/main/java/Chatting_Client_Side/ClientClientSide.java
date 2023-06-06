/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chatting_Client_Side;

import java.io.ByteArrayOutputStream;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Bilal
 */
public class ClientClientSide implements Runnable {

    Socket client;
    private InputStream in;
    private OutputStream out;
    int port;
    private short readBuffer = 1024;
    String nickname;

    private HashMap<String, Consumer> functions;

    public ClientClientSide(int port, String nickname) throws IOException {
        this.port = port;
        this.client = new Socket("13.53.199.23", this.port);
        this.in = client.getInputStream();
        this.out = client.getOutputStream();
        this.nickname = nickname;
        initFunctions();
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
            } while (getIn().available() > 0);

        } catch (IOException ex) {
            Logger.getLogger(ClientClientSide.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void write(JSONObject json) {
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
            Logger.getLogger(ClientClientSide.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (!this.client.isClosed()) {
            String receivedData = read();
            if (receivedData.isEmpty()) {
                try {
                    /*JSONObject json = new JSONObject();
                    json.put("processType", "blabla");
                    json.put("nick", this.nickname);
                    write(json);*/
                    // YUKARIDAKI SONRADAN IŞLEME ALINACAK
                    System.out.println("Client is disconnected");
                    this.client.close();
                    this.in.close();
                    this.out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ClientClientSide.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            receivedData = receivedData.trim();
            JSONObject jsonObject = new JSONObject(receivedData);

            Consumer consumer = functions.get(jsonObject.getString("processType"));
            if (consumer != null) {
                consumer.accept(jsonObject);
            }
        }
    }

    private void getGlobal(Object json) {
        JSONObject data = (JSONObject) json;

        JSONArray clientsJSON = data.getJSONArray("clients");

        GlobalScreen.getListmodel_global().removeAllElements();

        List<Object> clients = clientsJSON.toList();
        // System.out.println("Geldi mi? " + clients.toString());
        for (Object elementObject : clients) {
            String element = (String) elementObject;
            //System.out.println(elementObject);
            if (!elementObject.equals(this.nickname)) {
                GlobalScreen.getListmodel_global().addElement(element);
            }
        }
        // System.out.println("--- Clients Bitti---");
        JSONArray privateRoomsJSON = data.getJSONArray("privateRooms");
        GlobalScreen.getListmodel_private().removeAllElements();

        // System.out.println("--- Private Room---");
        List<Object> privateRooms = privateRoomsJSON.toList();
        for (Object elementObject : privateRooms) {
            String element = (String) elementObject;
            //System.out.println(elementObject);
            GlobalScreen.getListmodel_private().addElement(element);
        }
        // System.out.println("--- Private Room Bitti---");

        JSONArray groupRoomsJSON = data.getJSONArray("groupRooms");
        GlobalScreen.getListmodel_groups().removeAllElements();
        // System.out.println("--- Group Room ---");

        List<Object> groupRooms = groupRoomsJSON.toList();
        for (Object elementObject : groupRooms) {
            String element = (String) elementObject;
            GlobalScreen.getListmodel_groups().addElement(element);
        }
        //System.out.println("--- Group Room Bitti---");
        JSONArray messagesJSON = data.getJSONArray("messages");
        GlobalScreen.getListmodel_globalMessages().removeAllElements();
        // System.out.println("--- Nessages ---");

        messagesJSON.forEach(element -> {
            JSONObject jsonObj = new JSONObject();
            jsonObj = (JSONObject) element;

            String sender = jsonObj.getString("sender");
            String time = jsonObj.getString("time");
            sender += " " + time;
            GlobalScreen.getListmodel_globalMessages().addElement(sender);

            String message = jsonObj.getString("message");
            GlobalScreen.getListmodel_globalMessages().addElement(message);
        });

        JSONArray filesJSON = data.getJSONArray("files");
        GlobalScreen.getListmodel_files().removeAllElements();

        filesJSON.forEach(element -> {
            JSONObject jsonObj = new JSONObject();
            jsonObj = (JSONObject) element;

            String fileName = jsonObj.getString("fileName");

            GlobalScreen.getListmodel_files().addElement(fileName);
        });

    }

    private void getRoom(Object json) {
        JSONObject data = (JSONObject) json;

        JSONArray clientsJSON = data.getJSONArray("users");
        GroupScreen.getListmodel_users().removeAllElements();

        List<Object> clients = clientsJSON.toList();
        // System.out.println("Geldi mi? " + clients.toString());
        for (Object elementObject : clients) {
            String element = (String) elementObject;
            //System.out.println(elementObject);
            if (!elementObject.equals(this.nickname)) {
                GroupScreen.getListmodel_users().addElement(element);
            }
        }

        JSONArray messagesJSON = data.getJSONArray("messages");
        GroupScreen.getListmodel_messages().removeAllElements();
        // System.out.println("--- Nessages ---");

        messagesJSON.forEach(element -> {
            JSONObject jsonObj = new JSONObject();
            jsonObj = (JSONObject) element;

            String sender = jsonObj.getString("sender");
            String time = jsonObj.getString("time");
            sender += " " + time;
            GroupScreen.getListmodel_messages().addElement(sender);

            String message = jsonObj.getString("message");
            GroupScreen.getListmodel_messages().addElement(message);
        });

        JSONArray filesJSON = data.getJSONArray("files");
        GroupScreen.getListmodel_files().removeAllElements();

        filesJSON.forEach(element -> {
            JSONObject jsonObj = new JSONObject();
            jsonObj = (JSONObject) element;

            String fileName = jsonObj.getString("fileName");
            GroupScreen.getListmodel_files().addElement(fileName);
        });
        //System.out.println("--- Nessages Bitti---");
    }

    private void getPrivateRoom(Object json) {
        JSONObject data = (JSONObject) json;

        JSONArray messagesJSON = data.getJSONArray("messages");
        PrivateScreen.getListmodel_messages().removeAllElements();
        // System.out.println("--- Nessages ---");

        messagesJSON.forEach(element -> {
            JSONObject jsonObj = new JSONObject();
            jsonObj = (JSONObject) element;

            String sender = jsonObj.getString("sender");
            String time = jsonObj.getString("time");
            sender += " " + time;
            PrivateScreen.getListmodel_messages().addElement(sender);

            String message = jsonObj.getString("message");
            PrivateScreen.getListmodel_messages().addElement(message);
        });
        
        JSONArray filesJSON = data.getJSONArray("files");
        PrivateScreen.getListmodel_files().removeAllElements();

        filesJSON.forEach(element -> {
            JSONObject jsonObj = new JSONObject();
            jsonObj = (JSONObject) element;

            String fileName = jsonObj.getString("fileName");
            PrivateScreen.getListmodel_files().addElement(fileName);
        });
    }

    private void preparingToSendFile(Object json) {
        JSONObject jsonObject = (JSONObject) (json);
        Consumer consumer = functions.get(jsonObject.getString("fileWhere"));
        if (consumer != null) {
            consumer.accept(jsonObject);
        }
    }

    private void sendFileToGlobal(Object json) {
        File file = GlobalScreen.getSelectedFile();
        sendFile(file);
    }

    private void sendFile(File file) {
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            // String path = GlobalScreen.getSelectedFilePath();
            dataInputStream = new DataInputStream(this.client.getInputStream());
            dataOutputStream = new DataOutputStream(this.client.getOutputStream());

            // File file = GlobalScreen.getSelectedFile();
            FileInputStream fileInputStream = new FileInputStream(file);

            /*String fileName = file.getName();
            byte[] fileNameBytes = fileName.getBytes();*/
            byte[] fileContentBytes = new byte[(int) file.length()];
            fileInputStream.read(fileContentBytes);

            /*dataOutputStream.writeInt(fileNameBytes.length);
            dataOutputStream.write(fileNameBytes);*/
            dataOutputStream.writeInt(fileContentBytes.length);
            dataOutputStream.write(fileContentBytes);

            //dataOutputStream.writeLong(file.length());

            /* byte[] buffer = new byte[4 * 1024];
            int bytes;
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }*/
        } catch (IOException ex) {
            Logger.getLogger(ClientClientSide.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveFile(Object json) {
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

            /*          int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            long size = dataInputStream.readLong(); // read file size
            byte[] buffer = new byte[4 * 1024];
            while (size > 0
                    && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size)))
                    != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;
            }
            storeFileToPc(newFile);

            System.out.println(newFile);
            System.out.println(newFile.getAbsoluteFile());
            System.out.println(newFile.getName());
            System.out.println(newFile.getTotalSpace());*/
        } catch (IOException ex) {
            Logger.getLogger(ClientClientSide.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendFileToRoom(Object json) {
        File file = GroupScreen.getSelectedFile();
        sendFile(file);
    }

    private void sendFileToPrivate(Object json) {
        File file = PrivateScreen.getSelectedFile();
        sendFile(file);
    }

    /* private void storeFileToPc(File file) {
        String destinationDir = "C:\\Users\\Bilal\\Desktop\\";
        String fileName = file.getName();

        try {
            // Read the file content into a byte array
            byte[] contentBytes = Files.readAllBytes(file.toPath());

            // Create the destination directory if it doesn't exist
            File destDir = new File(destinationDir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // Create the file path
            Path filePath = Paths.get(destinationDir, fileName);

            // Save the file to storage
            Files.write(filePath, contentBytes, StandardOpenOption.CREATE);

            System.out.println("File saved successfully!");
        } catch (IOException e) {
            System.out.println("Error occurred while saving the file: " + e.getMessage());
        }

    }
     */
    private void initFunctions() {
        this.functions = new HashMap<>();
        this.functions.put("2", this::getGlobal);
        this.functions.put("5", this::getRoom);
        this.functions.put("8", this::getPrivateRoom);
        this.functions.put("10", this::preparingToSendFile);
        this.functions.put("11", this::sendFileToGlobal);
        this.functions.put("12", this::receiveFile);
        this.functions.put("13", this::sendFileToRoom);
        this.functions.put("14", this::receiveFile);
        this.functions.put("15", this::sendFileToPrivate);
        this.functions.put("16", this::receiveFile);

    }
// Message Types:
// "1" : getNickName for server
// "2" : getGlobal for client // for server sentGlobal
// "5" : call rooms from client
// "8" : call private message
// "10": start file sending process

    /**
     * @return the in
     */
    public InputStream getIn() {
        return in;
    }

    /**
     * @return the out
     */
    public OutputStream getOut() {
        return out;
    }

}
