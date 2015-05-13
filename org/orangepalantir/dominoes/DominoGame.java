package org.orangepalantir.dominoes;


import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.orangepalantir.dominoes.players.BasicAI;
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

    List<AvailableMove> moves = Collections.synchronizedList(new ArrayList<>());
    List<Domino> played = Collections.synchronizedList(new ArrayList<>());

    Random ng = new Random();

    Set<Domino> boneYard = Collections.synchronizedSet(new HashSet<>());
    List<Player> players = new ArrayList<>(4);
    HumanPlayer humanPlayer;
    GameMode mode;
    boolean SHUTDOWN = false;
    boolean validMove = false;
    boolean spinner = false;

    PlayerScores scoreBoard = new PlayerScores();

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

    public List<AvailableMove> getAvailableMoves(){
        return Collections.unmodifiableList(moves);
    }

    void drawBoard(){
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        gc.setFill(Color.AQUAMARINE);

        gc.fillRect(0,0,800, 600);
        gc.strokeRect(0, 0, 800, 600);
    }

    void drawBoneYard(){
        synchronized(boneYard) {
            boneYard.forEach(d -> d.draw(gc));
        }
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

    /**
     * Gets a random domino from the boneyard. If there is one available. Otherwise returns null.
     *
     * @return
     */
    public Domino getRandomBone(){
        if(boneYard.size()==0){
            return null;
        }
        int i = ng.nextInt(boneYard.size());
        Iterator<Domino> iter = boneYard.iterator();
        for(int j = 0; j<i; j++){
            iter.next();
        }
        Domino d = iter.next();
        synchronized(boneYard) {
            iter.remove();
        }
        return d;

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
            if(!spinner){
                if(d.A==d.B){
                    d.setSpinner(true);
                    spinner=true; //no more spinners this game.
                }
            }
            moves.remove(m);
            moves.addAll(m.performMove(d));
            played.add(d);
        }

        return validMove;
    }

    public boolean performMove(Domino d, AvailableMove m){
        return performMove(d, moves.indexOf(m));
    }

    public void clicked(double x, double y){
        switch(mode){
            case ChoosePieces:
                choosePiece(x, y);
                break;
            case GetPlayers:
                mode = GameMode.ChoosePieces;
                humanPlayer = new HumanPlayer(this);
                scoreBoard.addPlayer(humanPlayer);
                BasicAI bai = new BasicAI(this);
                scoreBoard.addPlayer(bai);

                players.add(humanPlayer);
                players.add(bai);

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
        moves.add(new AvailableMove(450, 250));
        update();
        boolean playing = true;
        while(playing) {
            for (Player p : players) {
                validMove=false;
                while (!validMove) {
                    System.out.println("waiting...");
                    p.makeMove();
                    if (SHUTDOWN) return;
                    update();
                }
                calculateScore(p);
                if(p.getDominoCount()==0){
                    playing=false;
                    break;
                }
            }
        }


    }

    private void calculateScore(Player p) {
        int tally = 0;
        for(AvailableMove mv: moves){
            tally += mv.getScore();
        }

        if(tally%5==0){
            System.out.println("Score: " + tally);
        } else{
            System.out.println("mark: " + tally);
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

class PlayerScores{

    Map<Player, Score> scores = new HashMap<>();

    public void addPlayer(Player p){
        scores.put(p, new Score());
    }
    public void score(Player p, int value){
        if(value<=0||value%5!=0){
            throw new IllegalArgumentException("scores must be multiples of 5 greater than 0.");
        }
        int marks = value/5;
        Score s = scores.get(p);
        s.addScore(marks);
    }

}

class Score{
    List<Tally> tallies = new ArrayList<>();
    int total;
    public int getValue(){
        return total*5;
    }

    public void addScore(int marks){

        total += marks;
        if(tallies.size()!=0){
            Tally last = tallies.get(tallies.size()-1);
            if(last.value<2){
                last.add(1);
                marks = marks-1;
            }
            int tens = marks/2;
            for(int i = 0; i<tens; i++){
                tallies.add(new Tally(2));
            }
            if(marks%2>0){
                tallies.add(new Tally(1));
            }
        }

    }

}

class Tally{
    int value;
    int count = 0;
    public Tally(int v){
        value=v;
        count = 1;
    }

    public void add(int v){
        value+=v;
        count++;
    }

}