package Gui;

import Recipe.Recipe;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Gui {
    private final Selection selection;

    public Gui(Main main){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception ignored) {}

        JFrame frame = new JFrame("Blancas Back Bar ❤");
        frame.setIconImage(newBuffImg("icon"));

        frame.setLayout(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) { }

            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                Recipe.saveRecipes(Recipe.path);
                main.exit();
            }

            public void windowClosed(WindowEvent e) { }
            public void windowIconified(WindowEvent e) { }
            public void windowDeiconified(WindowEvent e) { }
            public void windowActivated(WindowEvent e) { }
            public void windowDeactivated(WindowEvent e) { }
        });

        JLabel label = new JLabel();
        label.setSize(frame.getWidth(), frame.getHeight());
        frame.add(label);

        selection = new Selection(label);

        Recipe.setup(label, this);

        frame.repaint();
    }

    public void updateSelection(){
        selection.update();
    }

    public static Image newBuffImgColor(Color color, int width, int height){
        BufferedImage returnImg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = returnImg.getGraphics();

        g.setColor(color);
        g.fillRect(0,0,width,height);

        g.dispose();

        return returnImg;
    }

    public void updateSelection(String name, String newName){
        selection.updateSingle(name, newName);
    }

    public static BufferedImage newBuffImg(String name){
        BufferedImage returnImg = null;

        try {
            returnImg = ImageIO.read(Gui.class.getResource("/res/images/" + name + ".png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return returnImg;
    }

    private static final HashMap<JButton, Color> colorsNow = new HashMap<>();

    public static Color standardButtonColor = new Color(225,225,225);
    public static void addLookToButton(JButton button, Color standard){
        button.setBackground(standard);
        button.setFocusable(false);
        button.setBorder(null);

        button.addActionListener(e -> {
            new Thread(() -> {
               button.setBackground(Color.lightGray);

               try{
                   Thread.sleep(100);
               }
               catch(Exception i){
                   i.printStackTrace();
               }

               button.setBackground(colorsNow.get(button));
            }).start();
        });

        button.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {
                Color hoverColor = new Color(229,241,251);
                colorsNow.put(button, hoverColor);
                button.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(standard);
                colorsNow.put(button, standard);
            }
        });
    }
}
