package sample.controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.Context;
import sample.ExtendedPoint;
import sample.Main;
import sample.MyZiImageLib.ImageToolException;
import sample.MyZiImageLib.MPOSplitter;
import sample.RemService;

import java.io.File;
import java.io.IOException;

import static org.opencv.core.Core.minMaxLoc;
import static sample.Operations.*;

public class Controller {

    @FXML
    public ProgressIndicator progressIndicator;
    @FXML
    private CheckBox sobel;
    @FXML
    private ImageView imageView, leftIcon, rightIcon;
    @FXML
    private
    TextField mouseCts;
    @FXML
    private Button bdot;
    @FXML
    private Button leftImageButton;
    @FXML
    private Button rightImageButton;
    @FXML
    private Button resultImageButton;

    @FXML
    private Button SURFbutton;
    // @FXML
    // private ScrollPane sc;
    @FXML
    private StackPane stackPane;

    private Group leftGroup, rightGroup, resultGroup;
    // the main stage
    private Stage stage;

    private Mat leftMat;
    private Mat rightMat;
    private Mat template;
    private Mat ccoefR;
    private Mat sqdiffR;
    private Mat ccoefRN;
    private Mat sqdiffRN;
    private Image leftImage, rightImage, resultImage;

    private int leftX, leftY, rightX, rightY;


    public void init() {
        leftMat = new Mat();
        rightMat = new Mat();

        stackPane.getChildren().removeAll(imageView);
        leftGroup = new Group(imageView);
        rightGroup = new Group();

        leftImageButton.setDisable(true);
        rightImageButton.setDisable(true);
        resultImageButton.setDisable(true);
        sobel.setDisable(true);

        bdot.setDisable(true);

//        progressIndicator.

        mouseCts.setVisible(false);

        stackPane.addEventFilter(MouseEvent.MOUSE_MOVED, e ->
                mouseCts.setText("[" + e.getX() + "; " + e.getY() + "]"));

        final Affine accumulatedScales = new Affine();
        stackPane.getTransforms().add(accumulatedScales);

        stackPane.addEventFilter(ScrollEvent.ANY, event -> {
            double scaleFactor = (event.getDeltaY() > 0)
                    ? 1.1
                    : 1 / 1.1;
            accumulatedScales.appendScale(scaleFactor, scaleFactor, event.getX(), event.getY());
            event.consume();
        });

        Context.getInstance().setProgressIndicator(progressIndicator);
        progressIndicator.setVisible(false);
    }

    @FXML
    protected void loadImage() {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        FileChooser.ExtensionFilter extFilterMPO = new FileChooser.ExtensionFilter("MPO files (*.mpo)", "*.MPO");
        fileChooser.getExtensionFilters().addAll(extFilterMPO, extFilterPNG, extFilterJPG);

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {

            String[] array = file.getName().split("\\.");
            String format = array[array.length - 1];
            System.out.println(format);

            leftImageButton.setDisable(true);
            rightImageButton.setDisable(true);
            resultImageButton.setDisable(true);
            bdot.setDisable(true);

            if (stackPane.getChildren().size() == 0) {
                stackPane.getChildren().add(leftGroup);
            }

            new RemService(() -> {

                if (leftGroup.getChildren().size() > 1) {
                    deleteAllPoint();
                }

                MPOSplitter mpo = new MPOSplitter();
                try {
                    mpo.setSRC(file);
                    mpo.searchImages();   // Количество найденых картинок
                    leftMat = Imgcodecs.imdecode(mpo.getImageNr(0), Imgcodecs.CV_LOAD_IMAGE_COLOR);
                    rightMat = Imgcodecs.imdecode(mpo.getImageNr(1), Imgcodecs.CV_LOAD_IMAGE_COLOR);
                } catch (ImageToolException | IOException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    leftImage = mat2Image(leftMat);
                    imageView.setImage(leftImage);
                    imageView.setViewport(new Rectangle2D(0, 0, leftMat.width(), leftMat.height()));
                    rightImage = mat2Image(rightMat);

                    sobel.setDisable(false);
                    bdot.setDisable(false);

                    mouseCts.setVisible(true);

                    leftImageButton.setDisable(false);
                    rightImageButton.setDisable(false);
                    resultImageButton.setDisable(false);
                    System.out.println("leftMat loaded");
                });
            }).start();
        }
    }

    @FXML
    private void showLeftImage() {
        if (sobel.isSelected()) {
            leftImage = mat2Image(doSobel(leftMat));
            imageView.setImage(leftImage);
        } else {
            leftImage = mat2Image(leftMat);
            imageView.setImage(leftImage);
        }
        leftGroup.getChildren().remove(imageView);
        leftGroup.getChildren().add(imageView);
        imageView.toBack();
        stackPane.getChildren().set(0, leftGroup);
    }

    @FXML
    private void showRightImage() {
        if (sobel.isSelected()) {
            rightImage = mat2Image(doSobel(rightMat));
            imageView.setImage(rightImage);
        } else {
            rightImage = mat2Image(rightMat);
            imageView.setImage(rightImage);
        }
        rightGroup.getChildren().remove(imageView);
        rightGroup.getChildren().add(imageView);
        imageView.toBack();
        stackPane.getChildren().set(0, rightGroup);
    }

    public void showResultImage() {
        Mat transposeLeft = new Mat();
        Mat mat1 = new Mat();
        Mat mat2 = new Mat();
        Mat result = new Mat();
        Core.transpose(doSobel(leftMat), transposeLeft);
        transposeLeft.convertTo(mat1, CvType.CV_32FC1);
        doSobel(rightMat).convertTo(mat2, CvType.CV_32FC1);
        System.out.println("width = " + transposeLeft.width() + ";  height = " + transposeLeft.height());
        System.out.println("width = " + rightMat.width() + ";  height = " + rightMat.height());
        Core.gemm(mat1, mat2, 1, new Mat(), 0, result);
        resultImage = mat2Image(result);
//        resultGroup.getChildren().remove(imageView);
//        resultGroup.getChildren().add(imageView);
//        imageView.toBack();
//        stackPane.getChildren().set(0, resultGroup);
    }

    @FXML
    void dotClick() {
        if (imageView.getImage() != null) {
            if (imageView.getImage() != leftImage) {
                showLeftImage();
            }
            bdot.setDisable(true);
            leftImageButton.setDisable(true);
            rightImageButton.setDisable(true);
            resultImageButton.setDisable(true);
            imageView.setPickOnBounds(true);

            ccoefR = Mat.zeros(leftMat.size(), CvType.CV_32F);
            sqdiffR = Mat.zeros(leftMat.size(), CvType.CV_32F);

            ccoefRN = Mat.zeros(leftMat.size(), CvType.CV_32F);
            sqdiffRN = Mat.zeros(leftMat.size(), CvType.CV_32F);

            imageView.setOnMouseClicked(e -> {

                if (e.isControlDown()) {
                    int winSize = 7;

                    if (this.imageView.getImage() == leftImage) {
                        leftX = (int) e.getX();
                        leftY = (int) e.getY();
                        System.out.println("leftX = " + leftX + "\nleftY = " + leftY);


                        //System.out.println("["+e.getX()+", "+e.getY()+"]");
                        dotPut(leftGroup, new ExtendedPoint(leftX, leftY, Color.AZURE, Color.ALICEBLUE));
                        showIcon(leftMat, leftIcon, leftX, leftY);

                        showRightImage();
                        System.out.println("rightMat loaded");
                        template = new Mat(leftMat, new Range(leftY - winSize, leftY + winSize), new Range(leftX - winSize, leftX + winSize));

                    } else if (imageView.getImage() == rightImage) {
                        rightX = (int) e.getX();
                        rightY = (int) e.getY();
                        System.out.println("rightX = " + rightX + "\nrightY = " + rightY);

                        showLeftImage();

                        int dx = rightX - 100 + winSize;
                        int dy = rightY - 100 + winSize;

                        Imgproc.matchTemplate(new Mat(rightMat, new Range(rightY - 100, rightY + 100), new Range(rightX - 100, rightX + 100)), template, ccoefR, Imgproc.TM_CCOEFF);
                        Imgproc.matchTemplate(new Mat(rightMat, new Range(rightY - 100, rightY + 100), new Range(rightX - 100, rightX + 100)), template, sqdiffR, Imgproc.TM_SQDIFF);
                        Imgproc.matchTemplate(new Mat(rightMat, new Range(rightY - 100, rightY + 100), new Range(rightX - 100, rightX + 100)), template, ccoefRN, Imgproc.TM_CCOEFF_NORMED);
                        Imgproc.matchTemplate(new Mat(rightMat, new Range(rightY - 100, rightY + 100), new Range(rightX - 100, rightX + 100)), template, sqdiffRN, Imgproc.TM_SQDIFF_NORMED);
                        double cx = minMaxLoc(ccoefR).maxLoc.x + dx;
                        double cy = minMaxLoc(ccoefR).maxLoc.y + dy;
                        double sx = minMaxLoc(sqdiffR).minLoc.x + dx;
                        double sy = minMaxLoc(sqdiffR).minLoc.y + dy;
                        double cnx = minMaxLoc(ccoefRN).maxLoc.x + dx;
                        double cny = minMaxLoc(ccoefRN).maxLoc.y + dy;
                        double snx = minMaxLoc(sqdiffRN).minLoc.x + dx;
                        double sny = minMaxLoc(sqdiffRN).minLoc.y + dy;

                        showIcon(rightMat, rightIcon, rightX, rightY);
                        dotPut(rightGroup, new ExtendedPoint(cx, cy, Color.BLUE, Color.ALICEBLUE));
                        dotPut(rightGroup, new ExtendedPoint(sx, sy, Color.RED, Color.ALICEBLUE));
                        dotPut(rightGroup, new ExtendedPoint(cnx, cny, Color.INDIGO, Color.ALICEBLUE));
                        dotPut(rightGroup, new ExtendedPoint(snx, sny, Color.YELLOW, Color.ALICEBLUE));

                        System.out.println("CCOEFR: [" + (minMaxLoc(ccoefR).maxLoc.x + dx) + "; " + (minMaxLoc(ccoefR).maxLoc.y + dy) + "]");
                        System.out.println("SQDIFFR: [" + (minMaxLoc(ccoefR).minLoc.x + dx) + "; " + (minMaxLoc(ccoefR).minLoc.y + dy) + "]");
                        System.out.println("CCOEFRN: [" + (minMaxLoc(sqdiffR).maxLoc.x + dx) + "; " + (minMaxLoc(sqdiffR).maxLoc.y + dy) + "]");
                        System.out.println("SQDIFFRN: [" + (minMaxLoc(sqdiffR).minLoc.x + dx) + "; " + (minMaxLoc(sqdiffR).minLoc.y + dy) + "]");

                        //System.out.println("Z = " + (6.3 * 71 / (Math.abs(rightX - leftX))));  + 201 * ((rightY / 201) )
                        imageView.setOnMouseClicked(null);
                        bdot.setDisable(false);

                        leftImageButton.setDisable(false);
                        rightImageButton.setDisable(false);
                        resultImageButton.setDisable(false);

                    }
                } else {
                    e.consume();
                }
            });
        }
    }

    private void showIcon(Mat mat, ImageView icon, int x, int y) {
        // optimize the dimension of the loaded image
        Mat clone = mat.clone(); // tak nado
        int z = 13;
        Imgproc.drawMarker(clone, new Point(x, y), new Scalar(255, 255, 255), Imgproc.MARKER_CROSS, 2, 1, Imgproc.LINE_4);
        Mat pre = new Mat(clone, new Range(y - z, y + z), new Range(x - z, x + z));
        Mat padded = optimizeImageDim(pre);
        padded.convertTo(padded, CvType.CV_32FC1);

        // show the result of the transformation as an image
        icon.setImage(mat2Image(padded));
    }

    private void dotPut(Group stackPane, ExtendedPoint extPoint) {
        stackPane.getChildren().addAll(extPoint.circle, extPoint.textOnImage);
        extPoint.circle.toFront();
        extPoint.textOnImage.toFront();
    }

    @FXML
    void deleteAllPoint() {
        leftGroup.getChildren().removeIf(node -> node instanceof Circle || node instanceof Text);
        rightGroup.getChildren().removeIf(node -> node instanceof Circle || node instanceof Text);
    }

    @FXML
    void deleteLastPoint() {
        int size = leftGroup.getChildren().size();
        if (size > 1) {
            //this.leftGroup.getChildren().removeIf(node -> (node instanceof Circle || node instanceof Text) && node.);
            leftGroup.getChildren().remove(size - 2, size);
            rightGroup.getChildren().remove(size - 2, size);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("No Point on Image");
            alert.setContentText("Point Not Found");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    void doSURF() {
        resultImage = mat2Image(mySURF(leftMat, rightMat));
    }

    public Button getResultImageButton() {
        return resultImageButton;
    }

    public Image getResultImage() {
        doSURF();
        return resultImage;
    }

    public Button getSURFbutton() {
        return SURFbutton;
    }

    public Mat getLeftMat() {
        return leftMat;
    }

    public Mat getRightMat() {
        return rightMat;
    }

//    @FXML
//    protected void sobelSelected() {
//        // check whether the other checkbox is selected and deselect it
//        if (this.dilateErode.isSelected()) {
//            this.dilateErode.setSelected(false);
//            this.inverse.setDisable(true);
//            this.threshold.setDisable(true);
//        }
//    }

}