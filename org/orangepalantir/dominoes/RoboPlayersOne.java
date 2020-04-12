package org.orangepalantir.dominoes;

import org.orangepalantir.dominoes.players.BasicAI;
import org.orangepalantir.dominoes.players.ImprovedBasic;
import org.orangepalantir.dominoes.players.Player;
import org.orangepalantir.dominoes.players.RandomAI;

import java.util.Arrays;
import java.util.List;

/**
 * First attempt at making ai's play many many games.
 *
 *
 * Created on 5/28/17.
 */
public class RoboPlayersOne{
    RoboPlayersOne(){

    }

    public void runGames(){
        int[] tallies = new int[3];
        BasicAI bai = new BasicAI();
        Player iai = new ImprovedBasic(1, 3, 0.001);
        RandomAI rai = new RandomAI();
        List<Player> players = Arrays.asList(bai, iai, rai);

        for(int i = 0; i<10000; i++){


            DominoGame game = DominoGame.startSixesGame(players);
            int goal = game.getGoal();

            game.playAndWait();

            boolean found = false;
            for(int j = 0; j<players.size(); j++){
                int v = game.getPlayersScore(players.get(j));
                if(v==goal){
                    if(found){
                        System.out.println("two winners!");
                    }
                    tallies[j]++;
                    found = true;
                }
            }
            if(!found){
                System.out.println("no winners");
            }

        }
        for(int j = 0; j<players.size(); j++){
            System.out.println(players.get(j) + " :: " + tallies[j]);
        }
    }
    public static void main(String[] args){
        RoboPlayersOne one = new RoboPlayersOne();
        one.runGames();
    }
}
