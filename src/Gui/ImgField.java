package Gui;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ImgField {
    private final HashMap<String, BufferedImage> images;
    private final JLabel label = new JLabel();

    private final JButton newImage;

    private JLabel imgLabel;
    private final JScrollBar bar;

    private final HashSet<JButton> buttons = new HashSet<>();

    public ImgField(JLabel back, HashMap<String, BufferedImage> images, int y){
        this.images = images;
        int WIDTH = MATH.mult(back.getWidth(), 0.4);
        int HEIGHT = back.getHeight() - y;

        label.setBounds(MATH.mult(back.getWidth(), 0.6) - 1, y, WIDTH, HEIGHT);
        label.setIcon(new ImageIcon(Gui.newBuffImgColor(new Color(255,188,219), WIDTH, HEIGHT)));
        back.add(label);

        bar = new JScrollBar();
        label.add(bar);
        bar.setMaximum(0);
        bar.setVisible(true);
        bar.setUnitIncrement(24);
        bar.setBounds(0,0,MATH.mult(WIDTH, 0.03),MATH.mult(label.getHeight(),0.88));
        bar.addAdjustmentListener(e -> {
            if(imgLabel == null)
                return;

            imgLabel.setLocation(imgLabel.getX(),-bar.getValue());

            imgLabel.setSize(imgLabel.getWidth(), bar.getHeight() + bar.getValue());
        });

        label.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                bar.setValue(bar.getValue() + e.getWheelRotation() * bar.getUnitIncrement());
            }
        });

        reloadImgs();

        FileChooser fileChooser = new FileChooser();
        fileChooser.addActionListener(e -> {
            if(e.getActionCommand().equals("ApproveSelection")){
                File file = fileChooser.getFile();

                try{
                    File[] imgs;
                    if(file.isDirectory()){
                        imgs = file.listFiles();
                    }
                    else{
                        imgs = new File[]{file};
                    }

                    for(File f : imgs){
                        BufferedImage img = ImageIO.read(f);
                        String imgName = file.getName();

                        if(!imgName.endsWith(".png") && !imgName.endsWith(".jpg") && !imgName.endsWith(".jpeg"))
                            continue;

                        imgName = imgName.replaceAll(".png","");
                        imgName = imgName.replaceAll(".jpg","");
                        imgName = imgName.replaceAll(".jpeg","");

                        images.put(imgName, img);
                    }

                }
                catch(Exception i){
                    i.printStackTrace();
                }
            }

            reloadImgs();
        });

        newImage = new JButton("neues Bild");
        newImage.setLocation(bar.getX() + bar.getWidth() + 5, imgLabel.getHeight() + 10);
        newImage.setSize(imgLabel.getWidth(), MATH.mult(label.getHeight(),0.05));
        label.add(newImage);
        newImage.setFocusable(false);
        newImage.addActionListener(e -> {
            fileChooser.setCurrentDirectory(new File("C:\\Users\\"+ System.getProperty("user.name") +"\\Pictures"));
            fileChooser.setVisible();
        });
    }

    private Thread loadThread;
    private void reloadImgs(){
        if(loadThread != null)
            if(loadThread.isAlive())
                loadThread.stop();

        if(imgLabel != null)
            imgLabel.removeAll();

        buttons.clear();
        HashSet<JLabel> labels = new HashSet<>();

        imgLabel = new JLabel();
        label.add(imgLabel);
        imgLabel.setLocation(bar.getX() + bar.getWidth(), bar.getY());
        imgLabel.setSize(label.getWidth() - imgLabel.getX(), bar.getHeight());

        loadThread = new Thread(() -> {
            for(Map.Entry<String,BufferedImage> entry : images.entrySet()){
                String name = entry.getKey();
                BufferedImage img = entry.getValue();

                double scale;
                if(img.getWidth() > img.getHeight())
                    scale = imgLabel.getWidth() / 1.0 / img.getWidth();
                else
                    scale = imgLabel.getWidth() / 2.0 / img.getWidth();

                JLabel label = new JLabel();
                label.setSize(MATH.mult(img.getWidth(),scale), MATH.mult(img.getHeight(),scale));

                img = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, label.getWidth(), label.getHeight());
                label.setIcon(new ImageIcon(img));

                labels.add(label);
                imgLabel.add(label);

                JButton button = new JButton();
                button.setSize(MATH.mult(imgLabel.getWidth(), 0.05),MATH.mult(imgLabel.getWidth(), 0.05));
                button.setFocusable(false);
                label.add(button);
                buttons.add(button);
                button.setVisible(edit);
                button.addActionListener(e -> {
                    imgLabel.removeAll();
                    images.remove(name, entry.getValue());
                    reloadImgs();
                });
            }

            int lastLabelHeight = 0;
            int y = 5;
            int x = 5;

            for(JLabel label : labels){
                if(label.getWidth() <= label.getHeight()){
                    label.setLocation(x,y);

                    if(x == 5) {
                        x += label.getWidth() + 5;

                        lastLabelHeight = label.getHeight();
                    }
                    else {
                        if(label.getHeight() >= lastLabelHeight)
                            lastLabelHeight = label.getHeight() + 5;

                        y += lastLabelHeight + 5;

                        x = 5;
                    }


                }
            }

            for(JLabel label : labels){
                if(label.getWidth() > label.getHeight()){
                    if(x != 5) {
                        y += lastLabelHeight + 5;
                        x = 5;
                    }

                    label.setLocation(x,y);
                    y += label.getHeight() + 5;
                }
            }

            int scrollMax = y - imgLabel.getHeight();

            bar.setMaximum(Math.max(scrollMax, 0));
        });
        loadThread.start();
    }

    private boolean edit;
    public void edit(boolean edit){
        this.edit = edit;

        for(JButton button : buttons){
            button.setVisible(edit);
        }

        newImage.setVisible(edit);
    }
}
