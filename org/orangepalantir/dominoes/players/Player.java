package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.List;

/**
 * To do refactor the make move, which causes the player to perform an action that the game updates upon.
 *
 * Created by msmith on 4/9/15.
 */
public interface Player {
    public void makeMove();
    public int getDominoCount();
    public List<Domino> returnDominos();
    public List<Domino> getDominos();
    public void setGame(DominoGame game);
}
