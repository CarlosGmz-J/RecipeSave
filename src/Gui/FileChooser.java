package Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class FileChooser {
    private final JFileChooser chooser;

    private final JFrame frame = new JFrame();

    public FileChooser(){
        chooser = new JFileChooser();

        frame.add(chooser);
        frame.setSize(600,500);
        frame.setLayout(null);

        chooser.setBounds(50,0,500,500);
        chooser.setBackground(Color.lightGray);

        frame.setResizable(false);

        chooser.addActionListener(e -> {
            if(e.getActionCommand().equals("ApproveSelection")){
                /*
                wenn ein knopf vom FileChooser gedrückt wird und der knopf ok knopf ist
                dann fenster wieder unichtbar machen
                 */
                frame.setVisible(false);
            }
        });
    }

    public void setVisible() {
        frame.setVisible(!frame.isVisible());
        /*
        wenn das fenster sichtbar ist, dann wird es unsichtbar und andersherum
         */
    }

    public File getFile(){
        /*
        gibt vom fileChooser ausgewählte datei zurück
         */
        return chooser.getSelectedFile();
    }

    public void setCurrentDirectory(File file){
        /*
        ordner setzten, der angezeigt wird
         */
        chooser.setCurrentDirectory(file);
    }

    public void addActionListener(ActionListener actionListener){
        chooser.addActionListener(actionListener);
    }
}
