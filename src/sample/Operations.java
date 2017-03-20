package sample;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;


public class Operations {

//    Image showImage() {
//
//        if (this.canny) {
//            this.image = mat2Image(doCanny(mat));
////            this.mat = this.doCanny(mat);
//        }
//        // foreground detection
//        else if (this.dilateErode) {
//            this.image = mat2Image(doBackgroundRemoval(mat));
////            this.mat = this.doBackgroundRemoval(mat);
//        } else if (this.sobel) {
//            this.image = mat2Image(doSobel(mat));
//        } else {
//
//            // convert the Mat object (OpenCV) to Image (JavaFX)
//            this.image = mat2Image(this.mat);
//        }
//        return image;
//    }

//    Image showIcon(Mat mat) {
//        // optimize the dimension of the loaded image
//        Mat padded = optimizeImageDim(mat);
//        padded.convertTo(padded, CvType.CV_32FC1);
//
//        // show the result of the transformation as an image
//        this.icon = mat2Image(padded);
//
//        return icon;
//    }

    static double clamp(double value, double min, double max) {

        if (Double.compare(value, min) < 0)
            return min;

        if (Double.compare(value, max) > 0)
            return max;

        return value;
    }

    static Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    /**
     * Get the average hue value of the image starting from its Hue channel
     * histogram
     *
     * @param hsvImg    the current frame in HSV
     * @param hueValues the Hue component of the current frame
     * @return the average Hue value
     */
    private static double getHistAverage(Mat hsvImg, Mat hueValues) {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++) {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    public static Mat doSobel(Mat frame) {
        // init
        Mat src_gray = new Mat();
        Mat detectedEdges = new Mat();
        Mat grad = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

        Imgproc.cvtColor(frame, src_gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.blur(src_gray, detectedEdges, new Size(3, 3));

        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_x, abs_grad_x);
        // Scharr( src_gray, grad_y, ddepth, 0, 1, scale, delta, BORDER_DEFAULT );
        Sobel(src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_y, abs_grad_y);

        addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);

        threshold(grad, grad, 100, 255, THRESH_BINARY);
        double sum = 0;
        for (int i = 0; i < grad.width(); i++) {
            for (int j = 0; j < grad.height(); j++) {
                sum = sum + grad.get(j, i)[0];
            }
        }
        System.out.println("sum : " + (sum / 255));
        return grad;
    }

    public static Mat optimizeImageDim(Mat image) {
        // init
        Mat padded = new Mat();
        // get the optimal rows size for dft
        int addPixelRows = Core.getOptimalDFTSize(image.rows());
        // get the optimal cols size for dft
        int addPixelCols = Core.getOptimalDFTSize(image.cols());
        // apply the optimal cols and rows size to the image
        Core.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
                Core.BORDER_CONSTANT, Scalar.all(0));

        return padded;
    }

    /**
     * Optimize the magnitude of the complex image obtained from the DFT, to
     * improve its visualization
     *
     * @param complexImage the complex image obtained from the DFT
     * @return the optimized image
     */
    private Mat createOptimizedMagnitude(Mat complexImage) {
        // init
        List<Mat> newPlanes = new ArrayList<>();
        Mat mag = new Mat();
        // split the comples image in two planes
        Core.split(complexImage, newPlanes);
        // compute the magnitude
        Core.magnitude(newPlanes.get(0), newPlanes.get(1), mag);

        // move to a logarithmic scale
        Core.add(Mat.ones(mag.size(), CvType.CV_32F), mag, mag);
        Core.log(mag, mag);
        // optionally reorder the 4 quadrants of the magnitude image
        this.shiftDFT(mag);
        // normalize the magnitude image for the visualization since both JavaFX
        // and OpenCV need images with value between 0 and 255
        // convert back to CV_8UC1
        mag.convertTo(mag, CvType.CV_8UC1);
        Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        // you can also write on disk the resulting image...
        // Imgcodecs.imwrite("../magnitude.png", mag);

        return mag;
    }

    /**
     * Reorder the 4 quadrants of the image representing the magnitude, after
     * the DFT
     *
     * @param image the {@link Mat} object whose quadrants are to reorder
     */
    private void shiftDFT(Mat image) {
        image = image.submat(new Rect(0, 0, image.cols() & -2, image.rows() & -2));
        int cx = image.cols() / 2;
        int cy = image.rows() / 2;

        Mat q0 = new Mat(image, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(image, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(image, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(image, new Rect(cx, cy, cx, cy));

        Mat tmp = new Mat();
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);

        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);
    }

    /**
     * Given a binary image containing one or more closed surfaces, use it as a
     * mask to find and highlight the objects contours
     *
     * @param maskedImage the binary image to be used as a mask
     * @param frame       the original {@link Mat} image to be used for drawing the
     *                    objects contours
     * @return the {@link Mat} image with the objects contours framed
     */
    static Mat findAndDrawBalls(Mat maskedImage, Mat frame) {

        // init
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }

        return frame;
    }

    public static Mat mySURF(Mat img1, Mat img2) {
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB); //4 = SURF

        MatOfKeyPoint keypointsIm1 = new MatOfKeyPoint();
        MatOfKeyPoint keypointsIm2 = new MatOfKeyPoint();

        detector.detect(img1, keypointsIm1);
        detector.detect(img2, keypointsIm2);

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB); // 2 = SURF;

        Mat descriptorIm1 = new Mat();
        Mat descriptorIm2 = new Mat();

        extractor.compute(img1, keypointsIm1, descriptorIm1);
        extractor.compute(img2, keypointsIm2, descriptorIm2);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED); // 1 = FLANNBASED
        MatOfDMatch matches = new MatOfDMatch();

        if (descriptorIm1.type() != CvType.CV_32F) {
            descriptorIm1.convertTo(descriptorIm1, CvType.CV_32F);
        }

        if (descriptorIm2.type() != CvType.CV_32F) {
            descriptorIm2.convertTo(descriptorIm2, CvType.CV_32F);
        }

        matcher.match(descriptorIm1, descriptorIm2, matches);
        List<DMatch> matchesList = matches.toList();

        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < descriptorIm1.rows(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist) min_dist = dist;
            if (dist > max_dist) max_dist = dist;
        }

        System.out.println("-- Max dist : " + max_dist);
        System.out.println("-- Min dist : " + min_dist);

        LinkedList<DMatch> good_matches = new LinkedList<>();
        MatOfDMatch gm = new MatOfDMatch();

        for (int i = 0; i < descriptorIm1.rows(); i++) {
            if (matchesList.get(i).distance < 3 * min_dist) {
                good_matches.addLast(matchesList.get(i));
            }
        }

        gm.fromList(good_matches);

        Mat img_matches = new Mat();
        Features2d.drawMatches(
                img1,
                keypointsIm1,
                img2,
                keypointsIm2,
                gm,
                img_matches,
                new Scalar(255, 0, 0),
                new Scalar(0, 0, 255),
                new MatOfByte(),
                2);

        LinkedList<Point> objList = new LinkedList<>();
        LinkedList<Point> sceneList = new LinkedList<>();

        List<KeyPoint> keypointsList1 = keypointsIm1.toList();
        List<KeyPoint> keypointsList2 = keypointsIm2.toList();

        for (DMatch good_match : good_matches) {
            objList.addLast(keypointsList1.get(good_match.queryIdx).pt);
            sceneList.addLast(keypointsList2.get(good_match.trainIdx).pt);
        }

        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);

        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);

        Mat hg = Calib3d.findHomography(obj, scene);

        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

        obj_corners.put(0, 0, 0, 0);
        obj_corners.put(1, 0, img1.cols(), 0);
        obj_corners.put(2, 0, img1.cols(), img1.rows());
        obj_corners.put(3, 0, 0, img1.rows());

        Core.perspectiveTransform(obj_corners, scene_corners, hg);
        return img_matches;
//        System.out.println(String.format("Writing %s", pathResult));
//        Highgui.imwrite("src/OpenCV/" + pathResult, img_matches);
    }

    public static Mat SURF(Mat img_object, Mat img_scene) {
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF); //4 = SURF

        MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
        MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();

        detector.detect(img_object, keypoints_object);
        detector.detect(img_scene, keypoints_scene);

        detector.detect(img_object, keypoints_object);
        detector.detect(img_scene, keypoints_scene);

        DescriptorExtractor extractor = DescriptorExtractor.create(2); // 2 = SURF;

        Mat descriptor_object = new Mat();
        Mat descriptor_scene = new Mat();

        extractor.compute(img_object, keypoints_object, descriptor_object);
        extractor.compute(img_scene, keypoints_scene, descriptor_scene);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED); // 1 = FLANNBASED
        MatOfDMatch matches = new MatOfDMatch();

        matcher.match(descriptor_object, descriptor_scene, matches);
        List<DMatch> matchesList = matches.toList();

        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < descriptor_object.rows(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist) min_dist = dist;
            if (dist > max_dist) max_dist = dist;
        }

        System.out.println("-- Max dist : " + max_dist);
        System.out.println("-- Min dist : " + min_dist);

        LinkedList<DMatch> good_matches = new LinkedList<>();
        MatOfDMatch gm = new MatOfDMatch();

        for (int i = 0; i < descriptor_object.rows(); i++) {
            if (matchesList.get(i).distance < 3 * min_dist) {
                good_matches.addLast(matchesList.get(i));
            }
        }

        gm.fromList(good_matches);

        Mat img_matches = new Mat();
        Features2d.drawMatches(
                img_object,
                keypoints_object,
                img_scene,
                keypoints_scene,
                gm,
                img_matches,
                new Scalar(255, 0, 0),
                new Scalar(0, 0, 255),
                new MatOfByte(),
                2);

        LinkedList<Point> objList = new LinkedList<>();
        LinkedList<Point> sceneList = new LinkedList<>();

        List<KeyPoint> keypoints_objectList = keypoints_object.toList();
        List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();

        for (DMatch good_match : good_matches) {
            objList.addLast(keypoints_objectList.get(good_match.queryIdx).pt);
            sceneList.addLast(keypoints_sceneList.get(good_match.trainIdx).pt);
        }

        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);

        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);

        Mat hg = Calib3d.findHomography(obj, scene);

        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

        obj_corners.put(0, 0, 0, 0);
        obj_corners.put(1, 0, img_object.cols(), 0);
        obj_corners.put(2, 0, img_object.cols(), img_object.rows());
        obj_corners.put(3, 0, 0, img_object.rows());

        Core.perspectiveTransform(obj_corners, scene_corners, hg);
        return img_matches;
        //Sauvegarde du résultat
//        System.out.println(String.format("Writing %s", pathResult));
//        Highgui.imwrite("src/OpenCV/" + pathResult, img_matches);
    }

//    private Image grabFrame()
//    {
//        // init everything
//        Image imageToShow = null;
//        Mat frame = new Mat();
//
//        // check if the capture is open
//            try
//            {
//                // if the frame is not empty, process it
//                if (!frame.empty())
//                {
//                    // init
//                    Mat blurredImage = new Mat();
//                    Mat hsvImage = new Mat();
//                    Mat mask = new Mat();
//                    Mat morphOutput = new Mat();
//                    // remove some noise
//                    Imgproc.blur(frame, blurredImage, new Size(7, 7));
//                    // convert the frame to HSV
//                    Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
//                    // get thresholding values from the UI
//                    // remember: H ranges 0-180, S and V range 0-255
//                    Scalar minValues = new Scalar(this.hueStart.getValue(), this.saturationStart.getValue(),
//                            this.valueStart.getValue());
//                    Scalar maxValues = new Scalar(this.hueStop.getValue(), this.saturationStop.getValue(),
//                            this.valueStop.getValue());
//                    // show the current selected HSV range
//                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
//                            + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
//                            + minValues.val[2] + "-" + maxValues.val[2];
//                    onFXThread(this.hsvValuesProp, valuesToPrint);
//                    // threshold HSV image to select tennis balls
//                    Core.inRange(hsvImage, minValues, maxValues, mask);
//                    // show the partial output
//                    onFXThread(this.maskImage.imageProperty(), mat2Image(mask));
//                    // morphological operators
//                    // dilate with large element, erode with small ones
//                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
//                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
//                    Imgproc.erode(mask, morphOutput, erodeElement);
//                    Imgproc.erode(mask, morphOutput, erodeElement);
//                    Imgproc.dilate(mask, morphOutput, dilateElement);
//                    Imgproc.dilate(mask, morphOutput, dilateElement);
//                    // show the partial output
//                    onFXThread(this.morphImage.imageProperty(), mat2Image(morphOutput));
//                    // find the tennis ball(s) contours and show them
//                    frame = findAndDrawBalls(morphOutput, frame);
//                    // convert the Mat object (OpenCV) to Image (JavaFX)
//                    imageToShow = mat2Image(frame);
//                }
//            }
//            catch (Exception e)
//            {
//                // log the (full) error
//                System.err.print("ERROR");
//                e.printStackTrace();
//            }
//        return imageToShow;
//    }
//    private static <T> void onFXThread(final ObjectProperty<T> property, final T value)
//    {
//        Platform.runLater(() -> property.set(value));
//    }
//    private void showLeftImage() {
//        this.leftImageButton.setDisable(true);
//        this.imageView.setImage(this.leftImage);
//        if (this.canny.isSelected()) {
//            this.imageView.setImage(mat2Image(doCanny(this.leftMat, threshold)));
//        }
//        // foreground detection
//        else if (this.dilateErode.isSelected()) {
//            this.imageView.setImage(mat2Image(doBackgroundRemoval(this.leftMat, inverse)));
////            this.leftMat = this.doBackgroundRemoval(leftMat);
//        } else if (this.sobel.isSelected()) {
//            this.imageView.setImage(mat2Image(doSobel(this.leftMat)));
//        } else {
//
//            // convert the Mat object (OpenCV) to Image (JavaFX)
//            this.leftImage = mat2Image(this.leftMat);
//            this.imageView.setImage(this.leftImage);
//        }
//        this.leftGroup.getChildren().add(this.imageView);
//        this.imageView.toBack();
//        this.group.getChildren().set(0, this.leftGroup);
//        this.rightImageButton.setDisable(false);
//    }
//    private void showRightImage() {
//        this.rightImageButton.setDisable(true);
//
//        this.imageView.setImage(this.rightImage);
//        if (this.canny.isSelected()) {
//            this.imageView.setImage(mat2Image(doCanny(this.rightMat, threshold)));
//        }
//        // foreground detection
//        else if (this.dilateErode.isSelected()) {
//            this.imageView.setImage(mat2Image(doBackgroundRemoval(this.rightMat, inverse)));
////            this.leftMat = this.doBackgroundRemoval(rightMat);
//        } else if (this.sobel.isSelected()) {
//            this.imageView.setImage(mat2Image(doSobel(this.rightMat)));
//        } else {
//
//            // convert the Mat object (OpenCV) to Image (JavaFX)
//            this.rightImage = mat2Image(this.rightMat);
//            this.imageView.setImage(this.rightImage);
//        }
//        this.rightGroup.getChildren().add(this.imageView);
//        this.imageView.toBack();
//        this.group.getChildren().set(0, this.rightGroup);
//        this.leftImageButton.setDisable(false);
//    }
}
