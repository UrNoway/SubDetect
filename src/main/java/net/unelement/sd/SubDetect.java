package net.unelement.sd;

import com.formdev.flatlaf.FlatLightLaf;
import net.unelement.sd.ffmpeg.*;
import net.unelement.sd.ffmpeg.event.VideoEvent;
import net.unelement.sd.ffmpeg.listener.VideoListener;
import net.unelement.sd.grid.XTablePanel;
import net.unelement.sd.image.ImageCompute;
import net.unelement.sd.image.OCR;
import net.unelement.sd.subtitle.SrtSubtitles;
import net.unelement.sd.subtitle.SubtitleEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SubDetect {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            System.out.println("UnElement Works :: SubDetect");
            FlatLightLaf.setup();
            SubDetectFrame frm = new SubDetectFrame();
            frm.setLocationRelativeTo(null);
            frm.setVisible(true);
        });
    }

    public static class SubDetectFrame extends JFrame {

        private final VideoViewer videoViewer;
        private final ProgressPanel progressPanel;
        private final XTablePanel xTablePanel;
        private final OCR ocr;
        private final Map<Long, String> detected;
        private SubtitleEvent subtitleEvent;
        private final SrtSubtitles srtSubtitles;

        public SubDetectFrame() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle("UnElement Works :: SubDetect (~git)");
            setSize(1900, 1000);
            createMenuBar();

            ocr = new OCR("/home/yves/tessdata/tessdata");
            if(!ocr.init()) throw new RuntimeException("Init not done!");
            ocr.startThread();
            detected = new HashMap<>();
            srtSubtitles = new SrtSubtitles();

            JPanel contentPane = new JPanel(new GridLayout(1, 2, 2, 2));

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(createToolBar(), BorderLayout.NORTH);
            getContentPane().add(contentPane, BorderLayout.CENTER);
            progressPanel = new ProgressPanel();
            getContentPane().add(progressPanel, BorderLayout.SOUTH);

            JPanel leftPane = new JPanel(new BorderLayout());
            JPanel rightPane = new JPanel(new BorderLayout());

            leftPane.setBorder(new LineBorder(new Color(0, 0, 0, 33)));
            rightPane.setBorder(new LineBorder(new Color(0, 0, 0, 33)));

            contentPane.add(leftPane);
            contentPane.add(rightPane);

            videoViewer = new VideoViewer(ocr, progressPanel);
            xTablePanel = new XTablePanel();

            leftPane.add(videoViewer, BorderLayout.CENTER);
            rightPane.add(xTablePanel, BorderLayout.CENTER);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);

                    ocr.interruptThread();
                    ocr.dispose();
                    progressPanel.dispose();
                }
            });

            ocr.addOcrListener((event) -> {
                String text = event.getText();
                if(!text.isEmpty()){
                    detected.put(event.getMicroseconds(), text);
                }
            });
        }

        private void createMenuBar() {
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenu aboutMenu = new JMenu("About");
            JMenuItem exitItem = new JMenuItem("Quit");
            exitItem.addActionListener(e -> System.exit(0));
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);
            menuBar.add(aboutMenu);

            setJMenuBar(menuBar);
        }

        private JToolBar createToolBar() {
            JToolBar toolBar = new JToolBar();

            ImageIcon iNew = new ImageIcon(
                    Objects.requireNonNull(getClass()
                            .getResource("/images/48_newdocument.png")));
            JButton btnNewDoc = new JButton(iNew);
            btnNewDoc.addActionListener(e -> videoViewer.open());
            toolBar.add(btnNewDoc);

            ImageIcon iOpen = new ImageIcon(
                    Objects.requireNonNull(getClass()
                            .getResource("/images/48_folder.png")));
            JButton btnOpen = new JButton(iOpen);
            btnOpen.addActionListener(e -> {});
            toolBar.add(btnOpen);

            ImageIcon iSave = new ImageIcon(
                    Objects.requireNonNull(getClass()
                            .getResource("/images/48_floppydisk.png")));
            JButton btnSave = new JButton(iSave);
            btnSave.addActionListener(e -> {});
            toolBar.add(btnSave);

            JSeparator sep01 = new JSeparator(JSeparator.VERTICAL);
            toolBar.add(sep01);

            ImageIcon iPlay = new ImageIcon(
                    Objects.requireNonNull(getClass()
                    .getResource("/images/48_timer_stuffs play.png")));
            JButton btnPlay = new JButton(iPlay);
            btnPlay.addActionListener(e -> videoViewer.getFFMpeg().play());
            toolBar.add(btnPlay);

            ImageIcon iPause = new ImageIcon(
                    Objects.requireNonNull(getClass()
                            .getResource("/images/48_timer_stuffs pause.png")));
            JButton btnPause = new JButton(iPause);
            btnPause.addActionListener(e -> videoViewer.getFFMpeg().pause());
            toolBar.add(btnPause);

            ImageIcon iStop = new ImageIcon(
                    Objects.requireNonNull(getClass()
                            .getResource("/images/48_timer_stuffs stop.png")));
            JButton btnStop = new JButton(iStop);
            btnStop.addActionListener(e -> videoViewer.getFFMpeg().stop());
            toolBar.add(btnStop);

            return  toolBar;
        }

        public static class VideoViewer extends JPanel {
            private int currentFrame = 0;
            private long currentTime = 0;
            private double fps = 0d;

            private BufferedImage image;
            private final FFMpeg ff;

            public VideoViewer(OCR ocr, ProgressPanel progressPanel) {
                image = null;

                ff = new FFMpeg();
                ff.addMediaListener(new VideoListener() {
                    @Override
                    public void videoFrameUpdated(VideoEvent event) {
                        image = event.getImage();
                        currentFrame = event.getFrame();
                        currentTime = event.getCurrentMicro();
                        fps = event.getFps();

                        // Create a cleaned image
                        BufferedImage im = ImageCompute.compute(event.getImage(), Color.white, event.getImage().getWidth(), event.getImage().getHeight());
                        ocr.load(im, currentTime, currentFrame, ff.getMediaFrameCount());

                        progressPanel.updateValues(currentFrame + 1, ff.getMediaFrameCount());

                        repaint();

                        if(currentFrame + 1 == ff.getMediaFrameCount()) {
                            JOptionPane.showMessageDialog(null, "EOF");
                        }
                    }
                });
            }

            public FFMpeg getFFMpeg(){
                return ff;
            }

            public int getCurrentFrame() {
                return currentFrame;
            }

            public long getCurrentTime() {
                return currentTime;
            }

            public double getFps() {
                return fps;
            }

            public void open(){
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setDialogTitle("Open Video");
                chooser.setApproveButtonText("Open");

                int z = chooser.showOpenDialog(this);
                if (z == JFileChooser.APPROVE_OPTION) {
                    try{
                        ff.setMedia(chooser.getSelectedFile().getAbsolutePath());
                        repaint();
                    }catch(Exception e){
                        JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setColor(Color.lightGray);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                if(image != null){
                    float ratioX = (float)getWidth()/(float)image.getWidth();
                    float ratioY = (float)getHeight()/(float)image.getHeight();
                    float ratio = Math.min(ratioX, ratioY);

                    int w = Math.round(image.getWidth()*ratio);
                    int h = Math.round(image.getHeight()*ratio);

                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2d.drawImage(image, x, y, w, h, null);
                }

                g2d.dispose();
            }
        }

        public static class ProgressPanel extends JPanel implements Runnable {

            private final JProgressBar progressBar;
            private final JLabel lblCurFrame;
            private final JLabel lblTotalFrame;
            private int currentFrame;
            private int frameCount;

            private final Thread thread;
            private final AtomicBoolean working;
            private final AtomicBoolean updated;

            public ProgressPanel(){
                progressBar = new JProgressBar();
                lblCurFrame = new JLabel("0");
                JLabel lblSlash = new JLabel("/");
                lblTotalFrame = new JLabel("0");
                JPanel pEast = new JPanel(null);

                progressBar.setMinimum(0);
                currentFrame = 0;
                frameCount = 0;
                working = new AtomicBoolean(true);
                updated = new AtomicBoolean(false);

                thread = new Thread(this);
                thread.start();

                lblCurFrame.setHorizontalAlignment(JLabel.CENTER);
                lblSlash.setHorizontalAlignment(JLabel.CENTER);
                lblTotalFrame.setHorizontalAlignment(JLabel.CENTER);

                pEast.setPreferredSize(new Dimension(222, 22));
                pEast.add(lblCurFrame);
                pEast.add(lblSlash);
                pEast.add(lblTotalFrame);

                lblCurFrame.setSize(100, 22);
                lblSlash.setSize(22, 22);
                lblTotalFrame.setSize(100, 22);

                lblCurFrame.setLocation(0, 0);
                lblSlash.setLocation(100, 0);
                lblTotalFrame.setLocation(122, 0);

                lblCurFrame.setVisible(true);
                lblSlash.setVisible(true);
                lblTotalFrame.setVisible(true);

                setLayout(new BorderLayout());
                add(pEast, BorderLayout.EAST);
                add(progressBar, BorderLayout.CENTER);


            }

            public void dispose(){
                if(thread != null && thread.isAlive()){
                    working.set(false);
                    thread.interrupt();
                }
            }

            public void updateValues(int cur, int total){
                lblCurFrame.setText(Integer.toString(cur));
                lblTotalFrame.setText(Integer.toString(total));
                currentFrame = cur;
                frameCount = total;
                updated.set(true); // update JProgress
            }

            @Override
            public void run() {
                while(working.get()){
                    if(updated.get()){
                        progressBar.setMaximum(frameCount);
                        progressBar.setValue(currentFrame);
                        updated.set(false);
                    }
                }
            }

        }
    }
}
