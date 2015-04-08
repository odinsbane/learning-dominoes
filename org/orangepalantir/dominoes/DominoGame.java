package org.orangepalantir.dominoes;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps track of the game state.
 *
 * Created by melkor on 4/8/15.
 */
public class DominoGame{
    DominoSet set;
    AtomicBoolean running = new AtomicBoolean(false);
    private GraphicsContext gc;

    Random ng = new Random();

    Set<Domino> boneYard = new HashSet<>();

    GameMode mode;

    public static DominoGame startSixesGame(){
        DominoGame game = new DominoGame();
        game.set = DominoSet.doubleSixes();
        game.running.set(true);
        game.fillBoneYard();
        game.mode = GameMode.GetPlayers;
        return game;
    }

    public void setGraphicsContext2D(GraphicsContext graphicsContext2D) {

        this.gc = graphicsContext2D;
        drawBoard();
        drawBoneYard();
    }

    public void fillBoneYard(){
        while(set.hasDominos()){
            Domino d = set.getRandomDomino();
            d.setPosition(ng.nextDouble()*75 + 60, ng.nextDouble()*400 + 100);
            d.setAngle(2*ng.nextDouble()*Math.PI);
            boneYard.add(d);
        }
    }

    void drawBoard(){
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        gc.setFill(Color.AQUAMARINE);

        gc.fillRect(0,0,600, 600);
        gc.strokeRect(0, 0, 600, 600);
    }

    void drawBoneYard(){

        boneYard.forEach(d->d.draw(gc));
    }

    void choosePiece(double x, double y){
        //go through the pieces in the bone yard and select a piece.
    }

    public void clicked(double x, double y){
        switch(mode){
            case ChoosePieces:
                choosePiece(x, y);
                break;
            case PlayGame:
            case GetPlayers:
            default:
                //waiting.
                break;
        }
    }
    
}

enum GameMode{
    GetPlayers, ChoosePieces, PlayGame;

}