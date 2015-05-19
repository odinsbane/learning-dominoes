package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.Domino;

import java.util.List;

/**
 * Created by msmith on 4/9/15.
 */
public interface Player {
    public void makeMove();
    public int getDominoCount();
    public List<Domino> returnDominos();
    public List<Domino> getDominos();
}
