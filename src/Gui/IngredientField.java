package Gui;

import EasySave.SaveMap;
import Recipe.Recipe;
import Recipe.IngredientGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IngredientField {
    private final LinkedList<IngredientGroup> groups;

    private final JScrollBar bar;

    private final JLabel ingredientBack = new JLabel();

    private AtomicInteger numberCons;
    private AtomicBoolean diameterCons;

    private int number;
    private boolean diameter;

    public IngredientField(JLabel back, int y, LinkedList<IngredientGroup> groups, AtomicBoolean diameterCons, AtomicInteger numberCons){
        this.diameterCons = diameterCons;
        this.numberCons = numberCons;

        number = numberCons.get();
        diameter = diameterCons.get();

        int WIDTH = MATH.mult(back.getWidth(), 0.2);
        int HEIGHT = back.getHeight() - y;
        this.groups = groups;
        JLabel label = new JLabel();
        back.add(label);
        label.setSize(WIDTH, HEIGHT);
        label.setLocation(0,y);
        label.setIcon(new ImageIcon(Gui.newBuffImgColor(new Color(255,188,219), WIDTH, HEIGHT)));

        bar = new JScrollBar();
        label.add(bar);
        bar.setMaximum(0);
        bar.setUnitIncrement(4);
        bar.setSize(MATH.mult(WIDTH,0.063),MATH.mult(HEIGHT,0.88));
        bar.addAdjustmentListener(e -> updateScroll());
        bar.setUnitIncrement(MATH.mult(label.getHeight(), 0.005));

        label.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                bar.setValue(bar.getValue() + e.getWheelRotation() * bar.getUnitIncrement());
            }
        });

        JButton newIngredientGroup = new JButton("neue Zutat");
        Gui.addLookToButton(newIngredientGroup, Gui.standardButtonColor);
        label.add(newIngredientGroup);
        newIngredientGroup.setBounds(bar.getWidth() + 5,bar.getHeight() + 10, WIDTH - bar.getWidth() - 10, MATH.mult(HEIGHT,0.05));
        components.add(newIngredientGroup);
        newIngredientGroup.addActionListener(e -> {
            groups.add(new IngredientGroup());
            updateNumberAndDiameter();
            update(true);
        });

        label.add(ingredientBack);
        ingredientBack.setLocation(bar.getWidth() + 5, 5 + MATH.mult(bar.getHeight(), 0.05));
        ingredientBack.setSize(WIDTH - ingredientBack.getX() - 5,MATH.mult(bar.getHeight(), 0.95) -5);

        JLabel numberLabel = new JLabel();
        numberLabel.setBounds(ingredientBack.getX(), 5, ingredientBack.getWidth(), MATH.mult(bar.getHeight(), 0.05) -5);
        label.add(numberLabel);
        numberLabel.setIcon(new ImageIcon(Gui.newBuffImgColor(Color.white,numberLabel.getWidth(), numberLabel.getHeight())));

        JTextField showNumber = new JTextField("" + number);

        JButton down = new JButton("←");
        down.setSize(numberLabel.getHeight(), numberLabel.getHeight());
        numberLabel.add(down);
        Gui.addLookToButton(down, Gui.standardButtonColor);
        down.addActionListener(e -> {
            number--;
            number = Math.max(1, number);

            showNumber.setText("" + number);

            updateNumberAndDiameter();
        });

        JButton up = new JButton("→");
        up.setBounds(numberLabel.getWidth() - numberLabel.getHeight() * 2, 0, numberLabel.getHeight(), numberLabel.getHeight());
        numberLabel.add(up);
        Gui.addLookToButton(up, Gui.standardButtonColor);
        up.addActionListener(e -> {
            number++;
            number = Math.min(100, number);

            showNumber.setText("" + number);

            updateNumberAndDiameter();
        });

        showNumber.setBounds(down.getWidth(), 0, numberLabel.getWidth() - down.getWidth() * 3,down.getHeight());
        numberLabel.add(showNumber);
        showNumber.setHorizontalAlignment(SwingConstants.CENTER);
        showNumber.setEditable(false);

        JButton diameterButton = new JButton();
        if(diameter)
            diameterButton.setText("D");
        else
            diameterButton.setText("A");
        Gui.addLookToButton(diameterButton, Color.lightGray);
        diameterButton.setBounds(numberLabel.getWidth() - numberLabel.getHeight(), 0, numberLabel.getHeight(), numberLabel.getHeight());
        numberLabel.add(diameterButton);
        diameterButton.addActionListener(e -> {
            diameter = !diameter;

            if(diameter)
                diameterButton.setText("D");
            else
                diameterButton.setText("A");

            updateNumberAndDiameter();
        });
        
        update(false);
        updateNumberAndDiameter();
    }

    private final HashSet<JComponent> components = new HashSet<>();
    public void update(boolean scrollDown){
        ingredientBack.removeAll();

        int y = 0;
        for(IngredientGroup group : groups){
            y = group.drawGroup(ingredientBack,y, this);
        }

        int scroll = y - ingredientBack.getHeight();
        scrollBefore = 0;

        if(scroll > 0){
            bar.setMaximum(scroll);
            bar.setValue(scrollBefore);
        }
        else {
            bar.setMaximum(0);
        }

        if(scrollDown){
            bar.setValue(bar.getMaximum());
        }

        updateScroll();

        ingredientBack.repaint();
    }
    private void updateNumberAndDiameter(){
        diameterCons.set(diameter);
        numberCons.set(number);

        for(IngredientGroup group : groups){
            group.updateNumber(number, diameter);
        }
    }

    private int scrollBefore = 0;
    private void updateScroll(){
        int scroll = bar.getValue();

        int totalScroll = scroll - scrollBefore;

        for(IngredientGroup group : groups){
            group.updateScroll(totalScroll);
        }

        scrollBefore = scroll;
    }

    public void removeGroup(IngredientGroup group){
        groups.remove(group);
        update(false);
    }

    public void edit(boolean edit){
        for(JComponent component : components){
            component.setVisible(edit);
        }

        for(IngredientGroup group : groups){
            group.edit(edit);
        }
    }
}
