package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.AvailableMove;
import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by msmith on 5/13/15.
 */
public class BasicAI implements Player {
    DominoGame game;
    List<Domino> dominos = new ArrayList<>(7);
    Random ng = new Random();
    public BasicAI(DominoGame game){
        this.game = game;
    }

    public void getPieces(){
        dominos.add(game.getRandomBone());
    }
    class GoodMove{
        Domino domino;
        AvailableMove move;
        GoodMove(Domino d, AvailableMove m){
            domino = d;
            move = m;
        }
    }
    public void playDomino(){
        List<AvailableMove> moves = game.getAvailableMoves();
        int current = 0;
        List<GoodMove> good = new ArrayList<>();
        for(AvailableMove move: moves){
            current += move.getScore();
            for(Domino d: dominos){
                if(move.isValidMove(d)){
                    good.add(new GoodMove(d,move));
                }
            }
        }
        if(good.size()==0){
            Domino d = game.getRandomBone();
            if(d==null){
                game.pass();
            }
            dominos.add(d);
            return;
        }
        int max = -1;
        int i = 0;
        int dex = 0;
        for(GoodMove gm: good){
            AvailableMove m = gm.move;
            Domino d = gm.domino;
            int ifPlayed = current + m.changeIfCovered(d);
            if(ifPlayed%5==0){
                //scoring move, check if best.
                if(ifPlayed>max){
                    max = ifPlayed;
                    dex =  i;
                }
            }
            i++;
        }
        GoodMove gm = max>0?good.get(dex):good.get(ng.nextInt(good.size()));
        game.performMove(gm.domino, gm.move);
        dominos.remove(gm.domino);
        gm.domino.setFaceUp(true);
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
}
