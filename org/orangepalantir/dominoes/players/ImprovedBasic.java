package org.orangepalantir.dominoes.players;

import org.orangepalantir.dominoes.AvailableMove;
import org.orangepalantir.dominoes.Domino;
import org.orangepalantir.dominoes.DominoGame;

import java.util.*;

public class ImprovedBasic extends AbstractAI {
    double pointWeight = 1;
    double playWeight = 3;
    double countWeight = 0;
    /**
     * Creates an AI that has three basic criteria for playing a domino. Scoring points,
     * being able to play again, and the value of the domino being play.
     *
     * The basic AI would have an equivalent weight of 1, 0, 0.
     *
     * A common strategy to play is to, first check if you can score. Then get rid of the highest
     * piece, then get rid of a piece that gives you the best chances of playing again. That is what
     * this AI attempts to achieve.
     *
     * This also points out a serious flaw to the current status. We need a 'PlayedState' class
     * Then the game state we would 'apply changes' that way we wouldn't have to keep track of
     * all of these change updates. Apply the change, then evaluate the state. This could also
     * be used recursively for checking subsequent moves.
     *
     * @param game game used for deciding move
     * @param scoring The weight attributed to scoring points on a play.
     * @param playing The weight attributed to keeping remaining moves.
     * @param value The weight attributed to the value of the domino being played.
     */
    public ImprovedBasic(DominoGame game, double scoring, double playing, double value){
        super(game);
        pointWeight = scoring;
        playWeight = playing;
        countWeight = value;
    }

    class MoveScore implements Comparable<MoveScore>{
        final GoodMove move;

        int points; //the points a move would score.
        int plays; //the number of plays in hand.
        int count;

        List<Integer> exposed = new ArrayList<>();
        public MoveScore(GoodMove move){
            this.move = move;
            this.count = move.domino.A + move.domino.B;
        }

        public void setPoints(int points){
            this.points = points;
        }

        /**
         * Takes the set of integers that are exposed on the game board prior to making this move.
         * Adjusts them for making the new move.
         * @param oldExposed collection of exposed pieces on the current board
         */
        public void setExposed(List<Integer> oldExposed){
            exposed.addAll(oldExposed);
            if(move.move.hasBase()) {
                int ex = move.move.exposedNumber();
                int ad = move.domino.A == ex ? move.domino.B : move.domino.A;
                exposed.remove(move.move.exposedNumber());
                exposed.add(ad);
            } else{
                if(exposed.size() > 0) {
                  System.out.println("whats up with that.");
                }
                exposed.add(move.domino.A);
                exposed.add(move.domino.B);
            }
        }

        /**
         * Checks the domino, if this move is acceptable after the playable move has been
         * made, then
         * @param d domino that would be played after this play.
         * @return whether or not the domino would be playable.
         */
        public boolean playable(Domino d){
            if(d==move.domino){
                return false;
            }
            if(exposed.contains(d.A) || exposed.contains(d.B)){
                plays++;
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(MoveScore o){
            return Double.compare(getValue(), o.getValue());
        }

        public double getValue(){
            return points*pointWeight + plays*playWeight + count*countWeight;
        }
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

        List<MoveScore> scores = new ArrayList<>();
        List<Integer> exposedPieces = game.getExposedNumbers();
        for(GoodMove gm: good){
            AvailableMove m = gm.move;
            MoveScore score = new MoveScore(gm);
            score.setExposed(exposedPieces);

            Domino d = gm.domino;
            int ifPlayed = current + m.changeIfCovered(d);
            if(ifPlayed%5==0){
                score.setPoints(ifPlayed);
            }

            for(Domino otra: dominos){
                score.playable(otra);
            }

            scores.add(score);

        }

        Collections.sort(scores);

        GoodMove gm = scores.get(scores.size()-1).move;

        game.performMove(gm.domino, gm.move);

        //TODO details like this should not be in ai class.
        //eg player makes the move, game updates state.
        dominos.remove(gm.domino);
        gm.domino.setFaceUp(true);
    }

    @Override
    public String toString(){
        return "Better Basic";
    }
}
