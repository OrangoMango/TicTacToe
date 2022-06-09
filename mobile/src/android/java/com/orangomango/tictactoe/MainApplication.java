package com.orangomango.tictactoe;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import com.orangomango.tictactoe.lan.*;
import android.os.Build;
import android.widget.Toast;
import javafxports.android.FXActivity;
import java.lang.reflect.*;

public class MainApplication extends Application {

    public static final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    //public static final Rectangle2D bounds = new Rectangle2D(0, 0, 360, 648);
    private volatile GameState gs;
    private Client client;
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    private GridPane getMenuLayout() {
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(bounds.getHeight()/4, 0, 0, bounds.getWidth()/4));
        layout.setHgap(10);
        layout.setVgap(10);
        Label username = new Label("Your name (O):");
        Label opponent = new Label("Opponent's name (X):");
        username.setFocusTraversable(true);
        TextField field = new TextField();
        field.setPromptText("Guest");
        TextField field2 = new TextField();
        field2.setPromptText("Guest2");
        Label diff = new Label("AI difficulty:");
        ComboBox<String> difficulty = new ComboBox<>();
        difficulty.getItems().addAll("Easy", "Medium", "Unbeatable");
        difficulty.getSelectionModel().select(1);
        Button single = new Button("SINGLEPLAYER");
        single.setOnAction(e -> {
            Board board = new Board(field.getText().equals("") ? "Guest" : field.getText(), "AI", (bounds.getWidth() - Board.BOARD_SIZE) / 2, (bounds.getHeight() - Board.BOARD_SIZE) / 2);
            board.setAI(true);
            board.setDifficulty(difficulty.getSelectionModel().getSelectedItem());
            this.stage.getScene().setRoot(getPlayLayout(board));
        });
        Button multi = new Button("LOCAL MULTIPLAYER");
        multi.setOnAction(e -> {
            Board board = new Board(field.getText().equals("") ? "Guest" : field.getText(), field2.getText().equals("") ? "Guest2" : field2.getText(), (bounds.getWidth() - Board.BOARD_SIZE) / 2, (bounds.getHeight() - Board.BOARD_SIZE) / 2);
            this.stage.getScene().setRoot(getPlayLayout(board));
        });
        Button lan = new Button("LAN MULTIPLAYER");
        lan.setOnAction(e -> this.stage.getScene().setRoot(getLanLayout()));
        layout.add(username, 0, 0);
        layout.add(field, 0, 1);
        layout.add(opponent, 0, 2);
        layout.add(field2, 0, 3);
        layout.add(diff, 0, 4);
        layout.add(difficulty, 0, 5);
        layout.add(new Separator(), 0, 6);
        layout.add(single, 0, 7);
        layout.add(multi, 0, 8);
        layout.add(lan, 0, 9);
        return layout;
    }
    
    private void createServerConfig(GridPane pane){
        Label hostL = new Label("Host: ");
        hostL.setFocusTraversable(true);
        Label portL = new Label("Port: ");
        TextField host = new TextField();
        host.setPromptText("127.0.0.1");
        TextField port = new TextField();
        port.setPromptText("1234");
        Button create = new Button("Create room");
        create.setOnAction(e -> {
            Server server = new Server(host.getText().equals("") ? "127.0.0.1" : host.getText(), port.getText().equals("") ? 1234 : Integer.parseInt(port.getText()));
            if (server.isClosed()){
				FXActivity.getInstance().runOnUiThread(() -> Toast.makeText(FXActivity.getInstance(), "Server error", Toast.LENGTH_LONG).show());
                return;
            }
            // ... Make toast that indicates server creation
			FXActivity.getInstance().runOnUiThread(() -> Toast.makeText(FXActivity.getInstance(), "Server started", Toast.LENGTH_LONG).show());
        });
        pane.add(hostL, 0, 0);
        pane.add(portL, 0, 1);
        pane.add(host, 1, 0);
        pane.add(port, 1, 1);
        pane.add(create, 1, 2);
    }
    
    private void createClientConfig(GridPane pane){
        Label hostL = new Label("Host: ");
        Label portL = new Label("Port: ");
        TextField host = new TextField();
        host.setPromptText("127.0.0.1");
        TextField port = new TextField();
        port.setPromptText("1234");
        Label userL = new Label("Username: ");
        TextField user = new TextField();
        user.setPromptText("Guest");
        Button join = new Button("Join room");
        join.setOnAction(e -> {
            this.client = new Client(user.getText().equals("") ? "Guest" : user.getText(), host.getText().equals("") ? "127.0.0.1" : host.getText(), port.getText().equals("") ? 1234 : Integer.parseInt(port.getText()));
            if (this.client.isClosed()){
                this.client = null;
                return;
            }
            Board board = new Board("--", "--", (bounds.getWidth() - Board.BOARD_SIZE) / 2, (bounds.getHeight() - Board.BOARD_SIZE) / 2);
            board.waiting = true;
            board.canPlace = false;
            Thread loop = new Thread(() -> {
                while (true){
                    GameState gs = client.receive();
                    //System.out.println(gs);
                    this.gs = gs;
                    if (this.gs == null) return;
                    board.updateFromState(gs);
                }
            });
            loop.setDaemon(true);
            Thread pl = new Thread(() -> {
                System.out.println("Waiting for players...");
                this.gs = client.receive();
                if (this.gs == null) return;
                System.out.println("Ready!");
                //System.out.println(this.gs);
                board.updateFromState(this.gs);
                if (this.gs.names[0].equals(this.client.getUsername())){
                    board.playerLock = Player.PLAYER_O;
                    board.canPlace = true;
                } else if (this.gs.names[1].equals(this.client.getUsername())){
                    board.playerLock = Player.PLAYER_X;
                    board.canPlace = true;
                } else {
                    // Spectator
                    board.canPlace = false;
                }
                board.waiting = false;
                client.send(this.gs);
                loop.start();
            });
            pl.setDaemon(true);
            pl.start();
            this.stage.getScene().setRoot(getPlayLayout(board));
        });
        pane.add(hostL, 0, 4);
        pane.add(portL, 0, 5);
        pane.add(userL, 0, 6);
        pane.add(host, 1, 4);
        pane.add(port, 1, 5);
        pane.add(user, 1, 6);
        pane.add(join, 1, 7);
    }
    
    private GridPane getLanLayout(){
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(bounds.getHeight()/4, 0, 0, bounds.getWidth()/6));
        pane.setHgap(10);
        pane.setVgap(10);
		
        // Server configuration
        createServerConfig(pane);
        pane.add(new Separator(), 0, 3, 2, 1);
        // Client connection
        createClientConfig(pane);
        
        return pane;
    }

    private StackPane getPlayLayout(Board gotBoard) {
        StackPane pane = new StackPane();
        Canvas canvas = new Canvas(bounds.getWidth(), bounds.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Board board = gotBoard;
        canvas.setOnMousePressed(e -> {
            if (!board.canPlace) return;
            if (board.isGameFinished()){
                board.clear();
                board.drawPlayCanvas(gc);
                if (this.gs != null){
                    this.gs.board = board.getBoard();
                    this.gs.gameFinished = board.isGameFinished();
                    this.gs.currentMessage = board.getMessage();
                    this.gs.startTime = board.getStartTime();
                    this.client.send(this.gs);
                }
            } else {
                double x = e.getX();
                double y = e.getY();
                int boardX = (int) ((x - board.getX()) / Board.CELL_SIZE);
                int boardY = (int) ((y - board.getY()) / Board.CELL_SIZE);
                if (boardX < 0 || boardX >= 3 || boardY < 0 || boardY >= 3) {
                    return;
                }
                board.place(board.getCurrentPlayer(), boardX, boardY);
                if (this.gs != null){
                    this.gs.board = board.getBoard();
                    this.gs.gameFinished = board.isGameFinished();
                    this.gs.currentMessage = board.getMessage();
                    this.client.send(this.gs);
                }
                board.drawPlayCanvas(gc);
            }
        });
        board.drawPlayCanvas(gc);
        Timeline loop = new Timeline(new KeyFrame(Duration.millis(100), e -> board.drawPlayCanvas(gc)));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
        pane.getChildren().add(canvas);
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception{
		if (Build.VERSION.SDK_INT >= 29){
			Method forName = Class.class.getDeclaredMethod("forName", String.class);
			Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
			Class vmRuntimeClass = (Class) forName.invoke(null, "dalvik.system.VMRuntime");
			Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
			Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[] { String[].class} );
			Object vmRuntime = getRuntime.invoke(null);
			setHiddenApiExemptions.invoke(vmRuntime, (Object[])new String[][]{new String[]{"L"}});
		}
        this.stage = stage;
        Scene scene = new Scene(getMenuLayout(), bounds.getWidth(), bounds.getHeight());
        scene.setOnKeyPressed(e -> {
            //System.out.println(e.getCode());
            if (e.getCode() == KeyCode.ESCAPE){
                scene.setRoot(getMenuLayout());
                this.gs = null;
                if (this.client != null){
                    this.client.close();
                    this.client = null;
                }
            }
        });
        scene.getStylesheets().add(getClass().getClassLoader().getResource("styles.css").toExternalForm());
        stage.setTitle("TicTacToe");
        stage.setScene(scene);
        stage.show();
    }
}
