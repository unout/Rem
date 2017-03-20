package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Created by User on 13.03.2017.
 */
public class SURFController {

    private ImageView surfView;
    private Stage dialogStage;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public ImageView getSURFView() {
        return surfView;
    }

    public void setSURFView(Image i) {
        this.surfView.setImage(i);
    }
}
