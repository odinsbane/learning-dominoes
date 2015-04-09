package org.orangepalantir.dominoes;


import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.orangepalantir.dominoes.players.HumanPlayer;
import org.orangepalantir.dominoes.players.Player;

import java.util.*;
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
    Thread gameLoop;

    List<AvailableMove> moves = new ArrayList<>();
    List<Domino> played = new ArrayList<>();

    Random ng = new Random();

    Set<Domino> boneYard = Collections.synchronizedSet(new HashSet<>());
    List<Player> players = new ArrayList<>(4);
    HumanPlayer humanPlayer;
    GameMode mode;
    boolean SHUTDOWN = false;
    boolean validMove = false;

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
    public GameMode getMode(){
        return mode;
    }
    void drawBoard(){
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        gc.setFill(Color.AQUAMARINE);

        gc.fillRect(0,0,600, 600);
        gc.strokeRect(0, 0, 600, 600);
    }

    void drawBoneYard(){

        boneYard.forEach(d -> d.draw(gc));
    }

    boolean choosePiece(double x, double y){
        //go through the pieces in the bone yard and select a piece.
        for(Domino d: boneYard){
            if(d.contains(x,y)) {
                humanPlayer.choosePiece(d);
                return true;
            }
        }

        return false;
    }

    public boolean takePieceFromBoneYard(Domino d){
        if(boneYard.contains(d)){
            boneYard.remove(d);
            return true;
        }
        return false;
    }

    boolean selectPlay(double x, double y){
        int i = 0;
        for(AvailableMove move: moves){
            if(move.contains(x,y)){
                humanPlayer.setMove(i);
                return true;
            }
            i++;
        }

        return false;
    }

    public boolean performMove(Domino d, int moveIndex){
        AvailableMove m = moves.get(moveIndex);
        if(m.isValidMove(d)){
            validMove=true;
            moves.remove(m);
            moves.addAll(m.performMove(d));
            played.add(d);
        }

        return validMove;
    }

    public void clicked(double x, double y){
        switch(mode){
            case ChoosePieces:
                choosePiece(x, y);
                break;
            case GetPlayers:
                mode = GameMode.ChoosePieces;
                humanPlayer = new HumanPlayer(this);
                players.add(humanPlayer);
                gameLoop = new Thread(()->gameLoop());
                gameLoop.start();
                break;
            case PlayGame:
                if(choosePiece(x,y)){
                    update();
                } else if(humanPlayer.selectDomino(x,y)){
                    update();
                } else if(selectPlay(x, y)){
                    update();
                }
                break;
            default:
                //waiting.
                break;
        }
    }

    void gameLoop(){

        for(Player p: players){

            while(p.getDominoCount()<7){
                p.makeMove();
                if(SHUTDOWN) return;
                update();
            }

        }
        System.out.println("every player has pieces");
        mode = GameMode.PlayGame;
        moves.add(new AvailableMove(450, 250, null, -1));
        update();
        for(Player p: players){
            while(!validMove){
                p.makeMove();
                if(SHUTDOWN) return;
                update();
            }
        }



    }

    public void update(){
        Platform.runLater(this::repaint);
    }

    void shutdown(){
        SHUTDOWN=true;
        gameLoop.interrupt();
    }

    void drawMoves(){
        for(AvailableMove m: moves){
            m.draw(gc);
        }
    }

    void drawPlayed(){
        played.forEach(d->d.draw(gc));
    }

    void repaint(){
        drawBoard();
        drawBoneYard();
        humanPlayer.draw(gc);
        drawPlayed();
        drawMoves();

    }
    
}
