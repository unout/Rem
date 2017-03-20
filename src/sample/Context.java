package sample;

import javafx.scene.control.ProgressIndicator;

public class Context {

    private static volatile Context instance;

    private Context() {
    }

    public static Context getInstance() {

        Context localInstance = instance;
        if (localInstance == null) {
            synchronized (Context.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Context();
                }
            }
        }
        return localInstance;
    }

    private ProgressIndicator progressIndicator;

    ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

}