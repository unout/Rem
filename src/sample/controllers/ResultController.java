package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ResultController {

    public ImageView resultView;
    private Stage dialogStage;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public ImageView getResultView() {
        return resultView;
    }

    public void setResultView(Image i) {
        this.resultView.setImage(i);
        this.resultView.setFitWidth(800);
        this.resultView.setFitHeight(600);
    }

}
