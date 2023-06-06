/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chatting;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Bilal
 */
public class GlobalRoom {

    private ArrayList<Client> clients;
    private ArrayList<Message> messages;
    private ArrayList<File> files;
    public GlobalRoom() {
        this.clients = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.files = new ArrayList<>();

}

public void broadcastSendGlobal() {
        for (Client client : this.getClients()) {
            client.sendGlobal();
        }
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
    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    /**
     * @return the clients
     */
    public ArrayList<Client> getClients() {
        return clients;
    }

    /**
     * @param clients the clients to set
     */
    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }

    /**
     * @return the messages
     */
    public ArrayList<Message> getMessages() {
        return messages;
    }

    /**
     * @return the files
     */
    public ArrayList<File> getFiles() {
        return files;
    }
}
