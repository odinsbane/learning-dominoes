package org.orangepalantir.dominoes.scenes;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.orangepalantir.dominoes.DominoDisplay;
import org.orangepalantir.dominoes.DominoGame;

public class GameScene {
    @FXML
    Canvas canvas;
    GraphicsContext gc;
    DominoDisplay display;
    DominoGame game;

    public void startGame(DominoGame game){
        this.game = game;
        display.setGame(game);
        game.play();
    }
    public void keyPressed(KeyEvent evt){
        display.keyPressed(evt);
    }

    public void setGameFinishedCallback(Runnable r){
        display.setGameFinisher(r);
    }

    public void initialize(){
        //canvas.setScaleX(2);
        //canvas.setScaleY(2);
        gc = canvas.getGraphicsContext2D();
        display = new DominoDisplay(gc);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            display.clicked(mouseEvent.getX(), mouseEvent.getY());
        });

    }

}
