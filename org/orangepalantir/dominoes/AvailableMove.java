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
        return x>=this.x&&x - this.x<=width&&y>=this.y&&y - this.y<=width;
    }

    public void draw(GraphicsContext gc){

        gc.setStroke(Color.BLUE);
        gc.strokeRect(x, y, width, width);

    }

    public boolean isValidMove(Domino d){

        if(exposedLocation<0) return true;
        int exposedValue = b.getPlayableValue(exposedLocation);

        return exposedLocation<0||d.A==exposedValue||d.B==exposedValue;

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

                replacements.add(new AvailableMove(x, y + d.width*0.5, d, Domino.LEFT));
                replacements.add(new AvailableMove(x, y - d.width*0.5, d, Domino.RIGHT));

            } else{
                //available
                d.setPosition(x, y);
                d.setAngle(Math.PI / 2);

                replacements.add(new AvailableMove(x + d.length*0.5, y, d, Domino.FRONT));
                replacements.add(new AvailableMove(x - d.length*0.5, y, d, Domino.BACK));
            }
        }
        return replacements;
    }

}
