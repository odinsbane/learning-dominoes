package org.orangepalantir.dominoes;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

/**
 * Created by melkor on 4/9/15.
 */
public class JavaFXGeometryTests  extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

        Canvas canvas = new Canvas(600, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

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
        root.getChildren().add(rect);
    }



    public static void main(String[] args) {
        launch(args);
    }

}
