package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.AvailableMove;
import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by melkor on 7/5/15.
 */
abstract public class AbstractAI implements Player {
    DominoGame game;
    List<Domino> dominos = new ArrayList<>(7);
    Random ng = new Random();
    public AbstractAI(){

    }
    public void setGame(DominoGame game){
        this.game = game;
    }

    public void getPieces(){
        Domino d = game.getRandomBone();
        if(d==null){
            throw new RuntimeException("Domino is null!");
        } else {
            dominos.add(d);
        }
    }
    class GoodMove{
        Domino domino;
        AvailableMove move;
        GoodMove(Domino d, AvailableMove m){
            domino = d;
            move = m;
        }
    }
    abstract public void playDomino();

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

    @Override
    public int getDominoCount() {
        return dominos.size();
    }

    @Override
    public List<Domino> returnDominos() {
        List<Domino> ret = dominos.stream().collect(Collectors.toList());
        dominos.clear();
        return ret;
    }

    @Override
    public List<Domino> getDominos() {
        List<Domino> ret = dominos.stream().collect(Collectors.toList());
        return ret;
    }

    @Override
    public String toString(){
        return "abstract";
    }
}
