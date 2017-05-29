package org.orangepalantir.dominoes;

import org.orangepalantir.dominoes.players.BasicAI;
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
        for(int i = 0; i<10000; i++){
            DominoGame game = DominoGame.startSixesGame();
            BasicAI bai = new BasicAI(game);
            RandomAI ai = new RandomAI(game);
            game.addPlayer(bai);
            game.addPlayer(ai);
            game.setMonitor(this);
            game.startNewGame();
            game.gameLoop();
            int basic = game.scoreBoard.scores.get(bai).getValue();
            int random = game.scoreBoard.scores.get(ai).getValue();
            if(basic>random){
                basicTally += 1;
            } else{
                randomTally += 1;
            }
        }
        System.out.println(basicTally + ", " + randomTally);
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
