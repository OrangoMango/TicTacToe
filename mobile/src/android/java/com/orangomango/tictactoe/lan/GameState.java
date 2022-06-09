package com.orangomango.tictactoe.lan;

import java.io.Serializable;
import com.orangomango.tictactoe.Player;

public class GameState implements Serializable{
    public Player[][] board = new Player[3][3];
    public long startTime;
    public String[] names = new String[2];
    public int[] points = new int[2];
    public boolean gameFinished;
    public String currentMessage;
    
    @Override
    public String toString(){
        return board+" "+(names.length >= 2 ? names[0]+" "+names[1] : "null")+" time: "+startTime+" Game finished: "+gameFinished;
    }
}
