package org.orangepalantir.dominoes;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;

/**
 * Created by melkor on 4/8/15.
 */
public class Domino {

    static Image back = getBackImage();
    static Image front = getFrontImage();
    static Image[] images = getNumberImages();
    public double length = 30;
    public double width = 15;
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
            gc.drawImage(front, 0, 0);
            gc.drawImage(images[A], 0, 0);
            gc.drawImage(images[B], 0, length*0.5);
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
                pos[0] = -width*0.5;
                pos[1] = 0;
                break;
            case RIGHT:
                pos[0] = width*0.5;
                pos[1] = 0;
                break;
        }
        double dx = Math.cos(theta)*pos[0] - Math.sin(theta)*pos[1];
        double dy = Math.sin(theta)*pos[0] + Math.cos(theta)*pos[1];
        //System.out.printf("%d: %2.2f before %f,%f after %f,%f\n",position, theta/Math.PI, pos[0], pos[1], dx, dy);
        pos[0] = dx;
        pos[1] = dy;
        return pos;
    }

    public void connect(Domino d, int location) {
        neighbors[location]=d;
    }
    public int connectedCount(){
        int c = 0;
        for(int i = 0; i<neighbors.length; i++){
            if(neighbors[i]!=null){
                c++;
            }
        }
        return c;
    }
    public double getAngle() {

        return theta;

    }

    public void clearConnections() {
        for(int i = 0; i<4; i++){
            neighbors[i] = null;
        }
    }
    static Image getBackImage(){
        int length = 30;
        int width = 15;
        WritableImage wi = new WritableImage(width, length);
        PixelWriter pw = wi.getPixelWriter();
        for(int i = 0; i<width; i++){
            for(int j = 0; j<length; j++){
                pw.setColor(i, j, Color.PAPAYAWHIP);
            }
        }
        return wi;
    }

    static Image getFrontImage(){
        int length = 30;
        int width = 15;
        WritableImage wi = new WritableImage(width, length);
        PixelWriter pw = wi.getPixelWriter();
        for(int i = 0; i<width; i++){
            for(int j = 0; j<length; j++){
                pw.setColor(i, j, Color.PAPAYAWHIP);
            }
        }
        return wi;
    }

    static Image[] getNumberImages(){
        int width = 15;

        WritableImage blank = new WritableImage(width, width);

        WritableImage one = new WritableImage(width, width);
        PixelWriter pw = one.getPixelWriter();
        for(int j = width/2 -2; j<width/2+2; j++){
            for(int i = width/2 -2; i<width/2 + 2; i++){
                pw.setColor(i, j, Color.BLACK);
            }
        }

        WritableImage two = new WritableImage(width, width);

        pw = two.getPixelWriter();
        for(int j = width/2 -2; j<width/2+2; j++){
            for(int i = width/2 -2; i<width/2 + 2; i++){
                pw.setColor(i+4, j, Color.BLACK);
                pw.setColor(i-4, j, Color.BLACK);
            }
        }

        WritableImage three = new WritableImage(width, width);

        pw = three.getPixelWriter();
        for(int j = width/2 -2; j<width/2+2; j++){
            for(int i = width/2 -2; i<width/2 + 2; i++){
                pw.setColor(i, j - 4, Color.BLACK);
                pw.setColor(i, j, Color.BLACK);
                pw.setColor(i, j + 4, Color.BLACK);
            }
        }

        WritableImage four = new WritableImage(width, width);
        pw = four.getPixelWriter();
        for(int j = width/2 -2; j<width/2+2; j++){
            for(int i = width/2 -2; i<width/2 + 2; i++){
                pw.setColor(i - 3, j - 3, Color.BLACK);
                pw.setColor(i + 3, j - 3, Color.BLACK);
                pw.setColor(i + 3, j + 3, Color.BLACK);
                pw.setColor(i - 3, j + 3, Color.BLACK);
            }
        }

        WritableImage five = new WritableImage(width, width);
        pw = five.getPixelWriter();

        for(int j = width/2 -2; j<width/2+2; j++){
            for(int i = width/2 -2; i<width/2 + 2; i++){
                pw.setColor(i - 4, j - 4, Color.BLACK);
                pw.setColor(i + 4, j - 4, Color.BLACK);
                pw.setColor(i + 4, j + 4, Color.BLACK);
                pw.setColor(i, j, Color.BLACK);
                pw.setColor(i - 4, j + 4, Color.BLACK);
            }
        }

        WritableImage six = new WritableImage(width, width);
        pw = six.getPixelWriter();
        for(int j = width/2 -2; j<width/2+2; j++){
            for(int i = width/2 -2; i<width/2 + 2; i++){
                pw.setColor(i-3, j - 4, Color.BLACK);
                pw.setColor(i-3, j, Color.BLACK);
                pw.setColor(i-3, j + 4, Color.BLACK);
                pw.setColor(i+3, j - 4, Color.BLACK);
                pw.setColor(i+3, j, Color.BLACK);
                pw.setColor(i+3, j + 4, Color.BLACK);
            }
        }

        return new Image[] { blank, one, two, three, four, five, six};
    }

    @Override
    public String toString(){
        return " ||" + A + ":" + B + "|| ";
    }

}
