package net.unelement.sd.image;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.LeptonicaFrameConverter;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageCompute {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            String input = null;
            String output = null;

            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);

            int z = chooser.showOpenDialog(null);
            if (z == JFileChooser.APPROVE_OPTION) {
                input = chooser.getSelectedFile().getAbsolutePath();
            }

            if(input == null) System.exit(0);

            // Get image from path
            ImageIcon icon = new ImageIcon(input);

            // Transform the image
            BufferedImage image = ImageCompute.compute(
                    icon.getImage(), Color.white,
                    icon.getIconWidth(), icon.getIconHeight()
            );

            /*
            Error opening data file ./eng.traineddata
            Please make sure the TESSDATA_PREFIX environment variable is set to your "tessdata" directory.
            Failed loading language 'eng'
            Tesseract couldn't load any languages!
             */
            String text = ImageCompute.ocr(image, "/home/yves/tessdata/tessdata", "eng");
            System.out.println("Text: " + text);

            chooser.setDialogType(JFileChooser.SAVE_DIALOG);

            z = chooser.showSaveDialog(null);
            if (z == JFileChooser.APPROVE_OPTION) {
                output = chooser.getSelectedFile().getAbsolutePath();
            }

            if(output == null) System.exit(0);

            try{
                ImageIO.write(image, "png", new File(output));
            }catch(Exception _){

            }

            System.exit(0);
        });
    }


    private ImageCompute() { }

    public static BufferedImage colorFilter(Color preservedColor, BufferedImage image) {

        // Create values for the threshold min/max on preserved value
        // that can't be a lonely value but multiple blurred values
        // approaching the preserved value.
        // It's highly recommended to grayscale the image before using
        // this filter because grayscale offers no difference between
        // red, green or blue and a better application of this filter.
        // For the sake of your life, or maybe fot the sake of
        // treatment, I consider that you have gray-scaled your image
        // before using this filter.

        Color t1 = new Color(
                Math.max(0, preservedColor.getRed() - 10),
                Math.max(0, preservedColor.getGreen() - 10),
                Math.max(0, preservedColor.getBlue() - 10)
        );

        Color t2 = new Color(
                Math.min(255, preservedColor.getRed() + 10),
                Math.min(255, preservedColor.getGreen() + 10),
                Math.min(255, preservedColor.getBlue() + 10)
        );

        // Now we have determinate the threshold, and we can perform by apply
        // to rgb a range, we're ready to calculate the right rgb to return.

        BufferedImage im = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int rgb = image.getRGB(x, y);
                Color pixel = new Color(rgb);

                if(pixel.getRed() >= t1.getRed() && pixel.getRed() <= t2.getRed()){
                    im.setRGB(x, y, Color.black.getRGB()); // preserved
                }else{
                    im.setRGB(x, y, Color.white.getRGB());
                }

            }
        }

        return im;
    }

    public static BufferedImage grayFilter(BufferedImage image, int width, int height) {
        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                im.setRGB(x, y, grayFilter(new Color(image.getRGB(x, y))).getRGB());
            }
        }

        return im;
    }

    // https://stackoverflow.com/questions/18710560/how-to-convert-colors-to-grayscale-in-java-with-just-the-java-io-library
    public static Color grayFilter(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = green = blue = (int)(red * 0.299 + green * 0.587 + blue * 0.114);
        return new Color(red, green, blue);
    }

    public static BufferedImage toBufferedImage(Image image, int width, int height) {
        if (image instanceof BufferedImage) return (BufferedImage) image;

        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = im.createGraphics();

        g2d.drawImage(image, 0, 0, null);

        g2d.dispose();
        return im;
    }

    public static BufferedImage compute(Image image, Color textColor,
                                        int imageWidth, int imageHeight) {
        Image im;
        im = grayFilter(toBufferedImage(image, imageWidth, imageHeight), imageWidth, imageHeight);
        im = colorFilter(grayFilter(textColor), toBufferedImage(im, imageWidth, imageHeight));
        return toBufferedImage(im, imageWidth, imageHeight);
    }

    public static String ocr(BufferedImage img, String tessPath, String lang){
        String text;

        try(TessBaseAPI api = new TessBaseAPI();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            LeptonicaFrameConverter converter2 = new LeptonicaFrameConverter()
        ){
            BytePointer outText;

            if(api.Init(tessPath, lang) != 0) return "";

            PIX pix = converter2.convert(converter.convert(img));
            api.SetImage(pix);

            outText = api.GetUTF8Text();
            text = outText.getString();

            api.End();
            outText.deallocate();
            //pixDestroy(pix); // <- cause double free or corrupted (out) in leptonica
        }

        return text;
    }
}
