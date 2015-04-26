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
    double x;
    double y;
    int exposedLocation = -1;
    Domino played;
    public AvailableMove(Domino d, int location){
        double[] pos = d.getPosition();
        double[] offset = d.getDirection(location);
        this.x = pos[0] + offset[0];
        this.y = pos[1] + offset[1];
        played = d;
        exposedLocation = location;
    }

    public AvailableMove(double x, double y){
        this.x = x;
        this.y = y;
    }


    public boolean contains(double x, double y){
        return x>=this.x - hw&&x - this.x<=hw&&y>=this.y-hw&&y - this.y<=hw;
    }

    public void draw(GraphicsContext gc){

        gc.setStroke(Color.BLUE);
        gc.strokeRect(x - 0.5 * width, y - 0.5 * width, width, width);
        gc.setFill(Color.RED);
        if(played!=null)
        gc.fillText("" + exposedLocation + "," + played.getPlayableValue(exposedLocation), x, y);

    }

    public boolean isValidMove(Domino d){
        System.out.println("checking");

        if(exposedLocation<0) return true;
        int exposedValue = played.getPlayableValue(exposedLocation);
        return d.A==exposedValue||d.B==exposedValue;

    }

    public List<AvailableMove> performMove(Domino playing){
        //set the position of the domino.
        List<AvailableMove> replacements = new ArrayList<AvailableMove>();
        if(exposedLocation==-1){
            //first play.
            if(playing.isSpinner()){
                System.out.println("spinner");
                //available spots on the left and right of domino
                playing.setPosition(x, y);
                playing.setAngle(Math.PI / 2);
                replacements.add(new AvailableMove(playing, Domino.LEFT));
                replacements.add(new AvailableMove(playing, Domino.RIGHT));

            } else{
                //available
                playing.setPosition(x, y);
                playing.setAngle(Math.PI / 2);
                replacements.add(new AvailableMove(playing, Domino.FRONT));
                replacements.add(new AvailableMove(playing, Domino.BACK));

            }
        } else{
            if(playing.A==playing.B){
                //gets placed side ways.
                connectDominos(played, exposedLocation, playing, Domino.RIGHT);
                double[] displace = playing.getDirection(Domino.RIGHT);
                replacements.add(new AvailableMove(playing, Domino.LEFT));
            } else{
                int value = played.getPlayableValue(exposedLocation);
                int exposed;
                int connect;
                if(value==playing.A){
                    exposed=Domino.BACK;
                    connect=Domino.FRONT;
                } else{
                    exposed = Domino.FRONT;
                    connect = Domino.BACK;
                }


                connectDominos(played, exposedLocation, playing, connect);
                replacements.add(new AvailableMove(playing, exposed));
                if(played.isSpinner() && played.connectedCount()==2){
                    replacements.add(new AvailableMove(played, Domino.FRONT));
                    replacements.add(new AvailableMove(played, Domino.BACK));
                }
            }
        }


        return replacements;
    }
    public int getScore(){
        int score;

        if(played.isSpinner()){
            //check for exposed
            if(played.connectedCount()==1){
                score = played.A + played.B;
            } else if(played.connectedCount()==0){
                score = played.A;
            } else{
                score = 0;
            }
        } else if(played.A==played.B){
            score = played.A + played.B;
        } else{
            if(exposedLocation==Domino.FRONT){
                score = played.A;
            } else{
                score = played.B;
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

    public static void main(String[] args){

        AvailableMove one = new AvailableMove(0,0);
        List<AvailableMove> next = one.performMove(new Domino(1,0));
        Domino b = new Domino(1,0);
        for(AvailableMove m: next){
            System.out.println("playing: " + m.exposedLocation + "," + m.x + "," + m.y);
            List<AvailableMove> c = m.performMove(b);
            for(AvailableMove cm: c){
                System.out.println(cm.exposedLocation + "," + cm.x + "," + cm.y);
            }
        }

    }

}
