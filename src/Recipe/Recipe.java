package Recipe;

import EasySave.SaveMap;
import ErrorLog.ErrorLog;
import Gui.Gui;
import Gui.MATH;
import Gui.StepField;
import Gui.IngredientField;
import Gui.ImgField;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Recipe {
    private static final LinkedHashMap<String,Recipe> recipes = new LinkedHashMap<>();

    public static final String path = System.getenv("APPDATA") + "/recipes";

    public static void addRecipe(Recipe recipe){
        recipes.put(recipe.getNewName(),recipe);
    }
    public static LinkedHashMap<String, Recipe> getRecipes() {
        return recipes;
    }

    public static void loadRecipes(){
        try {
            File directory = exist(path, true);

            boolean first = true;
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                String name = file.getName();
                if (first) {
                    Recipe.first = name;
                    first = false;
                }
                String path = Recipe.path + "/" + name;
                Recipe recipe = new Recipe(name, true);

                File imgDirectory = exist(path + "/imgs", true);

                for (File imgFile : Objects.requireNonNull(imgDirectory.listFiles())) {
                    try {
                        BufferedImage img = ImageIO.read(imgFile);
                        String imgName = imgFile.getName();

                        if (!imgName.endsWith(".png") && !imgName.endsWith(".jpg") && !imgName.endsWith(".jpeg"))
                            continue;

                        imgName = imgName.replaceAll(".png", "");
                        imgName = imgName.replaceAll(".jpg", "");
                        imgName = imgName.replaceAll(".jpeg", "");

                        recipe.addImg(imgName, img);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Recipe.recipes.put(name, recipe);
            }
        }
        catch(Exception e){
            ErrorLog.addError(e);
        }
    }

    public static boolean deleteFolder(File folder) {
        boolean success = true;

        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if(!f.delete())
                        success = false;
                }
            }
        }
        if(!folder.delete())
            success = false;

        return success;
    }

    public static void saveRecipes(String savePath){
        try {
            for (Map.Entry<String, Recipe> entry : recipes.entrySet()) {
                Recipe recipe = entry.getValue();

                if(!recipe.wasLoaded())
                    continue;

                String path = savePath + "/" + recipe.name;

                exist(path, true);

                recipe.save(path);

                File imgDirectory = exist(path + "/imgs", true);
                for (Map.Entry<String, BufferedImage> imgEntry : recipe.images.entrySet()) {
                    try {
                        File imgFile = exist(imgDirectory.getPath() + "/" + imgEntry.getKey() + ".png", false);
                        ImageIO.write(imgEntry.getValue(), "png", imgFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e){
            ErrorLog.addError(e);
        }
    }

    public static File exist(String path, boolean folder){
        boolean exist;

        File file = new File(path);
        exist = file.exists();

        if(!exist){
            boolean created = false;

            while (!created) {
                if (folder)
                    created = file.mkdirs();
                else {
                    try {
                        created = file.createNewFile();
                    } catch (Exception ignored) {
                    }
                }

                if(!created)
                    System.err.println("Failed creating " + file.toPath());
            }
        }

        return file;

        /*
        test if file or folder exists
        if not -> create new file or folder
         */
    }

    private static String shownNow = null;

    private static String first;
    private static Gui gui;
    public static void setup(JLabel label, Gui gui){
        Recipe.gui = gui;
        label.add(recipeLabel);
        recipeLabel.setLocation(MATH.mult(label.getWidth(),0.2),0);
        recipeLabel.setSize(label.getWidth() - recipeLabel.getX(),label.getHeight());

        int WIDTH = recipeLabel.getWidth();
        int HEIGHT = recipeLabel.getHeight();

        recipeLabel.setIcon(new ImageIcon(Gui.newBuffImgColor(Color.white,WIDTH,HEIGHT)));

        showRecipe(first);
    }

    private static final JLabel recipeLabel = new JLabel();

    public static void showRecipe(String recipe){
        if(shownNow != null)
            if(recipes.get(shownNow) != null)
                recipes.get(shownNow).setVisible(false);

        shownNow = recipe;
        if(shownNow != null)
            recipes.get(shownNow).setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private final LinkedList<IngredientGroup> groups = new LinkedList<>();
    private final LinkedList<Step> steps = new LinkedList<>();
    private final HashMap<String, BufferedImage> images = new HashMap<>();
    private final AtomicBoolean diameter = new AtomicBoolean(false);
    private final AtomicInteger number = new AtomicInteger(1);

    public static String DIAMETER = "diameter", NUMBER = "number", INGREDIENTS = "ingredients";

    private final String name;

    private String newName;

    private final JLabel label = new JLabel();
    private boolean guiSetupDone = false;

    private boolean loaded = false;

    private final SaveMap ingredientMap = new SaveMap(), stepMap = new SaveMap();

    public Recipe(String name, boolean oldRecipe) {
        this.name = name;
        newName = name;

        ingredientMap.setPath(Recipe.path + "/" + name + "/ingredients.txt");
        stepMap.setPath(Recipe.path + "/" + name + "/steps.txt");

        if(!oldRecipe) {
            loaded = true;
        }
        else{
            ingredientMap.loadFromFile();

            try {
                number.set(Integer.parseInt(ingredientMap.get(new String[]{},NUMBER).toString()));
            }
            catch (Exception e){
                number.set(1);
                ErrorLog.addError(e);
            }

            try {
                diameter.set(Boolean.parseBoolean(ingredientMap.get(new String[]{},DIAMETER).toString()));
            }
            catch (Exception e){
                diameter.set(true);
                ErrorLog.addError(e);
            }

            Iterator<Map.Entry<String, Object>> itGroups = ingredientMap.getIterator(new String[]{INGREDIENTS});

            while(itGroups.hasNext()){
                Map.Entry<String, Object> entry = itGroups.next();

                groups.add(new IngredientGroup(entry.getKey(),(LinkedHashMap<String, Object>) entry.getValue()));
            }

            stepMap.loadFromFile();

            Iterator<Map.Entry<String, Object>> stepIt = stepMap.getIterator(new String[]{});

            while (stepIt.hasNext()){
                steps.add(new Step((String)stepIt.next().getValue()));
            }
        }
    }

    private void save(String path){
        try {
            System.out.println("save " + name);

            ingredientMap.clear();
            ingredientMap.add(new String[]{}, DIAMETER, diameter.toString());
            ingredientMap.add(new String[]{}, NUMBER, number.toString());

            LinkedHashMap<String, Object> map = new LinkedHashMap<>();

            for (IngredientGroup group : groups) {
                group.save(map);
            }

            ingredientMap.add(new String[]{}, INGREDIENTS, map);

            ingredientMap.setPath(path + "/ingredients.txt");
            ingredientMap.save();

            stepMap.clear();

            int number = 0;
            for (Step step : steps) {
                stepMap.add(new String[]{}, String.valueOf(number), step.toString());
                number++;
            }

            stepMap.setPath(path + "/steps.txt");
            stepMap.save();
        }
        catch (Exception e){
            ErrorLog.addError(e);
        }
    }

    public void addImg(String name, BufferedImage img){
        images.put(name, img);
    }

    public String getName() {
        return name;
    }
    public String getNewName() {
        return newName;
    }

    private JButton edit;

    private Thread lastDeleteThread = null;

    private final JTextField nameField = new JTextField();
    private StepField stepField;
    private IngredientField ingredientField;
    private ImgField imgField;
    private JFrame deleteFrame;

    private void setVisible(boolean visible){
        if(!guiSetupDone){
            if(visible) {
                loaded = true;
            }

            int WIDTH = recipeLabel.getWidth();
            int HEIGHT = recipeLabel.getHeight();

            guiSetupDone = true;

            label.setSize(WIDTH,HEIGHT);
            label.setVisible(false);
            recipeLabel.add(label);

            nameField.setBounds(MATH.mult(WIDTH,0.05),1,MATH.mult(WIDTH,0.3),MATH.mult(HEIGHT,0.04));
            nameField.setFont(new Font("Elephant",Font.PLAIN,25));
            label.add(nameField);
            nameField.setText(name);
            nameField.setBackground(Color.white);
            nameField.setBorder(null);
            nameField.getAccessibleContext().addPropertyChangeListener(e ->{
                if(nameField.getText() == null)
                    return;

                if(nameField.getText().isEmpty() || name.equals(nameField.getText()))
                    return;

                if(recipes.containsKey(nameField.getText()))
                    return;

                gui.updateSelection(name, newName = nameField.getText());
            });

            edit = new JButton("EDIT Off");
            edit.setBounds(nameField.getWidth() + nameField.getX(),1,MATH.mult(WIDTH,0.1),MATH.mult(HEIGHT,0.04));
            label.add(edit);
            edit.setFocusable(false);
            edit.addActionListener(e -> edit(edit.getText().equals("EDIT Off")));

            deleteFrame = new JFrame();
            deleteFrame.setUndecorated(true);
            deleteFrame.setLayout(null);
            deleteFrame.setResizable(false);

            JLabel question = new JLabel("Wirklich Löschen?");
            question.setSize(200, 25);
            deleteFrame.add(question);

            JButton TRUE = new JButton("ja");
            TRUE.setBounds(0, 25,100, 30);
            deleteFrame.add(TRUE);
            TRUE.setFocusable(false);
            TRUE.addActionListener(e -> {
                deleteFrame.setVisible(false);

                Iterator<Map.Entry<String, Recipe>> iterator = recipes.entrySet().iterator();
                String showNow = iterator.next().getKey();

                if(!showNow.equals(name)){
                    showRecipe(showNow);
                }
                else if(recipes.size() > 1){
                    showRecipe(iterator.next().getKey());
                }

                this.setVisible(false);
                Recipe.recipes.remove(name, this);
                deleteFolder(new File(path + "/" + name));

                gui.updateSelection();
            });

            JButton FALSE = new JButton("nein");
            FALSE.setBounds(100, 25, 100, 30);
            deleteFrame.add(FALSE);
            FALSE.setFocusable(false);
            FALSE.addActionListener(e -> {
                if(lastDeleteThread != null)
                    lastDeleteThread.stop();
                lastDeleteThread = null;
                deleteFrame.setVisible(false);
            });

            JButton deleteRecipe = new JButton("Rezept löschen");
            deleteRecipe.setBounds(edit.getX() + edit.getWidth(), edit.getY(), edit.getWidth(), edit.getHeight());
            label.add(deleteRecipe);
            deleteRecipe.setBorderPainted(false);
            deleteRecipe.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) { }
                public void mousePressed(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) {}

                public void mouseEntered(MouseEvent e) {
                    deleteRecipe.setBackground(Color.red);
                    label.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    deleteRecipe.setBackground(Color.lightGray);
                    label.repaint();
                }
            });
            deleteRecipe.setFocusable(false);
            deleteRecipe.setBackground(Color.lightGray);
            deleteRecipe.addActionListener(e -> {
                if(deleteFrame.getX() == 0)
                    deleteFrame.setBounds(edit.getLocationOnScreen().x + edit.getWidth() * 2, edit.getLocationOnScreen().y, 200, 55);

                if (deleteFrame.isVisible()) {
                    deleteFrame.setVisible(false);
                    if (lastDeleteThread != null) {
                        lastDeleteThread.stop();
                        lastDeleteThread = null;
                    }
                }
                else {
                    deleteFrame.setVisible(true);

                    lastDeleteThread = new Thread(() -> {
                        try {
                            Thread.sleep(10000);

                            deleteFrame.setVisible(false);
                        }
                        catch (Exception c){
                            c.printStackTrace();
                        }
                    });
                    lastDeleteThread.start();
                }
            });

            stepField = new StepField(label,nameField.getHeight() + 10,steps);
            ingredientField = new IngredientField(label, nameField.getHeight() + 10, groups, diameter, number);
            imgField = new ImgField(label, images, nameField.getHeight() + 10);
        }

        edit(false);

        label.setVisible(visible);
    }

    private boolean wasLoaded(){
        return loaded;
    }

    private void edit(boolean edit){
        if(edit){
            this.edit.setText("EDIT On");
        }
        else{
            this.edit.setText("EDIT Off");
        }

        if(!edit) {
            deleteFrame.setVisible(false);
            if(lastDeleteThread != null)
                lastDeleteThread.stop();
        }

        nameField.setEditable(edit);
        nameField.setFocusable(edit);

        if(stepField != null)
            stepField.edit(edit);
        if(ingredientField != null)
            ingredientField.edit(edit);
        if(imgField != null)
            imgField.edit(edit);

        label.repaint();
    }
}
