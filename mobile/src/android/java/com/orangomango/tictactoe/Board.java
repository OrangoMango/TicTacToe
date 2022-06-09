package com.orangomango.tictactoe;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Random;
import com.orangomango.tictactoe.lan.GameState;

public class Board {
    public static final double BOARD_SIZE = 200;
    public static final double CELL_SIZE = BOARD_SIZE / 3;
    
    private Player[][] board = new Player[3][3];
    private String player1, player2;
    private double x, y;
    private Player currentPlayer = Player.PLAYER_O;
    private boolean ai;
    private boolean gameFinished;
    private long gameStart;
    private long endTime;
    private String message = "";
    private String difficulty;
    public boolean canPlace = true;
    private int[] points = new int[2];
    public Player playerLock;
    public boolean waiting;
    
    public Board(String p1, String p2, double x, double y){
        this.player1 = p1;
        this.player2 = p2;
        this.x = x;
        this.y = y;
        this.gameStart = System.currentTimeMillis();
        this.message = this.player1+"'s turn";
    }
    
    public void updateFromState(GameState gs){
        this.player1 = gs.names[0];
        this.player2 = gs.names[1];
        this.gameStart = gs.startTime;
        this.board = gs.board;
        this.points = gs.points;
        int xPlayer = 0;
        int oPlayer = 0;
        for (int y = 0; y < 3; y++){
            for (int x = 0; x < 3; x++){
                if (this.board[y][x] == null) continue;
                switch (this.board[y][x]){
                    case PLAYER_O:
                        oPlayer++;
                        break;
                    case PLAYER_X:
                        xPlayer++;
                        break;
                }
            }
        }
        if (oPlayer <= xPlayer){
            this.currentPlayer = Player.PLAYER_O;
        } else {
            this.currentPlayer = Player.PLAYER_X;
        }
        this.gameFinished = gs.gameFinished;
        this.message = gs.currentMessage;
    }
    
    public void setDifficulty(String value){
        this.difficulty = value;
    }
    
    public void setAI(boolean ai){
        this.ai = ai;
    }
    
    public double getX(){
        return this.x;
    }
    
    public double getY(){
        return this.y;
    }
    
    public String getMessage(){
        return this.message;
    }
    
    public long getStartTime(){
        return this.gameStart;
    }
    
    public Player[][] getBoard(){
        return this.board;
    }
    
    public Player getCurrentPlayer(){
        return this.currentPlayer;
    }    
    
    public void draw(GraphicsContext gc){
        gc.save();
        gc.translate(x, y);
        gc.setLineWidth(4);
        gc.setFill(Color.RED);
        gc.setFont(new Font("Sans-serif", CELL_SIZE));
        for (int i = 0; i < 4; i++){
            if (i == 0 || i == 3) continue;
            gc.strokeLine(i*CELL_SIZE, 0, i*CELL_SIZE, BOARD_SIZE);
            gc.strokeLine(0, i*CELL_SIZE, BOARD_SIZE, i*CELL_SIZE);
        }
        for (int y = 0; y < 3; y++){
            for (int x = 0; x < 3; x++){
                if (this.board[y][x] != null){
                    gc.fillText(this.board[y][x].getSymbol(), x*CELL_SIZE+CELL_SIZE/6, y*CELL_SIZE+CELL_SIZE-CELL_SIZE/5);
                }
            }
        }
        gc.restore();
    }
    
    public void drawPlayCanvas(GraphicsContext gc){
        gc.clearRect(0, 0, MainApplication.bounds.getWidth(), MainApplication.bounds.getHeight());
        gc.setFill(Color.web("#d8e3cc"));
        gc.fillRect(0, 0, MainApplication.bounds.getWidth(), MainApplication.bounds.getHeight());
        draw(gc);
        long difference = System.currentTimeMillis() - this.gameStart;
        if (this.gameFinished && this.endTime == 0){
            this.endTime = difference;
        }
        gc.setFill(Color.BLUE);
        gc.setFont(new Font("sans-serif", 25));
        gc.fillText(String.format("Points > O:%s X:%s", this.points[0], this.points[1]), 50, MainApplication.bounds.getHeight()-140);
        if (this.gameFinished){
            difference = this.endTime;
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("sans-serif", 20));
            gc.fillText("Tap to restart", 50, MainApplication.bounds.getHeight()-100);
        }
        gc.setFont(new Font("sans-serif", 35));
        gc.setFill(Color.GREEN);
        long seconds = difference/1000%60;
        String secondsString;
        if (seconds < 10){
            secondsString = "0"+seconds;
        } else {
            secondsString = Long.toString(seconds);
        }
        gc.fillText(String.format("%s:%s", difference/60000, secondsString), 50, 100);
        gc.fillText(this.waiting ? "waiting..." : this.message, 50, 150);
    }
    
    private void nextPlayer(){
        if (this.currentPlayer == Player.PLAYER_X){
            this.currentPlayer = Player.PLAYER_O;
            this.message = this.player1+"'s turn";
        } else {
            this.currentPlayer = Player.PLAYER_X;
            this.message = this.player2+"'s turn";
        }
    }
    
    private boolean hasWon(Player player){
        for (int i = 0; i < 3; i++){
            if (this.board[i][0] == player && this.board[i][1] == player && this.board[i][2] == player){
                return true;
            }
        }
        for (int i = 0; i < 3; i++){
            if (this.board[0][i] == player && this.board[1][i] == player && this.board[2][i] == player){
                return true;
            }
        }
        if (this.board[0][0] == player && this.board[1][1] == player && this.board[2][2] == player){
            return true;
        }
        if (this.board[0][2] == player && this.board[1][1] == player && this.board[2][0] == player){
            return true;
        }
        return false;
    }
    
    private boolean isEmpty(int x, int y){
        return this.board[y][x] == null;
    }
    
    private boolean isFull(){
        for (int y = 0; y < 3; y++){
            for (int x = 0; x < 3; x++){
                if (this.board[y][x] == null){
                    return false;
                }
            }
        }
        return true;
    }
    
    public int[] randomMove(){
        int[] result = new int[2];
        int[][] empty = new int[3][3];
        Random random = new Random();
        int emptyN = 0;
        for (int y = 0; y < 3; y++){
            for (int x = 0; x < 3; x++){
                if (isEmpty(x, y)){
                    empty[y][x] = 1;
                    emptyN++;
                }
            }
        }
        int selected = random.nextInt(emptyN);
        int c = 0;
        for (int y = 0; y < 3; y++){
            for (int x = 0; x < 3; x++){
                if (empty[y][x] == 1 && c == selected){
                    result[0] = x;
                    result[1] = y;
                    break;
                }
                if (empty[y][x] == 1) c++;
            }
        }
        return result;
    }
    
    public void cpuMove(){
        nextPlayer();
        this.canPlace = false;
        int bestX = -1;
        int bestY = -1;
        diff:
        switch (this.difficulty) {
            case "Easy":
                int[] result = randomMove();
                bestX = result[0];
                bestY = result[1];
                break;
            case "Medium":
                for (int y = 0; y < 3; y++){
                    for (int x = 0; x < 3; x++){
                        if (isEmpty(x, y)){
                            this.board[y][x] = this.currentPlayer;
                            if (hasWon(this.currentPlayer)){
                                bestX = x;
                                bestY = y;
                                this.board[y][x] = null;
                                break diff;
                            }
                            this.board[y][x] = null;
                        }
                    }
                }
                for (int y = 0; y < 3; y++){
                    for (int x = 0; x < 3; x++){
                        if (isEmpty(x, y)){
                            this.board[y][x] = Player.opposite(this.currentPlayer);
                            if (hasWon(Player.opposite(this.currentPlayer))){
                                bestX = x;
                                bestY = y;
                                this.board[y][x] = null;
                                break diff;
                            }
                            this.board[y][x] = null;
                        }
                    }
                }
                int[] finalResult = randomMove();
                bestX = finalResult[0];
                bestY = finalResult[1];
                break;
            default:
                int bestScore = -1;
                for (int y = 0; y < 3; y++){
                    for (int x = 0; x < 3; x++){
                        if (isEmpty(x, y)){
                            this.board[y][x] = this.currentPlayer;
                            int score = minimax(0, false);
                            if (score > bestScore){
                                bestScore = score;
                                bestX = x;
                                bestY = y;
                            }
                            this.board[y][x] = null;
                        }
                    }
                }
                break;
        }
        this.board[bestY][bestX] = this.currentPlayer;
        if (hasWon(this.currentPlayer)){
            this.message = "AI won";
            this.points[this.currentPlayer == Player.PLAYER_O ? 0 : 1] += 1;
            this.gameFinished = true;
        } else if (isFull()){
            this.message = "Draw";
            this.gameFinished = true;
        } else if (hasWon(Player.opposite(this.currentPlayer))){
            this.message = this.player1+" won";
            this.points[this.currentPlayer == Player.PLAYER_O ? 0 : 1] += 1;
            this.gameFinished = true;
        } else {
            nextPlayer();
        }
        this.canPlace = true;
    }
    
    public void clear(){
        this.gameFinished = false;
        this.board = new Player[3][3];
        this.gameStart = System.currentTimeMillis();
        this.endTime = 0;
        this.message = this.player1+"'s turn";
        this.canPlace = true;
        this.currentPlayer = Player.PLAYER_O;
    }
    
    public boolean isGameFinished(){
        return this.gameFinished;
    }
    
    private int minimax(int depth, boolean maximizing){
        if (hasWon(this.currentPlayer)){
            return 1;
        } else if (hasWon(Player.opposite(this.currentPlayer))){
            return -1;
        } else if (isFull()){
            return 0;
        }
        
        if (maximizing){
            int bestScore = -1;
            for (int y = 0; y < 3; y++){
                for (int x = 0; x < 3; x++){
                    if (isEmpty(x, y)){
                        this.board[y][x] = this.currentPlayer;
                        int score = minimax(depth+1, false);
                        bestScore = Math.max(score, bestScore);
                        this.board[y][x] = null;
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = 1;
            for (int y = 0; y < 3; y++){
                for (int x = 0; x < 3; x++){
                    if (isEmpty(x, y)){
                        this.board[y][x] = Player.opposite(this.currentPlayer);
                        int score = minimax(depth+1, true);
                        bestScore = Math.min(score, bestScore);
                        this.board[y][x] = null;
                    }
                }
            }
            return bestScore;
        }
    }
    
    private String getPlayerName(Player player){
        if (player == Player.PLAYER_O){
            return this.player1;
        } else {
            return this.player2;
        }
    }
    
    public void place(Player player, int x, int y){
        if (!isEmpty(x, y) || this.gameFinished || (this.playerLock != null && this.playerLock != this.currentPlayer)){
            return;
        }
        this.board[y][x] = player;
        if (hasWon(this.currentPlayer)){
            this.message = getPlayerName(this.currentPlayer)+" won";
            this.points[this.currentPlayer == Player.PLAYER_O ? 0 : 1] += 1;
            this.gameFinished = true;
        } else if (isFull()){
            this.message = "Draw";
            this.gameFinished = true;
        } else {
            if (this.ai){
                cpuMove();
            } else {
                nextPlayer();
            }
        }
    }
}
