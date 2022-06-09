package com.orangomango.tictactoe;

public enum Player {
    PLAYER_X("X"), PLAYER_O("O");
    
    private String symbol;
    
    private Player(String symbol){
        this.symbol = symbol;
    }
    
    public String getSymbol(){
        return this.symbol;
    }
    
    public static Player opposite(Player player){
        return player == PLAYER_O ? PLAYER_X : PLAYER_O;
    }
}
