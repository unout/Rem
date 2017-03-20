package sample.MyZiImageLib;
/**
 * Klasse mit Exception
 *
 * @author Aleksej Tokarev
 * @version 1.0
 */
public class ImageToolException extends Exception{
    private static final long serialVersionUID = 1L;

    public ImageToolException(){}

    ImageToolException(String massage){
        super(massage);
    }

}
