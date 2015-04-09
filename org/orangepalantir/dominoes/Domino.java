package org.orangepalantir.dominoes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Created by melkor on 4/8/15.
 */
public class Domino {
    public double length = 60;
    public double width = 25;
    public final int A;
    public final int B;

    public static final int FRONT = 0;
    public static final int BACK = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    Domino[] neighbors = new Domino[4];

    final static double ugly = 180.0/Math.PI;
    double x;
    double y;
    Rectangle rect = new Rectangle(width, length);
    double theta;
    boolean faceUp=false;
    boolean spinner = false;

    public Domino(int a, int b){
        A = a;
        B = b;

        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2);
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

    public int getPlayableValue(int location){
        switch(location) {
            case FRONT:
                return A;
            case BACK:
                return B;
            case LEFT:
            case RIGHT:
                if (A != B) {
                    throw new IllegalArgumentException("Piece has different values, the side is not valid: " + location);
                }
                return A;
            default:
                throw new IllegalArgumentException("Not valid location: " + location);

        }
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public double[] getPosition(){
        return new double[]{x,y};
    }
    private void updateRectangle(){
        rect.setTranslateX(x);
        rect.setTranslateY(y);
        rect.setRotate(theta * ugly);

    }
    public Rectangle getBounds(){
        return rect;
    }
    public boolean contains(double x, double y){
        return rect.contains(rect.sceneToLocal(x,y));
    }

    public void setAngle(double v) {
        theta = v;
        updateRectangle();
    }

    public void setFaceUp(boolean t){
        faceUp=t;
    }

    public void setSpinner(boolean t){
        spinner = t;
    }
    public boolean isSpinner(){
        return spinner;
    }
    public void draw(GraphicsContext gc){
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        gc.translate(x + width * 0.5, y + length * 0.5);
        gc.rotate(theta * ugly);
        gc.translate(-width*0.5, -length*0.5);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.fillRect(0, 0, width, length);
        gc.strokeRect(0, 0, width, length);

        if(faceUp){
            gc.strokeText(A + "", 0.33*width, 0.75*length);
            gc.strokeText(B + "", 0.33*width, 0.25*length);
        }
        gc.translate(width*0.5, length*0.5);
        gc.rotate(-theta *ugly);
        gc.translate(-x-width*0.5, -y-length*0.5);

    }

    double[] getDirection(int position){
        double[] pos = new double[2];
        switch(position) {
            case FRONT:
                pos[0] = 0;
                pos[1] = -length*0.5;
                break;
            case BACK:
                pos[0] = 0;
                pos[1] = length*0.5;
                break;
            case LEFT:
                pos[0] = width*0.5;
                pos[1] = 0;
                break;
            case RIGHT:
                pos[0] = -width*0.5;
                pos[1] = 0;
                break;
        }
        double dx = Math.cos(theta)*pos[0] + Math.sin(theta)*pos[1];
        double dy = -Math.sin(theta)*pos[0] + Math.cos(theta)*pos[1];
        pos[0] = dx;
        pos[1] = dy;
        return pos;
    }

    public void connect(Domino d, int location) {
        neighbors[location]=d;
    }
}
