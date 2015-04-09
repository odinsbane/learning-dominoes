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
    Domino b;
    public AvailableMove(double x, double y, Domino d, int location){
        this.x = x;
        this.y = y;
        b = d;
        exposedLocation = location;
    }


    public boolean contains(double x, double y){
        return x>=this.x - hw&&x - this.x<=hw&&y>=this.y-hw&&y - this.y<=hw;
    }

    public void draw(GraphicsContext gc){

        gc.setStroke(Color.BLUE);
        gc.strokeRect(x - 0.5*width, y - 0.5*width, width, width);

    }

    public boolean isValidMove(Domino d){
        System.out.println("checking");

        if(exposedLocation<0) return true;
        int exposedValue = b.getPlayableValue(exposedLocation);
        return d.A==exposedValue||d.B==exposedValue;

    }

    public List<AvailableMove> performMove(Domino d){
        //set the position of the domino.
        List<AvailableMove> replacements = new ArrayList<AvailableMove>();
        if(exposedLocation==-1){
            //first play.
            if(d.isSpinner()){
                //available spots on the left and right of domino
                d.setPosition(x, y);
                d.setAngle(Math.PI / 2);
                double[] offset = d.getDirection(Domino.LEFT);
                replacements.add(new AvailableMove(x + offset[0], y + offset[1], d, Domino.LEFT));
                replacements.add(new AvailableMove(x - offset[0], y - offset[1], d, Domino.RIGHT));

            } else{
                //available
                d.setPosition(x, y);
                d.setAngle(Math.PI / 2);
                double[] offset = d.getDirection(Domino.FRONT);

                replacements.add(new AvailableMove(x + offset[0], y + offset[1], d, Domino.FRONT));
                replacements.add(new AvailableMove(x - offset[0], y - offset[1], d, Domino.BACK));

            }
        } else{
            if(d.A==d.B){
                //gets placed side ways.


                double[] loc = b.getDirection(exposedLocation);
                double[] norm = normalize(loc);
                loc[0] = norm[0]*b.width*0.5 + loc[0];
                loc[1] = norm[1]*b.width*0.5 + loc[1];
                double angle = Math.atan2(norm[1], -norm[0]);
                d.setPosition(b.x + loc[0],b.y + loc[1]);
                d.setAngle(angle + Math.PI / 2);
                double[] displace = d.getDirection(Domino.LEFT);
                b.connect(d, exposedLocation);
                d.connect(b, Domino.RIGHT);

                replacements.add(new AvailableMove(d.x + displace[0], d.y + displace[1], d, Domino.LEFT));
            } else{
                double[] loc = b.getDirection(exposedLocation);
                double[] norm = normalize(loc);
                loc[0] = norm[0]*b.length*0.5 + loc[0];
                loc[1] = norm[1]*b.length*0.5 + loc[1];
                double angle = Math.atan2(norm[1], -norm[0]);
                d.setPosition(b.x + loc[0],b.y + loc[1]);
                int value = b.getPlayableValue(exposedLocation);
                int exposed;
                int connect;
                if(value==d.A){
                    exposed=Domino.BACK;
                    connect = Domino.FRONT;
                    angle = angle - Math.PI;
                } else{
                    exposed = Domino.FRONT;
                    connect = Domino.BACK;
                }
                d.setAngle(angle);
                double[] displace = d.getDirection(exposed);
                b.connect(d, exposedLocation);
                d.connect(b, connect);

                replacements.add(new AvailableMove(d.x + displace[0], d.y + displace[1], d, exposed));
            }
        }


        return replacements;
    }

    double[] normalize(double[] v){
        double m = Math.sqrt(v[0]*v[0] + v[1]*v[1]);

        return new double[]{v[0]/m, v[1]/m};
    }

}
