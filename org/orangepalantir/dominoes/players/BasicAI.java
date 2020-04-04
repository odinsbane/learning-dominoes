package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.AvailableMove;
import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.ArrayList;
import java.util.List;

/**
 * It will play the highest scoring value it can. Otherwise it will
 * play a random valid move.
 *
 * Created by msmith on 5/13/15.
 */
public class BasicAI extends AbstractAI {
    public BasicAI(DominoGame game){
        super(game);
    }

    @Override
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
                return;
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
    public String toString(){
        return "basic";
    }
}
