package sample.MyZiImageLib;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;

import java.io.*;
import java.util.Vector;


import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * by Aleksej Tokarev 2011
 * @author Aleksej Tokarev
 * @version 1.0
 */
public class MPOSplitter {

    private static final int SOM = 0xFF;  // Start of Marke
    private static final int SOI = 0xD8;  // Start of Image
    private static final int EOI = 0xD9;  // Ende of Image
    private static final int APP0 = 0xE0;// Application  0
    private static final int APP1 = 0xE1;// Application  1
    private static final int APP2 = 0xE2;// Application  2
    private static final int APP3 = 0xE3;// Application  3
    private static final int APP4 = 0xE4;// Application  4
    private static final int APP5 = 0xE5;// Application  5
    private static final int APP6 = 0xE6;// Application  6
    private static final int APP7 = 0xE7;// Application  7
    private static final int APP8 = 0xE8;// Application  8
    private static final int APP9 = 0xE9;// Application  9
    private static final int APP10 = 0xEA;// Application 10
    private static final int APP11 = 0xEB;// Application 11
    private static final int APP12 = 0xEC;// Application 12
    private static final int APP13 = 0xED;// Application 13
    private static final int APP14 = 0xEE;// Application 14
    private static final int APP15 = 0xEF;// Application 15

    private boolean DEBUG = false;

    private int offset = -1;

    private Vector<Integer> starts = new Vector<>();
    private Vector<Integer> ends = new Vector<>();

    private FileInputStream fileInputStream = null;

    private File file = null;

    public void showDebug(boolean debug) {
        this.DEBUG = debug;
    }

    public synchronized void setSRC(String src) throws ImageToolException {
        setSRC(new File(src));
    }

    public synchronized void setSRC(File file) throws ImageToolException {
        if (!file.exists()) {
            throw new ImageToolException("UPS: File " + file + " is not found");
        }
        this.file = file;
    }

    public synchronized Size getDimension() {
        Mat im = imread(file.getAbsolutePath());
        return new Size(im.width(), im.height());
    }

    public synchronized long getSize() {
        return file.length();
    }

    private synchronized int getImageCount() {
        int sL = starts.size();
        int eL = ends.size();
        return (sL < eL) ? sL : eL;
    }

    private synchronized MatOfByte getInputStreamImageNr(int nr) throws ImageToolException {
        if (starts.size() > nr && ends.size() > nr) {
            int start = starts.get(nr);
            int length = (ends.get(nr) - start) + 1;

            // Input stream erstellen
            FileInputStream fis;
            try {
                fis = new FileInputStream(this.file);
            } catch (FileNotFoundException e) {
                throw new ImageToolException("UPS: File \"" + this.file + "\" not found. " + e.getMessage());
            }
            try {
                fis.skip(start);
            } catch (IOException e) {
                throw new ImageToolException("UPS: Exception by skip(" + start + ")" + e.getMessage());
            }

            byte[] byteArray = new byte[length];
            try {
                fis.read(byteArray);
            } catch (IOException e) {
                throw new ImageToolException("UPS: Exception by read(new byte[" + length + "])" + e.getMessage());
            }

            try {
                fis.close();
            } catch (IOException e) {
                throw new ImageToolException("UPS: Exception by closing" + e.getMessage());
            }

            return new MatOfByte(byteArray);
        } else {
            throw new ImageToolException("UPS: Image with ID " + nr + " not exists.");
        }
    }

    public synchronized Mat getImageNr(int nr) throws ImageToolException {
        if (starts.size() > nr && ends.size() > nr) {
            // Stream mit Bild in BufferedImage umwandeln
            return getInputStreamImageNr(nr);
        } else {
            throw new ImageToolException("UPS: Image with ID " + nr + " not exists.");
        }
    }

    public synchronized void saveImageNr(int nr, File out) throws IOException, ImageToolException {

        if (starts.size() > nr && ends.size() > nr) {
            int start = starts.get(nr);
            int length = (ends.get(nr) - start) + 1; // +1 weil Letzte Byte muss auch gespeichert werden

            FileInputStream fis = new FileInputStream(this.file);
            fis.skip(start);

            FileOutputStream fos = new FileOutputStream(out);

            byte[] buff;
            int buffSize = 1024;
            while (length > 0) {
                if (length > buffSize) {
                    buff = new byte[buffSize];
                } else {
                    buff = new byte[length];
                }
                fis.read(buff);
                fos.write(buff);
                length -= buff.length;
            }

            fos.flush();
            fos.close();
            fis.close();
        } else {
            throw new ImageToolException("UPS: Image with ID " + nr + " not exists. Images count = " + getImageCount());
        }
    }

    public synchronized void searchImages() throws IOException {

        if (file == null) {
            throw new IOException("No File");
        }

        fileInputStream = new FileInputStream(file);

        starts = new Vector<>();
        ends = new Vector<>();
        offset = -1;

        int b;
        while ((b = read1byte()) != -1) {
            // Start of Marke
            if (b == 0xFF) {
                //System.out.println("SOM");
                b = read1byte();

                // Start of Image
                if (b == SOI) {
                    if (DEBUG) System.out.println("SOI Off: " + ((offset) - 1));
                    starts.add(offset - 1); // Start von Bild speichern
                }

                // End of Image
                if (b == EOI) {
                    if (DEBUG) System.out.println("EOI Off: " + ((offset)));
                    ends.add(offset); // Ende von Bild speichern
                }

                // APP0
                if (b == APP0) {
                    if (DEBUG) System.out.println("APP0 Off: " + ((offset) - 1));
                    ignoreApp();
                    if (DEBUG) System.out.println("Off nach Sprung: " + ((offset) - 1));
                }

                // APP1
                if (b == APP1) {
                    if (DEBUG) System.out.println("APP1 Off: " + ((offset) - 1));
                    ignoreApp();
                    if (DEBUG) System.out.println("Off nach Sprung: " + ((offset) - 1));
                }

                // APP2
                if (b == APP2) {
                    if (DEBUG) System.out.println("APP2 Off: " + ((offset) - 1));
                    ignoreApp();
                    if (DEBUG) System.out.println("Off nach Sprung: " + ((offset) - 1));
                }

                // APP3
                if (b == APP3) {
                    if (DEBUG) System.out.println("APP3 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP4
                if (b == APP4) {
                    if (DEBUG) System.out.println("APP4 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP5
                if (b == APP5) {
                    if (DEBUG) System.out.println("APP5 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP6
                if (b == APP6) {
                    if (DEBUG) System.out.println("APP6 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP7
                if (b == APP7) {
                    if (DEBUG) System.out.println("APP7 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP8
                if (b == APP8) {
                    if (DEBUG) System.out.println("APP8 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP9
                if (b == APP9) {
                    if (DEBUG) System.out.println("APP9 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP10
                if (b == APP10) {
                    if (DEBUG) System.out.println("APP10 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP11
                if (b == APP11) {
                    if (DEBUG) System.out.println("APP11 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP12
                if (b == APP12) {
                    if (DEBUG) System.out.println("APP12 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP13
                if (b == APP13) {
                    if (DEBUG) System.out.println("APP13 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP14
                if (b == APP14) {
                    if (DEBUG) System.out.println("APP14 Off: " + ((offset) - 1));
                    ignoreApp();
                }

                // APP15
                if (b == APP15) {
                    if (DEBUG) System.out.println("APP15 Off: " + ((offset) - 1));
                    ignoreApp();
                }
            }
        }

        fileInputStream.close();

        if (DEBUG) {
            for (int i = 0; i < starts.size() && i < ends.size(); i++) {
                System.out.println("SOI: " + starts.get(i) + " EOI: " + ends.get(i));
            }
        }
    }

    private int read1byte() throws IOException {
        int r = fileInputStream.read();
        if (r != -1) {
            offset++;
        }
        return r;
    }

    private int read2byte() throws IOException {
        int b1 = read1byte();
        int b2 = -1;
        if (b1 != -1) {
            b2 = read1byte();
            if (b1 != -1) {
                if (DEBUG) System.out.println("2 Byte Offset: " + (offset - 1));
                if (DEBUG)
                    System.out.println("2 Byte: ohne versch: " + b1 + " erg " + ((b1 << 8) + (b2)) + " = " + (b1 << 8) + "+" + (b2));
                return (b1 << 8) + (b2);
            }
        }
        return -1;
    }

    private void jump(int len) throws IOException {
        //for(int i=0; i<len && read1byte()!=-1; i++);
        offset += fileInputStream.skip(len);
    }

    private void ignoreApp() throws IOException {
        int length = read2byte();
        if (length != -1) {
            if (DEBUG) System.out.println("Ignore: " + length);
            jump(length - 3);
        }
    }
}
