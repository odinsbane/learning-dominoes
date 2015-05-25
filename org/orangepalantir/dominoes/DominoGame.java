package org.orangepalantir.dominoes;


import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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
    int passCounter = 0;
    Player next = null;
    Monitor monitor;
    public static DominoGame startSixesGame(){
        DominoGame game = new DominoGame();
        game.set = DominoSet.doubleSixes();
        game.running.set(true);
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
            d.setFaceUp(false);
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
                startGame();
                break;
            case PlayGame:
                if(choosePiece(x,y)){
                    update();
                } else if(humanPlayer.selectDomino(x,y)){
                    update();
                } else if(selectPlay(x, y)){
                    update();
                }
                checkControls(x,y);
                break;
            default:
                //waiting.
                break;
        }
        monitor.input();
    }

    void gameLoop(){
        dealHand();
        boolean playing = true;
        while(playing) {

            switch(mode){
                case ChoosePieces:
                    for(Player p: players){

                        while(p.getDominoCount()<7){
                            p.makeMove();
                            if(SHUTDOWN) return;
                            update();
                        }

                    }
                    mode = GameMode.PlayGame;
                    if(next==null) {
                        int highest = -1;

                        for (Player p : players) {
                            List<Domino> dominos = p.getDominos();
                            for (Domino d : dominos) {
                                if (d.A == d.B && highest < d.A) {
                                    highest = d.A;
                                    next = p;
                                }
                            }
                        }
                        if (highest >= 0) {
                            moves.add(AvailableMove.firstMoveOfGame(400, 300, highest));
                        } else {
                            moves.add(new AvailableMove(400, 300));
                            next = players.get(0);
                        }
                    } else{
                        moves.add(new AvailableMove(400, 300));
                    }

                    update();
                case PlayGame:
                    Player p = next;
                    int i = players.indexOf(p);
                    next = players.get((i+1)%players.size());

                    validMove=false;
                    int passed = passCounter;
                    while (!validMove) {
                        p.makeMove();
                        if (SHUTDOWN) return;
                        update();
                    }
                    if(passCounter>passed){
                        //pass count increasing.
                    } else{
                        passCounter=0;
                    }
                    calculateScore(p);
                    if(p.getDominoCount()==0||passCounter==players.size()){
                        mode=GameMode.EndOfHand;
                    }
                    break;
                case EndOfHand:
                    endOfHandScore();
                    break;
                case EndOfGame:
                    //playing=false;
                    //SHUTDOWN=true;
                    update();
                    monitor.waitForInput();
                    startNewGame();
                    break;
            }



        }

    }

    private void startGame(){
        humanPlayer = new HumanPlayer(this);
        scoreBoard.addPlayer(humanPlayer);
        BasicAI bai = new BasicAI(this);
        scoreBoard.addPlayer(bai);
        BasicAI bai2 = new BasicAI(this);
        scoreBoard.addPlayer(bai2);
        players.add(humanPlayer);
        players.add(bai);
        players.add(bai2);
        monitor = new HumanMonitor();

        gameLoop = new Thread(()->gameLoop());
        gameLoop.start();
    }

    private void startNewGame(){

        players.forEach(p->p.returnDominos().forEach(set::returnDomino));

        played.forEach(set::returnDomino);
        played.clear();
        boneYard.forEach(set::returnDomino);
        boneYard.clear();
        passCounter = 0;
        moves.clear();
        scoreBoard.resetScores();
        next=null;
        dealHand();
    }

    private void dealHand(){
        fillBoneYard();
        spinner=false;
        mode = GameMode.ChoosePieces;
        update();
    }

    private void endOfHandScore(){
        int min = Integer.MAX_VALUE;
        int sum = 0;
        Player winner=null;
        for(Player p: players){
            List<Domino> dees = p.returnDominos();
            int v = dees.stream().mapToInt(d->d.A+d.B).sum();
            if(v<min){
               min = v;
                winner = p;
            }
            sum += v;
            dees.forEach(d->{d.setFaceUp(false);set.returnDomino(d);});
        }

        sum = sum - 2*min;
        sum = (sum - sum%5);
        boolean finished = false;
        if(sum>0) {
            finished = scoreBoard.score(winner, sum);
        }
        played.forEach(set::returnDomino);
        played.clear();
        boneYard.forEach(set::returnDomino);
        boneYard.clear();
        passCounter = 0;
        moves.clear();
        spinner = false;
        next=winner;
        if(!finished) {
            dealHand();
        } else{
            mode=GameMode.EndOfGame;
        }
    }


    private void calculateScore(Player p) {
        int tally = 0;
        for(AvailableMove mv: moves){
            tally += mv.getScore();
        }

        if(tally>0&&tally%5==0){
            boolean won = scoreBoard.score(p, tally);
            if(won){
                mode = GameMode.EndOfGame;
            }
        }

        scoreBoard.setCurrentTotal(tally);

    }

    public void update(){
        Platform.runLater(this::repaint);
    }

    void shutdown(){
        SHUTDOWN=true;
        gameLoop.interrupt();
    }

    void drawMoves(){
        synchronized(moves) {
            for (AvailableMove m : moves) {
                m.draw(gc);
            }
        }
    }

    void drawPlayed(){
        played.forEach(d -> d.draw(gc));
    }



    void repaint(){
        drawBoard();
        drawBoneYard();
        humanPlayer.draw(gc);
        drawPlayed();
        drawMoves();
        drawControls();
        scoreBoard.draw(gc);
    }

    Rectangle passButton = new Rectangle(600, 400, 30, 30);
    public void drawControls(){
        gc.setFill(Color.BLACK);

        gc.fillRect(600, 400, 30, 30);
        gc.setStroke(Color.WHITE);
        gc.setFont(new Font(10));
        gc.setLineWidth(1);
        gc.strokeText("pass", 600, 421);
    }

    public boolean checkControls(double x, double y){
        if(passButton.contains(x,y)){
            humanPlayer.pass();
            return true;

        }
        return false;
    }

    public void pass() {
        passCounter++;
        validMove=true;
    }
}

class PlayerScores{
    int winning = 150;
    Map<Player, Score> scores = new HashMap<>();
    int currentTotal = 0;
    public void addPlayer(Player p){
        scores.put(p, new Score());
    }
    public boolean score(Player p, int value){
        if(value<=0||value%5!=0){
            throw new IllegalArgumentException("scores must be multiples of 5 greater than 0.");
        }

        Score s = scores.get(p);
        if(s.getValue()+value>=winning){
            value = winning - s.getValue();
        }

        int marks = value/5;
        s.addScore(marks);
        boolean won = s.getValue()==winning;
        if(won){
            s.increaseGame();
        }
        return won;
    }
    public void resetScores(){
        for(Score s: scores.values()){
            s.tallies.clear();
            s.total=0;
        }
    }

    public void setCurrentTotal(int v){
        currentTotal = v;
    }

    public void draw(GraphicsContext gc) {
        //gc.setFill(Color.BLACK);
        //gc.fillRect(0, 0, 800, 200);
        gc.setFont(new Font(12));
        gc.setStroke(Color.WHITE);
        gc.strokeText("total: " + currentTotal, 5, 17);
        int i = 1;
        for(Player p: scores.keySet() ){
            gc.strokeText("player: " + i, 100*i, 17);
            gc.strokeText("total: " + scores.get(p).getValue(), 100*i, 30);
            gc.strokeText("dominos: " + p.getDominoCount(), 100*i, 43);
            gc.strokeText("games: " + scores.get(p).games, 100*i, 56);
            i++;
        }
    }
}

class Score{
    List<Tally> tallies = new ArrayList<>();
    int total;
    int games;
    public int getValue(){
        return total*5;
    }
    public void increaseGame(){
        games++;
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

interface Monitor{
    public void waitForInput();
    public void input();
}

class HumanMonitor implements Monitor{
    public void input(){
        synchronized (this){
            notifyAll();
        }
    }
    public void waitForInput(){
        synchronized(this){
            try {
                wait();
            } catch (InterruptedException e) {
                //if the game is ended.
            }
        }
    }
}