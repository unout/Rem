package sample;

import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/* Created on 07.10.2016.*/
public class ExtendedPoint extends Point2D {

    private String text;
    public Circle circle;
    public Text textOnImage;

    public ExtendedPoint(double x, double y, Paint dotColor, Paint textColor) {
        super(x, y);
        this.text = getX() + "; " + getY();
        this.circle = new Circle(getX(), getY(), 0.5, dotColor);
        this.textOnImage = new Text(getX(), getY(), this.text);
        this.textOnImage.setFill(textColor);
    }

    ExtendedPoint(double x, double y, String text, Paint dotColor, Paint textColor) {
        super(x, y);
        this.text = text;
        this.circle = new Circle(getX(), getY(), 2, dotColor);
        this.textOnImage = new Text(getX(), getY(), this.text);
        this.textOnImage.setFill(textColor);
    }

    ExtendedPoint(Point2D p, String text, Paint dotColor, Paint textColor) {
        super(p.getX(), p.getY());
        this.text = text;
        this.circle = new Circle(getX(), getY(), 2, dotColor);
        this.textOnImage = new Text(getX(), getY(), this.text);
        this.textOnImage.setFill(textColor);
    }

    Point2D getPoint() {
        return new Point2D(getX(), getY());
    }

//    void remove(Group group) {
//        group.getChildren().remove()
//    }
    
    @Override
    public String toString() {
        return "[" + getX() + "; " + getY() + "]   Text: " + this.text + ".";
    }
}
