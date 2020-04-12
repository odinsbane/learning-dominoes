package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.AvailableMove;
import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Plays a random valid move if any are available.
 *
 * Created by odinsbane on 7/5/15.
 */
public class RandomAI extends AbstractAI {
    public RandomAI(){
        super();
    }
    @Override
    public void playDomino() {
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
                return;
            }
            dominos.add(d);
            return;
        } else{
            GoodMove gm = good.get(ng.nextInt(good.size()));
            game.performMove(gm.domino, gm.move);
            dominos.remove(gm.domino);
            gm.domino.setFaceUp(true);
        }
    }
    @Override
    public String toString(){
        return "random";
    }
}
