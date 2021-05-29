package Gui;

import Recipe.Step;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class StepField {
    private final LinkedList<Step> steps;

    private final JLabel textAreaLabel = new JLabel();

    private final JScrollBar bar = new JScrollBar();
    private final static int SCROLLBARMULTI = 24;

    private final int HEIGHT;

    public StepField(JLabel back, int y, LinkedList<Step> steps){
        this.steps = steps;
        int WIDTH = MATH.mult(back.getWidth(), 0.4);
        HEIGHT = back.getHeight() - y;

        JLabel label = new JLabel();
        back.add(label);
        label.setSize(WIDTH,HEIGHT);
        label.setLocation(MATH.mult(back.getWidth(),0.2),y);
        label.setIcon(new ImageIcon(Gui.newBuffImgColor(new Color(255,188,219), WIDTH,HEIGHT)));

        label.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                bar.setValue(bar.getValue() + e.getWheelRotation() * SCROLLBARMULTI);
            }
        });

        textAreaLabel.setLocation(MATH.mult(label.getWidth(),0.03),5);
        textAreaLabel.setSize(label.getWidth() - textAreaLabel.getX(),MATH.mult(label.getHeight(),0.88) - 5);
        label.add(textAreaLabel);
        update();

        label.add(bar);
        bar.setUnitIncrement(SCROLLBARMULTI);
        bar.setBounds(0,0,textAreaLabel.getX(),textAreaLabel.getHeight() + 5);
        bar.addAdjustmentListener(e -> scroll(bar.getValue()));

        JButton newStep = new JButton("neuer Schritt");
        newStep.setFocusable(false);
        newStep.setBounds(textAreaLabel.getX() + 5, textAreaLabel.getHeight() + 15,textAreaLabel.getWidth() - 10,MATH.mult(label.getHeight(),0.05));
        label.add(newStep);
        newStep.addActionListener(e -> {
            steps.add(new Step());
            update();
        });
        components.add(newStep);

        scroll(0);
    }

    private final LinkedList<JTextArea> textAreas = new LinkedList<>();
    private final LinkedList<JButton> deleteButtons = new LinkedList<>();
    private final HashSet<JComponent> components = new HashSet<>();
    private void update(){
        textAreaLabel.removeAll();
        textAreas.clear();
        deleteButtons.clear();

        for(Step step : steps){
            JTextArea textArea = new JTextArea();
            System.out.println(textArea.getFont().toString());
            textAreas.add(textArea);
            textAreaLabel.add(textArea);
            textArea.setEditable(edit);
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateSizes();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateSizes();
                }

                public void changedUpdate(DocumentEvent e) {}
            });
            textArea.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    textArea.repaint();
                }

                public void focusLost(FocusEvent e) {
                    textArea.setBackground(Color.white);
                }
            });
            textArea.setText(step.getStep());
            textArea.setSize(textAreaLabel.getWidth() - 10, MATH.mult(HEIGHT,0.2) - 5);
            textArea.setBackground(Color.white);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);

            textArea.getAccessibleContext().addPropertyChangeListener(e -> step.setStep(textArea.getText()));

            JButton delete = new JButton();
            delete.setSize(MATH.mult(textArea.getWidth(),0.05),MATH.mult(textArea.getWidth(),0.05));
            delete.setLocation(textArea.getWidth() - delete.getWidth(),textArea.getHeight() - textArea.getHeight());
            delete.addActionListener(e -> {
                steps.remove(step);
                update();
            });
            textArea.add(delete);
            delete.setVisible(edit);
            delete.setFocusable(false);
            deleteButtons.add(delete);
        }

        bar.setMaximum(0);

        updateSizes();

        textAreaLabel.repaint();
    }

    private void updateSizes(){
        int minHeight = MATH.mult(HEIGHT,0.2) - 5;

        for(JTextArea area : textAreas){
            String text = area.getText();

            Font font = area.getFont();
            FontMetrics fm = area.getFontMetrics(font);

            int height = fm.getHeight() * getLines(area);

            System.out.println(getLines(area));

            height = Math.max(minHeight, height);

            area.setSize(area.getWidth(), height + fm.getDescent());
        }

        scroll(bar.getValue());
    }

    private void scroll(int value){
        int y = 0;

        for(JTextArea textArea: textAreas){
            textArea.setLocation(5,y - value);
            y+= textArea.getHeight() + 5;
        }

        bar.setMaximum(Math.max(y - textAreaLabel.getHeight(), 0));
    }

    private boolean edit;
    public void edit(boolean edit){
        this.edit = edit;

        for(JComponent component : components){
            component.setVisible(edit);
        }

        for(JButton button : deleteButtons){
            button.setVisible(edit);
        }
        for(JTextArea textArea : textAreas){
            textArea.setEditable(edit);
        }
    }

    private int getLines(JTextArea area){
        int lines = 0;

        String[] texts = area.getText().replaceAll("\n","\n ").split("\n");

        for(String text : texts) {
            lines++;
            final int width = area.getWidth();
            final FontMetrics fontMetrics = area.getGraphics().getFontMetrics(area.getFont());

            StringBuilder sb = new StringBuilder();
            for (final Character c : text.toCharArray()) {
                sb.append(c);
                if (fontMetrics.stringWidth(sb.toString()) > width) {
                    sb.setLength(sb.length() - 1);
                    lines++;
                    sb = new StringBuilder(c.toString());
                }
            }
        }

        return lines;
    }
}
