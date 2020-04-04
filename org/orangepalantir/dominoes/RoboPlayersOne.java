package org.orangepalantir.dominoes;

import org.orangepalantir.dominoes.players.BasicAI;
import org.orangepalantir.dominoes.players.ImprovedBasic;
import org.orangepalantir.dominoes.players.Player;
import org.orangepalantir.dominoes.players.RandomAI;

/**
 * First attempt at making ai's play many many games.
 *
 *
 * Created on 5/28/17.
 */
public class RoboPlayersOne implements Monitor{
    RoboPlayersOne(){

    }

    public void runGames(){
        int basicTally = 0;
        int randomTally = 0;
        int random2Tally = 0;
        for(int i = 0; i<10000; i++){
            DominoGame game = DominoGame.startSixesGame();
            BasicAI bai = new BasicAI(game);
            Player ai = new ImprovedBasic(game, 1, 4, 0.001);
            RandomAI ai2 = new RandomAI(game);
            game.addPlayer(bai);
            game.addPlayer(ai);
            game.addPlayer(ai2);

            game.setMonitor(this);
            game.startNewGame();
            game.gameLoop();
            int basic = game.scoreBoard.scores.get(bai).getValue();
            int random = game.scoreBoard.scores.get(ai).getValue();
            int random2 = game.scoreBoard.scores.get(ai2).getValue();
            basicTally += basic==150?1:0;
            randomTally += random==150?1:0;
            random2Tally += random2==150?1:0;
        }
        System.out.println(basicTally + ", " + randomTally + ", " + random2Tally);
    }
    public static void main(String[] args){

        RoboPlayersOne one = new RoboPlayersOne();
        one.runGames();
    }

    @Override
    public void waitForInput() {
    }

    @Override
    public void input() {
    }
}
