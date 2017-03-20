package sample;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RemService extends Service<Void> {

    private Runnable action;

    public RemService(Runnable action) {
        this.action = action;
    }

    protected Task<Void> createTask() {
        return new Task<Void>() {
            protected Void call() throws Exception {
                try {
                    Context.getInstance().getProgressIndicator().setDisable(false);
                    Context.getInstance().getProgressIndicator().setVisible(true);
                    action.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Context.getInstance().getProgressIndicator().setVisible(false);
                    Context.getInstance().getProgressIndicator().setDisable(true);
                }
                return null;
            }
        };
    }
}
