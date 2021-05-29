package Recipe;

import Gui.MATH;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Ingredient {
    private static final String[] numberChars = new String[]{"0","1","2","3","4","5","6","7","8","9","."};
    private static final String NUMBERFORONE = "numberForOne", UNIT = "unit";

    private String ingredient;

    private String unit, fullString;
    private int number;
    private boolean diameter;
    private BigDecimal numberFor1 = new BigDecimal("0");

    public Ingredient(int number, boolean diameter){
        this.number = number;
        this.diameter = diameter;
        ingredient = "";
        unit = "";
    }

    public Ingredient (String ingredient, LinkedHashMap<String, String> map){
        this.ingredient = ingredient;
        this.numberFor1 = new BigDecimal(map.get(NUMBERFORONE));
        this.unit = map.get(UNIT);
    }

    public void save(LinkedHashMap<String, Object> map){
        LinkedHashMap<String, Object> thisMap = new LinkedHashMap<>();
        thisMap.put(UNIT, unit);
        thisMap.put(NUMBERFORONE, numberFor1);

        map.put(ingredient, thisMap);
    }

    private void getFullString(){
        BigDecimal numberNow;

        if(diameter){
            int pow = number * number;

            numberNow = numberFor1.multiply(new BigDecimal(pow));
        }
        else{
            numberNow = numberFor1.multiply(new BigDecimal(number));
        }

        int round = 1;

        if(unit.contains("ml") || unit.contains("g")){
            round = 0;
        }

        numberNow = numberNow.setScale(round, RoundingMode.HALF_UP);



        fullString = numberNow.toPlainString();

        if(fullString.contains(".")) {
            while (true) {
                if (fullString.endsWith("0") || fullString.endsWith(".")) {
                    boolean BREAK = fullString.endsWith(".");

                    fullString = fullString.substring(0, fullString.length() - 1);

                    if (BREAK)
                        break;
                } else
                    break;
            }
        }

        if(fullString.equals("0")) {
            fullString = "" + unit;
            return;
        }

        fullString += unit;
    }

    private String unitSplit(String unit){
        unit = unit.replaceAll(",",".");

        String fullNumber = "";
        this.unit = unit;

        for(int i = 0; i < unit.length(); i++){
            String now = String.valueOf(unit.charAt(i));

            boolean isNumber = false;
            for(String numberChar : numberChars){
                if(numberChar.equals(now)){
                    fullNumber += now;
                    isNumber = true;
                    break;
                }
            }

            if(!isNumber) {
                break;
            }

            this.unit = this.unit.substring(1);
        }

        if(fullNumber.isEmpty())
            fullNumber = "0";

        return fullNumber;
    }

    private JButton deleteIngredient;
    private JTextField unitField;
    public int drawIngredient(int y, JLabel back, IngredientGroup group, HashSet<JTextField> textFields, HashSet<JButton> buttons){
        JTextField ingredientField = new JTextField();
        ingredientField.setBounds(MATH.mult(back.getWidth(),0.1),y,MATH.mult(back.getWidth(),0.65),MATH.mult(back.getHeight(),0.03));
        back.add(ingredientField);
        textFields.add(ingredientField);
        ingredientField.setText(ingredient);
        ingredientField.getAccessibleContext().addPropertyChangeListener(e -> {
            if(ingredientField.getText().isEmpty())
                return;

            ingredient = ingredientField.getText();
        });

        unitField = new JTextField();
        unitField.setBounds(ingredientField.getWidth() + ingredientField.getX(), ingredientField.getY(), MATH.mult(back.getWidth(), 0.25), ingredientField.getHeight());
        back.add(unitField);
        textFields.add(unitField);
        getFullString();
        unitField.setText(fullString);
        unitField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) { }
        });

        deleteIngredient = new JButton("-");
        buttons.add(deleteIngredient);
        Gui.Gui.addLookToButton(deleteIngredient, Color.white);
        deleteIngredient.setBounds(0, ingredientField.getY(), MATH.mult(back.getWidth(), 0.1),ingredientField.getHeight());
        back.add(deleteIngredient);
        deleteIngredient.setFocusable(false);
        deleteIngredient.addActionListener(e -> {
            group.removeIngredient(this);
        });

        y += ingredientField.getHeight() + 5;

        return y;
    }

    private void update(){
        if(changing)
            return;

        if(unitField.getText().isEmpty())
            return;

        if(number == 0)
            return;

        BigDecimal now = new BigDecimal(unitSplit(unitField.getText()));

        if(diameter){
            int divide = number * number;

            numberFor1 = now.divide(new BigDecimal(divide), 50, RoundingMode.HALF_UP);
        }
        else{
            numberFor1 = now.divide(new BigDecimal(this.number),50, RoundingMode.HALF_UP);
        }

        System.out.println();
    }

    private boolean changing = false;
    public void updateNumber(int number, boolean diameter){
        changing = true;

        this.number = number;
        this.diameter = diameter;

        getFullString();

        unitField.setText(fullString);

        changing = false;
    }
}
