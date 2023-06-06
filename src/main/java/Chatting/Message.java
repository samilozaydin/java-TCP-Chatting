/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chatting;

/**
 *
 * @author Bilal
 */
public class Message {

    private String sender;
    private String time;
    private String message;

    public Message(String sender, String time, String message) {
        this.sender = sender;
        this.time = time;
        this.message = message;
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }
}
