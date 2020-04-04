package org.orangepalantir.dominoes;


import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.orangepalantir.dominoes.players.*;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Keeps track of the game state.
 *
 * Five years later and I look at this class and think. What was I thinking.
 *
 * Created by melkor on 4/8/15.
 */
public class DominoGame{
    static int POINTS_TO_WIN = 150;
    static int INITIAL_DOMINOES = 5;
    DominoSet set;
    AtomicBoolean running = new AtomicBoolean(false);
    Executor gameLoop = Executors.newSingleThreadExecutor();
    List<AvailableMove> moveLog = Collections.synchronizedList(new ArrayList<>());
    List<GameState> stateLog = Collections.synchronizedList(new ArrayList<>());
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
    private Monitor monitor;
    List<GameObserver> observers = new ArrayList<>();
    public static DominoGame startSixesGame(){
        DominoGame game = new DominoGame();
        game.set = DominoSet.doubleSixes();
        game.running.set(true);
        game.mode = GameMode.GetPlayers;
        return game;
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

    /**
     * Goes through all of the available moves and finds the exposed number.
     *
     * @return the exposed number.
     */
    public List<Integer> getExposedNumbers(){
        return moves.stream().filter(
                AvailableMove::hasBase
            ).map(
                AvailableMove::exposedNumber
            ).collect(
                Collectors.toList()
            );
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
    public void setMonitor(Monitor m){
        monitor = m;
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
            moveLog.add(m);
            moves.remove(m);
            moves.addAll(m.performMove(d));
            played.add(d);
            stateLog.add(new GameState(this));
        }

        return validMove;
    }
    public void addObserver(GameObserver o){
        observers.add(o);
    }
    public void update(){
        for(GameObserver observer: observers){
            observer.update();
        }
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

                        while(p.getDominoCount()<DominoGame.INITIAL_DOMINOES){
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
                    update();
                    break;
                case EndOfHand:
                    endOfHandScore();
                    break;
                case EndOfGame:
                    playing=false;
                    update();
                    monitor.waitForInput();
                    break;
            }



        }


    }
    public void addPlayer(Player p){
        players.add(p);
        scoreBoard.addPlayer(p);
    }

    private void startGame(){
        humanPlayer = new HumanPlayer(this);
        scoreBoard.addPlayer(humanPlayer);
        Player bai = new RandomAI(this);
        scoreBoard.addPlayer(bai);
        Player bai2 = new ImprovedBasic(this, 1, 3, 0);
        scoreBoard.addPlayer(bai2);
        players.add(humanPlayer);
        players.add(bai);
        players.add(bai2);

        gameLoop.execute(()->gameLoop());
    }

    public void startNewGame(){

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
        Player finisher = null;
        for(Player p: players){
            List<Domino> dees = p.returnDominos();
            int v = dees.stream().mapToInt(d->d.A+d.B).sum();
            if(v<min){
                min = v;
                winner = p;
            }
            sum += v;
            if(dees.size()==0){
                finisher = p;
            }
            dees.forEach(d->{d.setFaceUp(false);set.returnDomino(d);});
        }
        if(finisher!=null){
            //In case somebody has 00 they could be the winner.
            winner = finisher;
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
        postMessage("Player " + winner + "has won!");
        checkObservers();
        if(!finished) {
            dealHand();
        } else{
            mode=GameMode.EndOfGame;
        }

    }

    private void postMessage(String s) {
        for(GameObserver observer: observers){
            observer.postMessage(s);
        }
    }

    private void checkObservers(){
        for(GameObserver observer: observers){
            observer.waitForInput();
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
    void shutdown(){
        SHUTDOWN=true;
    }
    Rectangle passButton = new Rectangle(600, 400, 30, 30);


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
    int winning = DominoGame.POINTS_TO_WIN;
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

        return won;
    }
    public void resetScores(){
        for(Score s: scores.values()){
            if(s.getValue()==winning){
                s.increaseGame();
            }
            s.tallies.clear();
            s.total=0;
        }
    }

    public void setCurrentTotal(int v){
        currentTotal = v;
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