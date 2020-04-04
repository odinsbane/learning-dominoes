package org.orangepalantir.dominoes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by msmith on 4/9/15.
 */
public class AvailableMove {
    Rectangle rect;
    double width = 20;
    double hw = width*0.5;
    double[] xy = new double[2];
    int exposedLocation = -1;
    Domino based;
    Domino played;

    /**
     * Creates an available move off of this dominoe.
     *
     * @param d the domino that is associated to this move.
     * @param location the location of the move relative to the domino end.
     */
    public AvailableMove(Domino d, int location){
        based = d;
        exposedLocation = location;
    }

    /**
     * Creates an emtpy available move, at the specified location. Presumable the start of the game.
     * @param x
     * @param y
     */
    public AvailableMove(double x, double y){
        xy[0] = x;
        xy[1] = y;
    }


    public boolean contains(double x, double y){
        double[] pos = getPosition();


        return x>=pos[0] - hw&&x - pos[0]<=hw&&y>=pos[1]-hw&&y - pos[1]<=hw;

    }

    public int changeIfCovered(Domino check){
        if(based ==null){
            return check.A + check.B;
        }
        int lost = getScore();
        if(based.isSpinner()&& based.connectedCount()==0){
            lost = 0;
        }

        if(check.A==check.B){
            return 2*check.A - lost;
        }

        int exposedValue = based.getPlayableValue(exposedLocation);
        return (exposedValue==check.A?check.B:check.A) - lost;
    }
    double[] getPosition(){
        double[] p = new double[2];
        if(based !=null){
            double[] pos = based.getPosition();
            double[] offset = based.getDirection(exposedLocation);
            double l = Math.sqrt(offset[0]*offset[0] + offset[1]*offset[1]);
            double a = (l+width*0.5+2)/l;
            p[0] = pos[0] + 0.5* based.width + offset[0]*a;
            p[1] = pos[1] + 0.5* based.length + +offset[1]*a;
        } else{
            p[0] = xy[0];
            p[1] = xy[1];
        }

        return p;
    }
    public void draw(GraphicsContext gc){

        double[] pos = getPosition();
        double x = pos[0] - hw;
        double y = pos[1] - hw;
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2.0);
        gc.strokeRect(x, y, width, width);
        gc.setFill(Color.RED);
        if(based !=null)
        gc.fillText("" + exposedLocation + "," + based.getPlayableValue(exposedLocation), x, y);

    }

    public boolean isValidMove(Domino d){

        if(exposedLocation<0) return true;
        int exposedValue = based.getPlayableValue(exposedLocation);
        return d.A==exposedValue||d.B==exposedValue;

    }

    public List<AvailableMove> performMove(Domino playing){
        played = playing;
        //set the position of the domino.
        List<AvailableMove> replacements = new ArrayList<AvailableMove>();
        if(exposedLocation==-1){
            //first play.
            if(playing.isSpinner()){
                //available spots on the left and right of domino
                playing.setPosition(xy[0], xy[1]);
                playing.setAngle(Math.PI / 2);
                replacements.add(new AvailableMove(playing, Domino.LEFT));
                replacements.add(new AvailableMove(playing, Domino.RIGHT));

            } else{
                //available
                playing.setPosition(xy[0], xy[1]);
                playing.setAngle(Math.PI / 2);
                replacements.add(new AvailableMove(playing, Domino.FRONT));
                replacements.add(new AvailableMove(playing, Domino.BACK));

            }
        } else{
            if(playing.A==playing.B){
                //gets placed side ways.
                connectDominos(based, exposedLocation, playing, Domino.RIGHT);
                replacements.add(new AvailableMove(playing, Domino.LEFT));
            } else{
                int value = based.getPlayableValue(exposedLocation);
                int exposed;
                int connect;
                if(value==playing.A){
                    exposed=Domino.BACK;
                    connect=Domino.FRONT;
                } else{
                    exposed = Domino.FRONT;
                    connect = Domino.BACK;
                }


                connectDominos(based, exposedLocation, playing, connect);
                replacements.add(new AvailableMove(playing, exposed));
                if(based.isSpinner() && based.connectedCount()==2){
                    replacements.add(new AvailableMove(based, Domino.FRONT));
                    replacements.add(new AvailableMove(based, Domino.BACK));
                }
            }
        }


        return replacements;
    }

    public boolean hasBase(){
        return based!=null;
    }

    public Integer exposedNumber(){
        return based.getPlayableValue(exposedLocation);
    }

    public int getScore(){
        if(based ==null){
            return 0;
        }
        int score;
        if(based.isSpinner()){
            //check for exposed
            if(based.connectedCount()==1){
                score = based.A + based.B;
            } else if(based.connectedCount()==0){
                score = based.A;
            } else{
                score = 0;
            }
        } else if(based.A== based.B){
            score = based.A + based.B;
        } else{
            if(exposedLocation==Domino.FRONT){
                score = based.A;
            } else{
                score = based.B;
            }
        }

        return score;
    }
    public void connectDominos(Domino a, int faceA, Domino b, int faceB){
        double[] aPos = a.getPosition();
        double angle = a.getAngle();
        double[] aDir = a.getDirection(faceA);

        switch(faceA){
            case Domino.FRONT:
                //no offset.
                break;
            case Domino.BACK:
                angle = angle+Math.PI;
                break;
            case Domino.LEFT:
                angle = angle - Math.PI/2;
                break;
            case Domino.RIGHT:
                angle = angle + Math.PI/2;
                break;
            default:
                throw new IllegalArgumentException(String.format("%d is not a valid face", faceA));
        }

        switch(faceB){
            case Domino.FRONT:
                //face to face
                angle = angle + Math.PI;
                break;
            case Domino.BACK:
                //same direction.
                break;
            case Domino.LEFT:
                angle = angle - Math.PI/2;
                break;
            case Domino.RIGHT:
                angle = angle + Math.PI/2;
                break;
            default:
                throw new IllegalArgumentException(String.format("%d is not a valid face", faceB));
        }
        b.setAngle(angle);
        double[] bDir = b.getDirection(faceB);
        b.setPosition(aPos[0] + aDir[0] - bDir[0], aPos[1] + aDir[1] - bDir[1]);

        a.connect(b, faceA);
        b.connect(a, faceB);
    }

    @Override
    public String toString(){
        return "played: " + played + " on " + based;
    }

    public static AvailableMove firstMoveOfGame(double x, double y, int value){
        return new AvailableMove(x,y){
            @Override
            public boolean isValidMove(Domino d){
                return d.A==value&&d.B==value;
            }
        };
    }

    public static void main(String[] args){

        AvailableMove one = new AvailableMove(0,0);
        List<AvailableMove> next = one.performMove(new Domino(1,0));
        Domino b = new Domino(1,0);
        for(AvailableMove m: next){
            System.out.println("playing: " + m.exposedLocation + "," + m.xy[0] + "," + m.xy[1]);
            List<AvailableMove> c = m.performMove(b);
            for(AvailableMove cm: c){
                System.out.println(cm.exposedLocation + "," + cm.xy[0] + "," + cm.xy[1]);
            }
        }

    }

}
