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
    public AvailableMove(double x, double y, Domino d, int location){
        this.x = x;
        this.y = y;
        played = d;
        exposedLocation = location;
    }


    public boolean contains(double x, double y){
        return x>=this.x - hw&&x - this.x<=hw&&y>=this.y-hw&&y - this.y<=hw;
    }

    public void draw(GraphicsContext gc){

        gc.setStroke(Color.BLUE);
        gc.strokeRect(x - 0.5*width, y - 0.5*width, width, width);
        gc.fillText("" + exposedLocation, x, y);

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
                double[] offset = playing.getDirection(Domino.LEFT);
                replacements.add(new AvailableMove(x + offset[0], y + offset[1], playing, Domino.LEFT));
                replacements.add(new AvailableMove(x - offset[0], y - offset[1], playing, Domino.RIGHT));

            } else{
                //available
                playing.setPosition(x, y);
                playing.setAngle(Math.PI / 2);
                double[] offset = playing.getDirection(Domino.FRONT);

                replacements.add(new AvailableMove(x + offset[0], y + offset[1], playing, Domino.FRONT));
                replacements.add(new AvailableMove(x - offset[0], y - offset[1], playing, Domino.BACK));

            }
        } else{
            if(playing.A==playing.B){
                //gets placed side ways.
                connectDominos(played, exposedLocation, playing, Domino.RIGHT);
                double[] displace = playing.getDirection(Domino.RIGHT);
                replacements.add(new AvailableMove(playing.x + displace[0], playing.y + displace[1], playing, Domino.LEFT));
            } else{
                int value = played.getPlayableValue(exposedLocation);
                int exposed;
                if(value==playing.A){
                    exposed=Domino.BACK;
                } else{
                    exposed = Domino.FRONT;
                }


                connectDominos(played, exposedLocation, playing, exposed);
                double[] displace = playing.getDirection(exposed);

                replacements.add(new AvailableMove(playing.x + displace[0], playing.y + displace[1], playing, exposed));
            }
        }


        return replacements;
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

    double[] normalize(double[] v){
        double m = Math.sqrt(v[0]*v[0] + v[1]*v[1]);

        return new double[]{v[0]/m, v[1]/m};
    }

}
