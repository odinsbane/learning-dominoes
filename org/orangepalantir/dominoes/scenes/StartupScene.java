package org.orangepalantir.dominoes.scenes;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Callback;
import org.orangepalantir.dominoes.DominoGame;
import org.orangepalantir.dominoes.DominoSet;
import org.orangepalantir.dominoes.players.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.function.Consumer;

public class StartupScene {
    Consumer<DominoGame> gameCallback;
    ObservableList<Class<? extends Player>> aiPlayers = FXCollections.observableArrayList(
            RandomAI.class,
            BasicAI.class,
            ImprovedBasic.class
    );

    @FXML
    ComboBox<Integer> numberPlayers;
    @FXML
    ComboBox<Class<? extends Player>> playerTwo;
    @FXML
    ComboBox<Class<? extends Player>> playerThree;
    @FXML
    ComboBox<Class<? extends Player>> playerFour;
    @FXML
    ComboBox<Class<? extends Player>> playerFive;
    @FXML
    Button startGame;
    void initializePlayerSelector(ComboBox<Class<? extends Player>> c){

        Callback<ListView<Class<? extends Player>>, ListCell<Class<? extends Player>>> factor = new Callback<ListView<Class<? extends Player>>, ListCell<Class<? extends Player>>>() {
            @Override
            public ListCell<Class<? extends Player>> call(ListView<Class<? extends Player>> param) {
                ListCell<Class<? extends Player>> lc = new ListCell< Class<? extends Player>>(){
                    @Override
                    public void updateItem(Class<? extends Player> item, boolean empty){
                        super.updateItem(item, empty);
                        if(!empty && item!=null) {
                            setText(item.getSimpleName());
                        }
                    }
                };
                return lc;
            }
        };

        c.setCellFactory(factor);
        c.setButtonCell(factor.call(null));
        c.setItems(aiPlayers);
        c.setValue(c.getItems().get(0));
    }

    public Player getInstance(Class<? extends Player> c){
        try {
            Constructor<? extends Player> con = c.getConstructor();
            Player p = con.newInstance();
            return p;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void startAction(){
        HumanPlayer humanPlayer = new HumanPlayer();
        List<ComboBox<Class<? extends Player>>> choices = Arrays.asList(
                playerTwo,
                playerThree,
                playerFour,
                playerFive
        );
        List<Player> players = new ArrayList<>();
        players.add(humanPlayer);

        int nAi = numberPlayers.getValue() - 1;

        for(int i = 0; i<nAi; i++){
            Player p = getInstance(choices.get(i).getValue());
            players.add(p);
        }

        DominoGame game = DominoGame.startSixesGame(players);
        game.setHumanPlayer(humanPlayer);
        gameCallback.accept(game);
    }

    public void enableChoices(ActionEvent event){

        int v = numberPlayers.getValue();
        playerFive.setDisable(v<5);
        playerFour.setDisable(v<4);
        playerThree.setDisable(v<3);
    }

    public void setStartGameCallback(Consumer<DominoGame> consumer){
        gameCallback = consumer;
    }



    public void initialize() {
        initializePlayerSelector(playerTwo);
        initializePlayerSelector(playerThree);
        initializePlayerSelector(playerFour);
        initializePlayerSelector(playerFive);
        numberPlayers.setValue(3);
        enableChoices(null);
    }
}
