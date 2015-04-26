package org.orangepalantir.dominoes;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by melkor on 4/9/15.
 */
public class JavaFXGeometryTests  extends Application {
    double ANGLE=Math.PI/4;
    public void drawVerticalDominoes(GraphicsContext gc){
        Domino a = new Domino(0,1);
        a.setPosition(75,75);
        a.setFaceUp(true);

        Domino b = new Domino(2, 3);
        b.setFaceUp(true);

        double[] z = a.getDirection(Domino.FRONT);
        double[] bz = b.getDirection(Domino.BACK);
        double[] p = a.getPosition();
        b.setPosition(p[0] + z[0] - bz[0], p[1]+z[1]-bz[1]);

        a.draw(gc);
        b.draw(gc);
    }

    public void drawDominos(GraphicsContext gc){

        for(int i = 0; i<4; i++){
            for(int j = 0; j<4; j++){

                double x = 100 + 150*i;
                double y = 100 + 150*j;
                int af,ab;

                switch(i){
                    case Domino.FRONT:
                        af=1;
                        ab=0;
                        break;
                    case Domino.BACK:
                        af=0;
                        ab=1;
                        break;
                    default:
                        af=1;
                        ab=1;
                }
                Domino a = new Domino(af,ab);
                a.setAngle(ANGLE);
                a.setPosition(x, y);
                a.setFaceUp(true);
                int bf, bb;
                switch(j){
                    case Domino.FRONT:
                        bf = 2;
                        bb = 0;
                        break;
                    case Domino.BACK:
                        bf = 0;
                        bb = 2;
                        break;
                    default:
                        bf=2;
                        bb=2;
                }
                Domino b = new Domino(bf, bb);
                connectDominos(a, i, b, j);
                b.setFaceUp(true);
                a.draw(gc);
                b.draw(gc);
            }

        }

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


    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

        Canvas canvas = new Canvas(600, 600);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        Rectangle rect = new Rectangle(300,300, 20, 100);
        rect.setStroke(Color.GREEN);
        rect.setFill(Color.BLUE);
        //rect.setRotate(45);
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        root.getChildren().add(rect);
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();

        rect.getTransforms().add(Transform.rotate(45, rect.getX(), rect.getY()));

        rect = new Rectangle(300,300, 20, 100);
        rect.setStroke(Color.GREEN);
        rect.setFill(Color.YELLOW);
        rect.getTransforms().add(Transform.rotate(45, rect.getX() + rect.getWidth() * 0.5, rect.getY() + rect.getHeight() * 0.5));
        root.getChildren().add(rect);

        rect = new Rectangle(300,300, 20, 100);
        rect.setStroke(Color.GREEN);
        rect.setFill(Color.RED);

        drawDominos(gc);

        root.getChildren().add(rect);
        /*
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {


                ANGLE+=0.075;

                Platform.runLater(() -> {
                    gc.clearRect(0,0,800,800);
                    drawDominos(gc);

                });

            }}, 200, 15);

        primaryStage.setOnHidden(evt -> {
            if (t != null) {
                t.cancel();
            }
        });
        */

    }



    public static void main(String[] args) {
        launch(args);
    }

}
