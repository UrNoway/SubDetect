package net.unelement.sd;

import com.formdev.flatlaf.FlatLightLaf;
import net.unelement.sd.ffmpeg.*;
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
import java.util.Objects;

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
        private final PositionView positionView;
        private final OCR ocr;
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
            srtSubtitles = new SrtSubtitles();

            JPanel contentPane = new JPanel(new GridLayout(1, 2, 2, 2));

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(createToolBar(), BorderLayout.NORTH);
            getContentPane().add(contentPane, BorderLayout.CENTER);

            JPanel leftPane = new JPanel(new BorderLayout());
            JPanel rightPane = new JPanel();

            leftPane.setBorder(new LineBorder(new Color(0, 0, 0, 33)));
            rightPane.setBorder(new LineBorder(new Color(0, 0, 0, 33)));

            contentPane.add(leftPane);
            contentPane.add(rightPane);

            videoViewer = new VideoViewer(ocr);
            positionView = new PositionView(videoViewer);
            leftPane.add(videoViewer, BorderLayout.CENTER);
            leftPane.add(positionView, BorderLayout.SOUTH);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);

                    ocr.stopThread();
                    ocr.dispose();
                }
            });

            ocr.addOcrListener((event) -> {
                String text = event.getText();
                if(subtitleEvent == null && !text.isEmpty()){
                    subtitleEvent = new SubtitleEvent();
                    subtitleEvent.setMicrosStart(event.getMicroseconds());
                    subtitleEvent.setText(text);
                }else if(subtitleEvent != null && text.isEmpty()){
                    subtitleEvent.setMicrosEnd(event.getMicroseconds());
                    srtSubtitles.getSubtitleEvents().add(subtitleEvent);

                    subtitleEvent = null;
                }
            });

            videoViewer.getFFMpeg().addMediaListener(new FFAdapter(){
                @Override
                public void endOfFile(EofEvent event) {
                    JOptionPane.showMessageDialog(new JFrame(), "End of file");
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                    chooser.setDialogTitle("Choose a file");

                    int z = chooser.showSaveDialog(null);
                    if (z == JFileChooser.APPROVE_OPTION) {
                        String path = chooser.getSelectedFile().getAbsolutePath();
                        srtSubtitles.writeSRT(path);
                        srtSubtitles.getSubtitleEvents().clear();
                    }
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

            public VideoViewer(OCR ocr){
                image = null;

                ff = new FFMpeg();
                ff.addMediaListener(new FFAdapter() {
                    @Override
                    public void updated(FFEvent event) {
                        image = event.getImage();
                        currentFrame = event.getFrame();
                        currentTime = event.getCurrentMicro();
                        fps = event.getFps();

                        // Create a cleaned image
                        BufferedImage im = ImageCompute.compute(event.getImage(), Color.white, event.getImage().getWidth(), event.getImage().getHeight());
                        ocr.load(im, currentTime);

                        repaint();
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

        public static class PositionView extends JPanel {

            private int position;
            private final JSlider slider;
            private final JTextField tfPosition;

            public PositionView(VideoViewer videoViewer){
                position = 0;
                slider = new JSlider();
                slider.addChangeListener(e -> {
                    double fps = videoViewer.getFps();
                    if(videoViewer.getFFMpeg().isReady() && fps != 0d){
                        position = slider.getValue();
                        long sTime = Math.round(position / Math.max(1d, fps));
                        videoViewer.getFFMpeg().setStartTime(sTime * 1_000_000L - 10);
                        videoViewer.getFFMpeg().setEndTime(sTime * 1_000_000L);
                        videoViewer.getFFMpeg().play();
                    }
                });
                slider.setMinimum(0);
                tfPosition = new JTextField("0");
                tfPosition.setHorizontalAlignment(JTextField.CENTER);
                JButton btnPosition = new JButton("Go to position");
                btnPosition.addActionListener(e -> {
                    try{
                        double fps = videoViewer.getFps();
                        if(videoViewer.getFFMpeg().isReady() && fps != 0d){
                            position = Integer.parseInt(tfPosition.getText());
                            slider.setValue(position);
                            long sTime = Math.round(position / Math.max(1d, fps));
                            videoViewer.getFFMpeg().setStartTime(sTime * 1_000_000L - 10);
                            videoViewer.getFFMpeg().setEndTime(sTime * 1_000_000L);
                            videoViewer.getFFMpeg().play();
                        }
                    }catch(Exception ex){
                        JOptionPane.showMessageDialog(new JFrame(), ex.getMessage());
                    }
                });

                JPanel topPanel = new JPanel(new GridLayout(1, 2));
                topPanel.add(tfPosition);
                topPanel.add(btnPosition);

                setLayout(new GridLayout(2, 1));
                add(topPanel);
                add(slider);
            }

            public void updatePosition(int position){
                this.position = position;
                slider.setValue(position);
                tfPosition.setText(String.valueOf(position));
            }

            public int getPosition(){
                return position;
            }

            public void updateMaxFrame(int frame){
                slider.setMaximum(frame);
            }
        }
    }
}
