package com.orangomango.tictactoe.lan;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private ServerSocket server;
    public static List<ClientManager> clients = new ArrayList<>();
    private boolean closed;
    
    public Server(String host, int port){
        try {
            this.server = new ServerSocket(port, 10, InetAddress.getByName(host));
        } catch (IOException ex){
            close();
            return;
        }
        Thread listener = new Thread(() -> listen());
        listener.setDaemon(true);
        listener.start();
        System.out.println("Server started");
    }
    
    private void listen(){
        while (!this.server.isClosed()){
            try {
                Socket socket = this.server.accept();
                Thread manager = new Thread(new ClientManager(socket));
                manager.setDaemon(true);
                manager.start();
            } catch (IOException ex){
                close();
            }
        }
    }
    
    public boolean isClosed(){
        return this.closed;
    }
    
    private void close(){
        try {
            if (this.server != null) this.server.close();
            this.closed = true;
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
