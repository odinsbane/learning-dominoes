package org.orangepalantir.dominoes;

import org.orangepalantir.dominoes.players.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melkor on 5/24/17.
 */
public class GameState {
    List<PlayerState> players;
    List<Domino> boneyard = new ArrayList<>();
    List<Domino> played = new ArrayList<>();
    public GameState(DominoGame game){
        players = new ArrayList<>();
        for(Player p: game.players){
            players.add(new PlayerState(p));
        }
        boneyard.addAll(game.boneYard);
        played.addAll(game.played);
    }

    @Override
    public String toString(){
        StringBuilder builds = new StringBuilder("bone yard: ");
        for(Domino d: boneyard){
            builds.append(d.toString());
        }

        builds.append(" played: ");
        for(Domino d: played){
            builds.append(d.toString());
        }
        for(PlayerState ps: players){
            builds.append(ps.toString());
        }
        return builds.toString();
}

class PlayerState{
    String name;
    List<Domino> dominos;
    public PlayerState(Player p){
        dominos = new ArrayList<>(p.getDominos());
        name = p.toString();
    }

    @Override
    public String toString(){
        StringBuilder builds = new StringBuilder(name);
        for(Domino d: dominos){
            builds.append(d.toString());
        }
        return builds.toString();
    }
}
}