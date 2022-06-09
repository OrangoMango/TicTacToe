package com.orangomango.tictactoe.lan;

import java.net.*;
import java.io.*;

public class ClientManager implements Runnable {
    private Socket socket;
    private String username;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private static GameState gameState;
    
    public ClientManager(Socket socket){
        this.socket = socket;
        try {
            this.writer = new ObjectOutputStream(this.socket.getOutputStream());
            this.reader = new ObjectInputStream(this.socket.getInputStream());
            this.username = (String)this.reader.readObject();
            System.out.println(this.username);
        } catch (IOException|ClassNotFoundException ex){
            close();
        }
        synchronized (Server.clients){
            Server.clients.add(this);
            if (Server.clients.size() >= 2){
                System.out.println("Client connected!");
                System.out.println("Clients: "+Server.clients);
                if (Server.clients.size() == 2){
                    GameState gs = new GameState();
                    gs.startTime = System.currentTimeMillis();
                    gs.names = new String[]{Server.clients.get(0).username, Server.clients.get(1).username};
                    gs.currentMessage = gs.names[0]+"'s turn";
                    for (ClientManager cm : Server.clients){
                        try {
                            cm.writer.reset();
                            cm.writer.writeObject(gs);
                        } catch (IOException ex){
                            ex.printStackTrace();
                            //cm.close();
                        }
                    }
                }
            }
        }
    }
    
    private void close(){
        try {
            if (Server.clients.contains(this)) Server.clients.remove(this);
            if (this.socket != null) this.socket.close();
            if (this.reader != null) this.reader.close();
            if (this.writer != null) this.writer.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
    @Override
    public void run(){
        while (this.socket.isConnected()){
            try {
                GameState gs = (GameState)this.reader.readObject();
                this.gameState = gs;
                for (ClientManager cm : Server.clients){
                    if (cm != this){
                        cm.writer.reset();
                        cm.writer.writeObject(gs);
                    }
                }
            } catch (IOException|ClassNotFoundException ex){
                close();
            }
        }
    }
}
