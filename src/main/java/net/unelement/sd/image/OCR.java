package net.unelement.sd.image;

import net.unelement.sd.image.event.OCREvent;
import net.unelement.sd.image.listener.OCRListener;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.LeptonicaFrameConverter;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import javax.swing.event.EventListenerList;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

// import static org.bytedeco.leptonica.global.leptonica.pixDestroy;

public class OCR implements Runnable {

    Java2DFrameConverter converter = new Java2DFrameConverter();
    LeptonicaFrameConverter converter2 = new LeptonicaFrameConverter();

    private final TessBaseAPI api;
    private BytePointer outText;

    private volatile Thread thread;
    private final AtomicBoolean working;
    private final AtomicBoolean running;

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

        working = new AtomicBoolean(false);
        running = new AtomicBoolean(false);
        done = true;
        image = null;

        api = new TessBaseAPI();
    }

    public OCR(String tessPath) {
        this(tessPath, "eng");
    }

    public void startThread() {
        working.set(true);
        running.set(true);
        thread = new Thread(this);
        thread.start();
    }

    private void stopThread() {
        if(thread != null && thread.isAlive()) {
            working.set(false);
            running.set(false);
            thread.interrupt();
        }
    }

    public void interruptThread() {
        running.set(false);
        working.set(false);
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

        // Pointer address is NULL for pix
        // Cn cause 'double free or corrupted (out)' in leptonica
        // Caution with your memory
        // pixDestroy(pix);

        if(!running.get()){
            stopThread();
        }
    }

    public void dispose(){
        api.End();
        if(outText != null){
            outText.deallocate();
        }
    }

    @Override
    public void run() {
        while(working.get()){
            if(running.get() && !done){
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

    protected void fireOcrEvent(OCREvent message){
        for(Object o : getListeners()){
            if(o instanceof OCRListener listen){
                listen.updateText(message);
                break;
            }
        }
    }

}
