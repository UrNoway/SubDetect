package net.unelement.sd.image;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.LeptonicaFrameConverter;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import javax.swing.event.EventListenerList;
import java.awt.image.BufferedImage;

import static org.bytedeco.leptonica.global.leptonica.pixDestroy;

public class OCR implements Runnable {

    Java2DFrameConverter converter = new Java2DFrameConverter();
    LeptonicaFrameConverter converter2 = new LeptonicaFrameConverter();

    private final TessBaseAPI api;
    private BytePointer outText;
    private PIX pix;

    private volatile Thread thread;
    private volatile boolean running;

    private final String tessPath;
    private final String lang;

    private volatile boolean done;
    private BufferedImage image;
    private long microseconds;
    private int currentFrame;
    private int frameCount;

    public OCR(String tessPath, String lang) {
        this.tessPath = tessPath;
        this.lang = lang;

        running = false;
        done = true;
        image = null;

        api = new TessBaseAPI();
    }

    public OCR(String tessPath) {
        this(tessPath, "eng");
    }

    public void startThread() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    private void stopThread() {
        if(thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public void interruptThread() {
        running = false;
    }

    public boolean init(){
        return api.Init(tessPath, lang) == 0;
    }

    public void load(BufferedImage image, long microseconds, int currentFrame, int frameCount) {
        this.image = image;
        this.microseconds = microseconds;
        this.currentFrame = currentFrame;
        this.frameCount = frameCount;
        done = false;
    }

    public void doWork(){
        PIX pix = converter2.convert(converter.convert(image));
        api.SetImage(pix);

        outText = api.GetUTF8Text();
        fireOcrEvent(new OCREvent(outText.getString(), microseconds, currentFrame, frameCount));

        done = true;

        if(!running){
            stopThread();
        }
    }

    public void dispose(){
        api.End();
        if(outText != null){
            outText.deallocate();
            pixDestroy(pix); // <- cause double free or corrupted (out) in leptonica
        }
    }

    @Override
    public void run() {
        while(true){
            if(running && !done){
                doWork();
            }
        }
    }

    //=======================================================
    //=======================================================
    //-------------------------------------------------------
    // EVENTS
    //=======================================================

    private final EventListenerList listeners = new EventListenerList();

    public Object[] getListeners(){
        return listeners.getListenerList();
    }

    public void addOcrListener(OCRListener listener){
        listeners.add(OCRListener.class, listener);
    }

    public void removeOcrListener(OCRListener listener){
        listeners.remove(OCRListener.class, listener);
    }

    protected void fireOcrEvent(OCREvent message){
        for(Object o : getListeners()){
            if(o instanceof OCRListener listen){
                listen.updateText(message);
                break;
            }
        }
    }

}
