package org.orangepalantir.dominoes;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by melkor on 5/24/17.
 */
public class PlayLog implements GameObserver{
    DominoGame game;
    Pane parent;
    public PlayLog(Pane p){
        parent = p;
    }

    public void setGame(DominoGame game){
        this.game = game;
        game.addObserver(this);
    }
    @Override
    public void update() {
        System.out.println("updating logs");

        List<Text> moves;
        synchronized (game.moveLog) {
            moves = game.moveLog.stream().map(m -> new Text(m.toString())).collect(Collectors.toList());
        }
        List<Text> states;
        synchronized (game.stateLog) {
            states = game.stateLog.stream().map(s -> new Text(s.toString())).collect(Collectors.toList());
        }

        List<Text> history = new ArrayList<>(moves.size()+states.size());
        int n = moves.size()<states.size()?moves.size():states.size();
        for(int i = 0;i<n; i++){
            history.add(moves.get(i));
            history.add(states.get(i));
        }

        Platform.runLater(()->{
            parent.getChildren().clear();
            parent.getChildren().addAll(history);
        });
        
    }

    @Override
    public void postMessage(String message) {

    }

    @Override
    public void waitForInput() {

    }
}
