package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import sample.controllers.Controller;
import sample.controllers.ResultController;
import sample.controllers.SURFController;

import java.io.IOException;

import static sample.Operations.mat2Image;
import static sample.Operations.mySURF;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        initMainStage();
        // Line line = new Line(400, 200, 400, 200);
        // root.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> System.out.println("x = " + e.getSceneX() + "; y = " + e.getSceneY() + ";"));
        // root.getChildren().add(line);
        // primaryStage.setOnCloseRequest((we -> controller.setClosed()));

    }

    private void initMainStage() {

        // load the FXML resource
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/scene.fxml"));
        BorderPane root;
        try {
            root = loader.load();
            Scene scene = new Scene(root, 800, 600);

            primaryStage.setTitle("Scene");
            primaryStage.setScene(scene);
            primaryStage.show();

            Controller controller = loader.getController();
            controller.setStage(primaryStage);
            controller.init();
            controller.getResultImageButton().setOnAction(e -> {
//                controller.showResultImage();
                showResult(controller.getResultImage());
            });
            controller.getSURFbutton().setOnAction(e -> showSURF(controller.getLeftMat(), controller.getRightMat()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showResult(Image i) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/result.fxml"));
            StackPane page = loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Result window");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(page, 1200, 600));

            // Передаём адресата в контроллер.
            ResultController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setResultView(i);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSURF(Mat leftMat, Mat rightMat) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/surf.fxml"));
            StackPane page = loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("SURF");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(page));

            SURFController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setSURFView(mat2Image(mySURF(leftMat, rightMat)));

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}