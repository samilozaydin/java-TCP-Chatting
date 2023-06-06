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
public class GroupRoom {

    private String groupName;
    private ArrayList<Client> clients;
    private ArrayList<Message> messages;
    private ArrayList<File> files;

    public GroupRoom(ArrayList<Client> clients, String groupName) {
        this.clients = clients;
        this.groupName = groupName;
        this.messages = new ArrayList<>();
        this.files = new ArrayList<>();

    }

    public void broadcastMessage() {
        JSONObject send = new JSONObject();
        JSONArray clientsSend = new JSONArray();
        for (Client client : this.clients) {
            clientsSend.put(client.getNick());
        }
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
        send.put("users", clientsSend);
        send.put("messages", messagesSend);
        send.put("processType", "5");
        send.put("files", filesSend);

        for (Client client : this.clients) {
            client.write(send);
        }
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
     * @return the clients
     */
    public ArrayList<Client> getClients() {
        return clients;
    }

    @Override
    public String toString() {
        return groupName + " is GROUP NAME";
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the messages
     */
    public ArrayList<Message> getMessages() {
        return messages;
    }

}
