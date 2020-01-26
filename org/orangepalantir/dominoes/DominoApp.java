package org.orangepalantir.dominoes;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Created by melkor on 4/8/15.
 */
public class DominoApp extends Application {
    DominoGame game;
    DominoDisplay display;
    GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Learning Dominos!");

        Canvas canvas = new Canvas(800, 600);
        canvas.setScaleX(2);
        canvas.setScaleY(2);
        gc = canvas.getGraphicsContext2D();
        display = new DominoDisplay(gc);




        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();
        primaryStage.setOnHidden(evt -> {
            if (game != null) {
                game.shutdown();
            }
        });

        VBox box = new VBox();
        ScrollPane pane = new ScrollPane(box);
        Stage logStage = new Stage();
        logStage.setScene(new Scene(pane, 400, 600));
        logStage.show();
        PlayLog log = new PlayLog(box);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if(game==null){
                game = DominoGame.startSixesGame();
                game.setMonitor(new HumanMonitor());
                display.setGame(game);
                log.setGame(game);

            } else{
                display.clicked(mouseEvent.getX(), mouseEvent.getY());
            }
        });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            display.keyPressed(keyEvent);
        });

    }



    public static void main(String[] args) {
        launch(args);
    }

}
