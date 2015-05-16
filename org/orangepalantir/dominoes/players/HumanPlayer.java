package org.orangepalantir.dominoes.players;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by msmith on 4/9/15.
 */
public class HumanPlayer implements Player{
    DominoGame game;
    List<Domino> dominos = new ArrayList<>(7);
    LinkedBlockingQueue<Domino> toTake = new LinkedBlockingQueue<>(1);
    final Object lock = new Object();

    int selected = -1;
    int play = -1;

    int count = 0;
    public HumanPlayer(DominoGame game){
        this.game = game;
    }

    public int getDominoCount(){
        return count;
    }

    @Override
    public List<Domino> returnDominos() {
        List<Domino> ret = dominos.stream().filter(w->w!=null).collect(Collectors.toList());
        dominos.clear();
        count = 0;
        return ret;
    }

    public void draw(GraphicsContext gc){
        for(Domino d: dominos){
            if(d!=null) {
                d.draw(gc);
            }
        }
        if(selected>-1&&selected<dominos.size()){
            Domino d = dominos.get(selected);
            if(d!=null){
                Bounds b  = d.getBounds().getBoundsInParent();
                gc.setStroke(Color.GREEN);
                gc.setLineWidth(2);
                gc.strokeRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
            } else{
                selected=-1;
            }

        }
    }

    public void choosePiece(Domino d){

        if(toTake.size()==0) {
            synchronized(lock){
                toTake.add(d);
                lock.notifyAll();
            }
        }

    }
    void addPiece(Domino d){
        int i;
        for(i = 0; i<dominos.size(); i++){
            Domino a = dominos.get(i);
            if(a==null){
                dominos.set(i, d);
                double y = 500 - (i/4)*1.2*d.width;
                double x = 300 + (i%4)*1.2*d.length;
                d.setPosition(x, y);
                d.setAngle(Math.PI / 2);
                count += 1;
                d.setFaceUp(true);
                return;
            }
        }
        dominos.add(d);
        double y = 500 - (i/4)*1.2*d.width;
        double x = 300 + (i%4)*1.2*d.length;
        d.setPosition(x, y);
        d.setAngle(Math.PI / 2);
        count += 1;
        d.setFaceUp(true);


    }
    void getPieces(){
        try {
            Domino d = toTake.take();
            if(game.takePieceFromBoneYard(d)){
                addPiece(d);
            }
        } catch (InterruptedException e) {
            //its ok.
        }

    }

    public boolean selectDomino(double x, double y){
        for(int i = 0; i<dominos.size(); i++){
            Domino d = dominos.get(i);
            if(d!=null){
                if(d.contains(x, y)){
                    selected = i;
                    synchronized(lock) {
                        lock.notifyAll();
                    }
                    return true;
                }
            }
        }
        return false;
    }
    public void setMove(int move){
        if(selected>=0) {

            play = move;
            synchronized (lock) {
                lock.notifyAll();
            }

        }
    }
    boolean passed=false;
    public void pass(){
        game.pass();

        passed=true;
        synchronized (lock){
            lock.notifyAll();
        }
    }

    void playDomino(){
        boolean playing = true;
        while(playing&&!Thread.interrupted()){

            if(selected<0||play<0){

                synchronized(lock) {
                    if(toTake.size()>0){
                        getPieces();
                    }
                    try {
                        lock.wait();
                        if(passed){
                            passed=false;
                            playing=false;
                        }
                    } catch (InterruptedException e) {
                        //game ended.
                        playing=false;
                    }
                }

            } else {

                Domino d = dominos.get(selected);
                dominos.set(selected, null);
                count--;
                if (game.performMove(d, play)) {
                    playing = false;
                } else {
                    //put it back.
                    dominos.set(selected, d);
                    count++;
                }

                selected = -1;
                play = -1;
            }

        }

    }
    @Override
    public void makeMove() {
        switch(game.getMode()){
            case ChoosePieces:
                getPieces();
                break;
            case PlayGame:
                playDomino();
                break;
        }
    }


}
