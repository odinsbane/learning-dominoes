package org.orangepalantir.dominoes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Keeping track of unused pieces.
 * Created by melkor on 4/8/15.
 */
public class DominoSet {
    Set<Domino> dominos;
    Random ng = new Random();
    public static DominoSet doubleSixes(){
        Set<Domino> dominos = new HashSet<>();
        for(int i = 0; i<7; i++) {
            for (int j = i; j < 7; j++) {
                dominos.add(new Domino(i,j));
            }
        }
        DominoSet nruter = new DominoSet();
        nruter.dominos = dominos;
        return nruter;
    }

    public Domino getRandomDomino(){
        if(dominos.size()==0){
            return null;
        }
        int i = ng.nextInt(dominos.size());
        Iterator<Domino> iter = dominos.iterator();
        Domino d = iter.next();
        for(int j = 0; j!=i; j++){
            d = iter.next();
        }
        iter.remove();
        return d;
    }

    public void returnDomino(Domino d){
        d.setSpinner(false);
        dominos.add(d);
    }

    public boolean hasDominos() {
        return dominos.size()>0;
    }
}
