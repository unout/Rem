package sample.MyZiImageLib;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Klasse fuer Erstellung von Stereobilder<br><br>
 * <p>
 * <b>Lizenz:</b><br>
 * Diese Klasse wird kostenfrei verbreitet und darf geaendert werden.<br><br>
 * <p>
 * <b>Copyright:</b><br>
 * by Aleksej Tokarev 2011
 *
 * @author Aleksej Tokarev
 * @version 1.0
 * @see http://atoks.bplaced.net
 */
class StereoImage extends ImageTool {

    static final int STEREO_COLUMN_INTERLEAVE_COLOR = 5;
    static final int STEREO_ROW_INTERLEAVE_COLOR = -5;
    static final int STEREO_SIDE_BY_SIDE_COLOR = 6;
    static final int STEREO_UP_DOWN_COLOR = -6;

    private BufferedImage left = null;
    private BufferedImage right = null;
    private int transferX = 0;
    private int transferY = 0;

    /**
     * Statndart konstruktor
     */
    public StereoImage() {
    }

    /**
     * Konstruktor mit eingabe von Quellbilder
     *
     * @param left  Linke Bild
     * @param right Rechte Bild
     */
    public StereoImage(BufferedImage left, BufferedImage right) {
        this.setLeftImage(left);
        this.setRightImage(right);
    }

    /**
     * Methode gibt linke bild zurueck<br>
     *
     * @return linke Bild
     */
    public BufferedImage getLeftImage() {
        return left;
    }

    /**
     * Methode tauscht linke und rechte Bilder
     */
    public void swapLeftrRightImage() {
        BufferedImage temp = left;
        left = right;
        right = temp;
    }

    /**
     * Methode setzt linke Bild
     *
     * @param left
     */
    private void setLeftImage(BufferedImage left) {
        this.setTransferX(0);
        this.setTransferY(0);
        this.left = left;
        //System.out.println("LW : "+left.getWidth());
    }

    /**
     * Methode gibt rechte bild zurueck<br>
     *
     * @return rechte Bild
     */
    public BufferedImage getRightImage() {
        return right;
    }

    /**
     * @param right
     */
    private void setRightImage(BufferedImage right) {
        this.setTransferX(0);
        this.setTransferY(0);
        this.right = right;
        //System.out.println("RW : "+right.getWidth());
    }

    /**
     * Methode gibt actuelle X-Verschiebung zurueck
     * @return
     */
    public int getTransferX() {
        return transferX;
    }

    /**
     * Methode setzt X-Verschiebung
     * @param transferX
     */
    private void setTransferX(int transferX) {
        this.transferX = transferX;
    }

    /**
     * Methode gibt actuelle Y-Verschiebung zurueck
     * @return
     */
    public int getTransferY() {
        return transferY;
    }

    /**
     * Methode setzt Y-Verschiebung
     *
     * @param transferY
     */
    private void setTransferY(int transferY) {
        this.transferY = transferY;
    }

    /**
     * Methode erstelt eine Stereobild
     * @param stereo_type Typ von Stereobild<br>
     *                    <b>Moegliche Typen:</b><br>
     *                    ANAGLYPH_RAD_CYAN_COLOR (defined in ImageTool)<br>
     *                    ANAGLYPH_RAD_CYAN_GRAY (defined in ImageTool)<br>
     *                    ANAGLYPH_RAD_GREEN_COLOR (defined in ImageTool)<br>
     *                    ANAGLYPH_RAD_GREEN_GRAY (defined in ImageTool)<br>
     *                    ANAGLYPH_RAD_BLUE_COLOR (defined in ImageTool)<br>
     *                    ANAGLYPH_RAD_BLUE_GRAY (defined in ImageTool)<br>
     *                    ANAGLYPH_YELLOW_BLUE_COLOR (defined in ImageTool)<br>
     *                    ANAGLYPH_YELLOW_BLUE_GRAY (defined in ImageTool)<br>
     *                    STEREO_COLUMN_INTERLEAVE_COLOR (defined in StereoImage)<br>
     *                    STEREO_ROW_INTERLEAVE_COLOR (defined in StereoImage)<br>
     *                    STEREO_SIDE_BY_SIDE_COLOR (defined in StereoImage)<br>
     *                    STEREO_UP_DOWN_COLOR (defined in StereoImage)<br>
     * @return Zusammen gesetzte Stereobild
     * @throws ImageToolException
     */
    @SuppressWarnings("static-access")
    public BufferedImage getResultImage(int stereo_type) throws ImageToolException {
        if (left == null) {
            throw new ImageToolException("UPS: Left Image is NULL (Image may not be a NULL)");
        }

        if (right == null) {
            throw new ImageToolException("UPS: Right Image is NULL (Image may not be a NULL)");
        }

        // Bildergroesse wenn noetig anpassen
        BufferedImage[] bilder = this.getAnpassteAnGroesseBilder();

        // Bilderverschiebung anpassen
        if (this.transferX != 0 || this.transferY != 0) {
            bilder = getAnpassteAnVeschiebungBilder(bilder, stereo_type);
        }

        // Wenn Stereobild muss von Anaglyph erzeugt werden
        // Stereotypen zwischen -4 und 4 gehoeren zu Anaglyph
        if (stereo_type > -5 && stereo_type < 5) {
            return this.getAnaglyphImage(bilder[0], bilder[1], stereo_type);
        } else if (stereo_type > -7 && stereo_type < 7) { // Stereotypen zwischen -6 und 6 gehoeren zu INTERLEAVE

            if (stereo_type == STEREO_COLUMN_INTERLEAVE_COLOR)
                return getColumnIntervaleImage(bilder[0], bilder[1]);
            if (stereo_type == STEREO_ROW_INTERLEAVE_COLOR)
                return getRowIntervaleImage(bilder[0], bilder[1]);
            if (stereo_type == STEREO_SIDE_BY_SIDE_COLOR)
                return getSideBySideImage(bilder[0], bilder[1]);
            if (stereo_type == STEREO_UP_DOWN_COLOR)
                return getUpDownImage(bilder[0], bilder[1]);

            throw new ImageToolException("UPS: Stereo Type is invalid (Pleas use valid value bitween -6 and 6)");
        } else {
            throw new ImageToolException("UPS: Stereo Type is invalid (Pleas use valid value bitween -6 and 6)");
        }
    }

    /**
     * Methode erstelt eine Stereobild mit Eingabe von Colormatizen
     *
     * @param leftMatrix  Matrize fuer linke Bild
     * @param rightMatrix Matrize fuer rechte Bild
     * @return Zusammen gesetzte Stereobild
     * @throws ImageToolException
     */
    public BufferedImage getResultImage(double[][] leftMatrix, double[][] rightMatrix) throws ImageToolException {
        if (left == null) {
            throw new ImageToolException("UPS: Left Image is NULL (Image may not be a NULL)");
        }

        if (right == null) {
            throw new ImageToolException("UPS: Right Image is NULL (Image may not be a NULL)");
        }

        // Bildergroesse wenn noetig anpassen
        BufferedImage[] bilder = this.getAnpassteAnGroesseBilder();

        // Bilderverschiebung anpassen
        if (this.transferX != 0 || this.transferY != 0) {
            bilder = getAnpassteAnVeschiebungBilder(bilder, 0); // Null heisst, dass Type wird von Matrizen difeniert
        }

        return ImageTool.getAnaglyphImage(bilder[0], bilder[1], leftMatrix, rightMatrix);
    }

    /*
     * Methode Erzeugt eine Stereobild UP_DOWN
     */
    private BufferedImage getUpDownImage(BufferedImage l, BufferedImage r) throws ImageToolException {
        int width = Math.max(l.getWidth(), r.getWidth());
        int height = Math.max(l.getHeight(), r.getHeight());
        BufferedImage udi = new BufferedImage(width, height * 2, l.getType());

        Graphics2D gUdi = (Graphics2D) udi.getGraphics();

        // Bild von 2 Bilder zusammen setzen
        gUdi.drawImage(l, 0, 0, null);
        gUdi.drawImage(r, 0, l.getHeight(), null);

        gUdi.dispose();
        return ImageTool.adaptImage(udi, width, height * 2);

    }

    /*
     *  Methode Erzeugt eine Stereobild SIDE_BY_SIDE
     */
    private BufferedImage getSideBySideImage(BufferedImage l, BufferedImage r) throws ImageToolException {
        int width = Math.max(l.getWidth(), r.getWidth());
        int height = Math.max(l.getHeight(), r.getHeight());
        BufferedImage sbsi = new BufferedImage(width * 2, height, l.getType());

        //System.out.println("width : "+width*2+" new : "+sbsi.getWidth()+" h: "+height);

        Graphics2D gSbsi = (Graphics2D) sbsi.getGraphics();

        // Bild von 2 Bilder zusammen setzen
        gSbsi.drawImage(l, 0, 0, null);
        gSbsi.drawImage(r, l.getWidth(), 0, null);

        gSbsi.dispose();
        return ImageTool.adaptImage(sbsi, width * 2, height);
    }

    /*
     *  Methode Erzeugt eine Stereobild ROW_INTERLEAVE
     */
    private BufferedImage getRowIntervaleImage(BufferedImage l, BufferedImage r) {
        int width = Math.max(l.getWidth(), r.getWidth());
        int height = Math.max(l.getHeight(), r.getHeight());
        BufferedImage rii = new BufferedImage(width, height, l.getType());

        Graphics2D gRii = (Graphics2D) rii.getGraphics();
        boolean leftRow = false;

        // Bild von 2 Bilder zusammen setzen
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                if (leftRow)
                    gRii.setColor(new Color(l.getRGB(w, h)));
                else
                    gRii.setColor(new Color(r.getRGB(w, h)));

                gRii.drawLine(w, h, w, h);
            }
            leftRow = !leftRow;
        }

        gRii.dispose();
        return rii;
    }

    /*
     *  Methode Erzeugt eine Stereobild COLUMN_INTERLEAVE
     */
    private BufferedImage getColumnIntervaleImage(BufferedImage l, BufferedImage r) {
        int width = Math.max(l.getWidth(), r.getWidth());
        int height = Math.max(l.getHeight(), r.getHeight());
        BufferedImage cii = new BufferedImage(width, height, l.getType());

        Graphics2D gCii = (Graphics2D) cii.getGraphics();
        boolean leftColomn = false;

        // Bild von 2 Bilder zusammen setzen
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                if (leftColomn)
                    gCii.setColor(new Color(l.getRGB(w, h)));
                else
                    gCii.setColor(new Color(r.getRGB(w, h)));

                gCii.drawLine(w, h, w, h);
            }
            leftColomn = !leftColomn;
        }

        gCii.dispose();
        return cii;
    }

    // Methode past Bilder an Verschiebung an
    // Wenn type = 0 => Type wurde von Matrix difeniert und Kann als Anaglyph angepast werden
    // Bei Anaglyph/INTERLEAVE-Verschiebung wird von gesamte Groesse des Bildes Verschibung abgezogen und Bilder werden entsprechend verschoben
    // Bei SIDE_BY_SYDE/UP_DOWN Bild wird vergroessert bzw verklienet
    private BufferedImage[] getAnpassteAnVeschiebungBilder(BufferedImage[] bilder, int type) {
        // Wenn beide Bilder eingegeben sind
        if (bilder[0] != null && bilder[1] != null) {

            // Wenn Type ausgewaelt bei welchem muss Groesse vergroessert weden
            if (type == StereoImage.STEREO_SIDE_BY_SIDE_COLOR || type == StereoImage.STEREO_UP_DOWN_COLOR) {


            } else {
                int newWidth = bilder[0].getWidth() - Math.abs(this.transferX);
                int newHeight = bilder[0].getHeight() - Math.abs(this.transferY);

                // Tempblider erstellen
                BufferedImage tempLeft = new BufferedImage(newWidth, newHeight, bilder[0].getType());
                BufferedImage tempRight = new BufferedImage(newWidth, newHeight, bilder[1].getType());

                Graphics2D gL = (Graphics2D) tempLeft.getGraphics();
                Graphics2D gR = (Graphics2D) tempRight.getGraphics();

                // Verschiebung asrechnen
                //System.out.println("x: "+transferX);
                //System.out.println("y: "+transferY);

                int vXl = this.transferX > 0 ? this.transferX * -1 : 0;
                int vYl = this.transferY > 0 ? 0 : this.transferY;

                int vXr = this.transferX < 0 ? this.transferX : 0;
                int vYr = this.transferY < 0 ? 0 : this.transferY * -1;

                // Bilder malen
                gL.drawImage(bilder[0], vXl, vYl, null);
                gR.drawImage(bilder[1], vXr, vYr, null);

                gL.dispose();
                gR.dispose();

                return new BufferedImage[]{tempLeft, tempRight};
            }

        }
        return new BufferedImage[]{bilder[0], bilder[1]};
    }

    // Methode past Bilder an Verschiebungen oder unterschiedliche groessen an
    // Bei unterschiedlichen groessen wird groesste Groesse genommen und kleine Bild wird um schwarze Umramung vergroesert
    private BufferedImage[] getAnpassteAnGroesseBilder() {
        // Wenn beide Bilder eingegeben sind
        if (left != null && right != null) {
            // Wenn Bilder mit unterschiedlichen Groessen
            if (left.getWidth() != right.getHeight() || left.getHeight() != right.getHeight()) {
                // Bilder Dimension abspeichern
                int newWidth = Math.max(left.getWidth(), right.getWidth());
                int newHeight = Math.max(left.getHeight(), right.getHeight());

                // Tempblider erstellen
                BufferedImage tempLeft = new BufferedImage(newWidth, newHeight, left.getType());
                BufferedImage tempRight = new BufferedImage(newWidth, newHeight, right.getType());

                Graphics2D gL = (Graphics2D) tempLeft.getGraphics();
                Graphics2D gR = (Graphics2D) tempRight.getGraphics();

                // Bildzentrierung ausrechnen
                int leftVerschibungX = Math.round((newWidth - left.getWidth()) / 2);
                int leftVerschibungY = Math.round((newHeight - left.getHeight()) / 2);

                int rightVerschibungX = Math.round((newWidth - right.getWidth()) / 2);
                int rightVerschibungY = Math.round((newHeight - right.getHeight()) / 2);

                // Bilder anpassen
                gL.drawImage(left, leftVerschibungX, leftVerschibungY, null);
                gR.drawImage(right, rightVerschibungX, rightVerschibungY, null);

                gL.dispose();
                gR.dispose();

                return new BufferedImage[]{tempLeft, tempRight};
            }
        }
        return new BufferedImage[]{left, right};
    }

}
