package org.orangepalantir.dominoes;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.orangepalantir.dominoes.players.HumanPlayer;
import org.orangepalantir.dominoes.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by melkor on 7/5/15.
 */
public class DominoDisplay implements GameObserver{
    GraphicsContext gc;
    DominoGame game;
    HumanPlayer player;
    List<String> messages = new ArrayList<>();

    public DominoDisplay(GraphicsContext gc){

        this.gc = gc;

    }

    public void setGame(DominoGame g) {
        this.game = g;
        g.addObserver(this);
    }

    public void clicked(double x, double y) {
        synchronized (this){
            notifyAll();
        }
        game.clicked(x, y);

    }

    void drawBoard(){
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        gc.setFill(Color.AQUAMARINE);

        gc.fillRect(0,0,800, 600);
        gc.strokeRect(0, 0, 800, 600);
    }

    void drawBoneYard(){
        synchronized(game.boneYard) {
            game.boneYard.forEach(d -> d.draw(gc));
        }
    }

    void drawMoves(){
        final List<AvailableMove> moves = game.moves;
        synchronized(moves) {
            for (AvailableMove m : moves) {
                m.draw(gc);
            }
        }
    }

    public void drawControls(){
        gc.setFill(Color.BLACK);

        gc.fillRect(600, 400, 30, 30);
        gc.setStroke(Color.WHITE);
        gc.setFont(new Font(10));
        gc.setLineWidth(1);
        gc.strokeText("pass", 600, 421);
    }
    final static String PLAYAGAIN = "Play Again";
    final static String QUIT = "Quit";
    public void update(){
        if(player==null) {
            if (game.players.size()>0 && game.players.get(0) instanceof HumanPlayer) {
                player = (HumanPlayer) game.players.get(0);
            }
        }
        if(game.getMode()==GameMode.EndOfGame){
            game.startNewGame();
            Platform.runLater(()->{
                ChoiceDialog<String> playAgain = new ChoiceDialog<>(PLAYAGAIN, PLAYAGAIN, QUIT);
                String option = playAgain.showAndWait().orElseGet(()->"none");
                System.out.println(option);
                switch(option){
                    case PLAYAGAIN:
                        break;
                    default:
                        game.mode = GameMode.GetPlayers;
                }
                game.gameLoop.execute(game::gameLoop);
            });
        }
        Platform.runLater(this::repaint);

    }

    @Override
    public void postMessage(String message) {
        messages.add(message);
    }

    @Override
    public void waitForInput() {
        synchronized(this){
            try{
                wait();
            } catch(Exception e){
                game.shutdown();
            }
            messages.clear();
        }
    }

    void drawPlayed(){
        game.played.forEach(d -> d.draw(gc));
    }

    void repaint(){
        drawBoard();
        drawBoneYard();
        drawHumanPlayer();
        drawControls();
       drawScoreBoard();
        drawMessages();
        drawMoves();
        drawPlayed();

    }

    public void drawMessages(){
        gc.setStroke(Color.NAVAJOWHITE);
        int i = 0;
        for(String message: messages){
            gc.strokeText(message, 200, 72 + 15*i);
            i++;
        }
    }

    public void drawScoreBoard() {
        PlayerScores scoreBoard = game.scoreBoard;
        //gc.setFill(Color.BLACK);
        //gc.fillRect(0, 0, 800, 200);
        gc.setFont(new Font(12));
        gc.setStroke(Color.WHITE);
        gc.strokeText("total: " + scoreBoard.currentTotal, 5, 17);
        int i = 1;
        Map<Player, Score> scores = scoreBoard.scores;
        for(Player p: scores.keySet() ){
            gc.strokeText("player: " + p, 100*i, 17);
            gc.strokeText("total: " + scores.get(p).getValue(), 100*i, 30);
            gc.strokeText("dominos: " + p.getDominoCount(), 100*i, 43);
            gc.strokeText("games: " + scores.get(p).games, 100*i, 56);
            if(game.next==p){
                gc.setFill(Color.RED);
                gc.fillOval(100 * i + 40, 60, 20, 20);
                gc.setFill(Color.WHITE);
            }
            i++;
        }
    }
    public void drawHumanPlayer(){
        player.draw(gc);
    }


    public void keyPressed(KeyEvent keyEvent) {
        switch(keyEvent.getCode()){
            case LEFT:
                shift(-5, 0);
                break;
            case RIGHT:
                shift(5, 0);
                break;
            case UP:
                shift(0,-5);
                break;
            case DOWN:
                shift(0, 5);
                break;
            default:
        }
    }

    public void shift(int dx, int dy){
        game.played.forEach(d ->{
            double[] p = d.getPosition();
            d.setPosition(p[0]+dx, p[1]+dy);
        });
        update();
    }
}
