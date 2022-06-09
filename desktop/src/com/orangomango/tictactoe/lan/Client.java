package com.orangomango.tictactoe.lan;

import java.net.*;
import java.io.*;

public class Client {
    private String username;
    private Socket socket;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private boolean closed;
    
    public Client(String username, String host, int port){
        this.username = username;
        try {
            this.socket = new Socket(host, port);
            this.writer = new ObjectOutputStream(this.socket.getOutputStream());
            this.reader = new ObjectInputStream(this.socket.getInputStream());
            this.writer.writeObject(this.username);
        } catch (IOException ex){
            close();
        }
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public void send(GameState gs){
        try {
            this.writer.reset();
            this.writer.writeObject(gs);
        } catch (IOException ex){
            close();
        }
    }
    
    public synchronized GameState receive(){
        GameState gs = null;
        try {
            Object o = this.reader.readObject();
            do {
                gs = (GameState)o;
            } while (!(o instanceof GameState));
        } catch (IOException|ClassNotFoundException ex){
            close();
        }
        return gs;
    }
    
    public boolean isClosed(){
        return this.closed;
    }
    
    public void close(){
        try {
            if (this.socket != null) this.socket.close();
            if (this.reader != null) this.reader.close();
            if (this.writer != null) this.writer.close();
            this.closed = true;
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
