package org.orangepalantir.dominoes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Created by melkor on 4/8/15.
 */
public class Domino {
    final int A;
    final int B;

    double x;
    double y;

    double theta;
    boolean faceUp=false;

    public Domino(int a, int b){
        A = a;
        B = b;
    }

    @Override
    public boolean equals(Object o){
        if(o==this) return true;

        if(o==null)return false;
        if(!getClass().isInstance(o)){
            return false;
        }
        Domino d = (Domino)o;
        return (d.A==A&&d.B==B)||(d.A==B&&d.B==A);
    }

    @Override
    public int hashCode(){
        return A+B;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setAngle(double v) {
        theta = v;
    }

    public void draw(GraphicsContext gc){
        gc.translate(x, y);
        gc.rotate(theta * 360);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.fillRect(0, 0, 25, 60);
        gc.strokeRect(0, 0, 25, 60);
        gc.rotate(-theta*360);
        gc.translate(-x, -y);

    }
}
