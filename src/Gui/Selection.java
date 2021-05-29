package Gui;

import Recipe.Recipe;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Selection {
    private final int HEIGHT;

    private final JLabel recipesLabel = new JLabel();
    private final JScrollBar scrollBar;

    public Selection(JLabel label){
        int WIDTH = MATH.mult(label.getWidth(), 0.2);
        HEIGHT = label.getHeight();

        JLabel back = new JLabel();
        label.add(back);
        back.setSize(WIDTH,HEIGHT);
        back.setIcon(new ImageIcon(Gui.newBuffImgColor(new Color(255,188,219), WIDTH,HEIGHT)));

        JTextField newRecipeName = new JTextField();
        newRecipeName.setBounds(0,0,MATH.mult(WIDTH,0.85),MATH.mult(HEIGHT,0.05));
        newRecipeName.setFont(new Font("Elephant",Font.PLAIN,25));
        newRecipeName.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.add(newRecipeName);

        JButton newRecipe = new JButton();
        newRecipe.setBounds(newRecipeName.getWidth(), 0, WIDTH - newRecipeName.getWidth(), newRecipeName.getHeight());
        newRecipe.setFocusable(false);
        back.add(newRecipe);
        newRecipe.addActionListener(e -> {
            if(!newRecipeName.getText().isEmpty()) {
                Recipe NEW = new Recipe(newRecipeName.getText(), false);
                Recipe.addRecipe(NEW);
                newRecipeName.setText("");
                update();
            }
        });

        recipesLabel.setBounds(MATH.mult(WIDTH,0.05),newRecipeName.getHeight(), WIDTH,HEIGHT - newRecipeName.getHeight());
        recipesLabel.setIcon(new ImageIcon(Gui.newBuffImgColor(new Color(255,188,219), recipesLabel.getWidth(), recipesLabel.getHeight())));
        back.add(recipesLabel);

        scrollBar = new JScrollBar();
        scrollBar.setBounds(0,newRecipeName.getHeight(),recipesLabel.getX(), recipesLabel.getHeight() - 40);
        back.add(scrollBar);
        scrollBar.setUnitIncrement(8);
        scrollBar.setMaximum(0);
        scrollBar.addAdjustmentListener(e -> updatePosition(scrollBar.getValue()));

        update();
    }

    private final LinkedList<JLabel> recipeLabels = new LinkedList<>();
    private final HashMap<String, JButton> buttons = new HashMap<>();
    private final int scrollScale = 64;
    public void update(){
        recipesLabel.removeAll();

        buttons.clear();
        recipeLabels.clear();

        int recipeNumber = 0;
        for(Map.Entry<String, Recipe> entry : Recipe.getRecipes().entrySet()) {
            Recipe recipe = entry.getValue();

            JLabel label = new JLabel();
            label.setSize(recipesLabel.getWidth() - recipesLabel.getX(), MATH.mult(HEIGHT, 0.05));
            recipeLabels.add(label);
            recipesLabel.add(label);

            label.setLocation(0,label.getHeight() * recipeNumber);
            if(recipeNumber % 2.0 == 0)
                label.setIcon(new ImageIcon(Gui.newBuffImgColor(Color.white,label.getWidth(),label.getHeight())));
            else
                label.setIcon(new ImageIcon(Gui.newBuffImgColor(new Color(255,188,219),label.getWidth(),label.getHeight())));

            JButton button = new JButton();
            button.setBounds(5,5,label.getWidth() - 10, label.getHeight() - 10);
            label.add(button);
            button.setFocusable(false);
            button.setText(recipe.getNewName());
            button.setFont(new Font("Elephant",Font.PLAIN,15));

            buttons.put(recipe.getName(),button);

            button.addActionListener(e -> Recipe.showRecipe(recipe.getName()));

            recipeNumber++;
        }

        int barValue = recipeNumber - 18;
        if(barValue > 0) {
            scrollBar.setMaximum(barValue * scrollScale);
        }

        recipesLabel.repaint();
    }

    public void updateSingle(String name, String newName){
        JButton button = buttons.get(name);
        button.setText(newName);

        recipesLabel.repaint();
    }

    private void updatePosition(int number){
        int counter = 0;
        for(JLabel label : recipeLabels){
            label.setLocation(0,(int)Math.round(label.getHeight() * (counter - number *1.0 / scrollScale)));
            counter++;
        }

        recipesLabel.repaint();
    }
}
