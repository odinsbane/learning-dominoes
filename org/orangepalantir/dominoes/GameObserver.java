package org.orangepalantir.dominoes;

/**
 * Created by melkor on 7/5/15.
 */
public interface GameObserver {
    public void update();
    public void postMessage(String message);
    public void waitForInput();
}
