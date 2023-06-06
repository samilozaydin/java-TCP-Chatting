/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chatting;

import java.io.File;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Bilal
 */
public class PrivateRoom {

    private Client first_client;
    private Client second_client;
    private ArrayList<Message> messages;
    private ArrayList<File> files;

    public PrivateRoom(Client first_client, Client second_client) {
        this.first_client = first_client;
        this.second_client = second_client;
        this.messages = new ArrayList<>();
        this.files = new ArrayList<>();

    }

    public void broadcastMessage() {
        JSONObject send = new JSONObject();

        JSONArray messagesSend = new JSONArray();
        for (Message message : this.messages) {
            JSONObject elements = new JSONObject();
            elements.put("sender", message.getSender());
            elements.put("message", message.getMessage());
            elements.put("time", message.getTime());
            messagesSend.put(elements);
        }
        JSONArray filesSend = new JSONArray();
        for (File file : files) {
            JSONObject elements = new JSONObject();
            elements.put("fileName", file.getName());
            filesSend.put(elements);
        }
        send.put("messages", messagesSend);
        send.put("processType", "8");
        send.put("files", filesSend);

        first_client.write(send);
        second_client.write(send);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }
    public void addFile(File file) {
        this.files.add(file);
    }
    public File getFile(String fileName){
        File file=null;
        for (File element: files) {
            if(element.getName().equals(fileName)){
                file = element;
            }
        }
        return file;
    }
    /**
     * @return the first_client
     */
    public Client getFirst_client() {
        return first_client;
    }

    /**
     * @return the second_client
     */
    public Client getSecond_client() {
        return second_client;
    }
}
