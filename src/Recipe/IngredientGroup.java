package Recipe;

import EasySave.SaveMap;
import Gui.IngredientField;
import Gui.MATH;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class IngredientGroup {
    private final LinkedList<Ingredient> ingredients = new LinkedList<>();
    private String name;
    private int number;
    private boolean diameter;

    public IngredientGroup(String name, LinkedHashMap<String, Object> map){
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();

        this.name = name;

        while(it.hasNext()){
            Map.Entry<String, Object> entry = it.next();
            ingredients.add(new Ingredient(entry.getKey(), (LinkedHashMap)entry.getValue()));
        }
    }

    public void save(LinkedHashMap<String, Object> map){
        LinkedHashMap<String, Object> group = new LinkedHashMap<>();

        map.put(name, group);

        for(Ingredient ingredient : ingredients){
            ingredient.save(group);
        }
    }

    public IngredientGroup(){
        name = " ";
    }

    private final HashSet<JTextField> textFields = new HashSet();
    private final HashSet<JButton> buttons = new HashSet();

    private final HashSet<JComponent> components = new HashSet();

    private IngredientField IField;
    public int drawGroup(JLabel back, int y, IngredientField IField){
        this.IField = IField;

        textFields.clear();
        components.clear();

        JTextField field = new JTextField();
        textFields.add(field);
        field.setFont(new Font("Elephant",Font.PLAIN,15));
        field.getAccessibleContext().addPropertyChangeListener(e -> {
            if(field.getText().isEmpty())
                return;

            name = field.getText();
        });
        field.setText(name);
        back.add(field);
        field.setBounds(0,y,back.getWidth(),MATH.mult(back.getHeight(),0.05));
        y += field.getHeight() + 5;

        JButton newIngredient = new JButton("+");
        newIngredient.setBorder(null);
        Gui.Gui.addLookToButton(newIngredient, Color.white);
        newIngredient.setBounds(MATH.mult(field.getWidth(),0.8),0,MATH.mult(field.getWidth(),0.1),MATH.mult(field.getWidth(),0.1));
        field.add(newIngredient);
        newIngredient.setFocusable(false);
        components.add(newIngredient);
        newIngredient.addActionListener(e -> {
            this.ingredients.add(new Ingredient(number, diameter));
            IField.update(true);
        });

        JButton deleteGroup = new JButton("-");
        deleteGroup.setBorder(null);
        Gui.Gui.addLookToButton(deleteGroup, Color.white);
        deleteGroup.setBounds(MATH.mult(field.getWidth(),0.9),0,MATH.mult(field.getWidth(),0.1),MATH.mult(field.getWidth(),0.1));
        field.add(deleteGroup);
        deleteGroup.setFocusable(false);
        components.add(deleteGroup);
        deleteGroup.addActionListener(e -> IField.removeGroup(this));

        for(Ingredient ingredient : this.ingredients){
            y = ingredient.drawIngredient(y, back, this, textFields,buttons);
        }

        return y;
    }

    public void removeIngredient(Ingredient ingredient){
        ingredients.remove(ingredient);
        IField.update(false);
    }

    public void updateScroll(int scroll){
        for(JTextField textField : textFields){
            textField.setLocation(textField.getX(), textField.getY() - scroll);
        }

        for(JButton button : buttons){
            button.setLocation(button.getX(), button.getY() - scroll);
        }
    }

    public void edit(boolean edit){
        for(JTextField field : textFields){
            field.setEditable(edit);
        }

        for(JButton button : buttons){
            button.setVisible(edit);
        }

        for(JComponent component : components){
            component.setVisible(edit);
        }
    }

    public void updateNumber(int number, boolean diameter){
        this.number = number;
        this.diameter = diameter;

        for(Ingredient i : ingredients){
            i.updateNumber(number, diameter);
        }
    }
}
