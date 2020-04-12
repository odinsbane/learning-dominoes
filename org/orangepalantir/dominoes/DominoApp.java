package org.orangepalantir.dominoes;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.orangepalantir.dominoes.players.BasicAI;
import org.orangepalantir.dominoes.players.ImprovedBasic;
import org.orangepalantir.dominoes.players.Player;
import org.orangepalantir.dominoes.players.RandomAI;
import org.orangepalantir.dominoes.scenes.GameScene;
import org.orangepalantir.dominoes.scenes.StartupScene;

import java.io.IOException;
import java.util.Arrays;


/**
 * Created by melkor on 4/8/15.
 */
public class DominoApp extends Application {
    GraphicsContext gc;
    Stage mainStage;
    Scene startupScene;
    Scene gameScene;
    Parent finishScene;
    GameScene gameController;
    EventHandler<KeyEvent> gameKeyPressedHandler;

    public void loadScenes() throws IOException {
        FXMLLoader startupLoader = new FXMLLoader(DominoApp.class.getResource("/fxml/startup.fxml"));
        startupScene = new Scene(startupLoader.load(), 1200, 1200);
        StartupScene startupController = startupLoader.getController();

        FXMLLoader gameLoader = new FXMLLoader(DominoApp.class.getResource("/fxml/gameplay.fxml"));
        gameScene = new Scene(gameLoader.load());
        gameController = gameLoader.getController();
        gameKeyPressedHandler = gameController::keyPressed;

        startupController.setStartGameCallback(game ->{
            gameController.startGame(game);
            gameController.setGameFinishedCallback(this::endGameScene);
            showGameScene();
        });

    }


    @Override
    public void start(Stage stage) throws Exception{
        loadScenes();
        stage.setTitle("Learning Dominos");
        stage.setScene(startupScene);
        stage.show();
        mainStage = stage;
    }

    public void endGameScene(){
        mainStage.removeEventHandler(KeyEvent.KEY_PRESSED, gameKeyPressedHandler);
        mainStage.setScene(startupScene);
    }

    public void showGameScene() {
        mainStage.setScene(gameScene);
        mainStage.addEventHandler(KeyEvent.KEY_PRESSED, gameKeyPressedHandler);
    }



    public static void main(String[] args) {
        launch(args);
    }

}
