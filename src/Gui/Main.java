package Gui;

import ErrorLog.ErrorLog;
import Recipe.Recipe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args){
        new Main();
    }

    private boolean running = true;

    private Main(){
        ErrorLog.addErrorLogger("recipeErrors");

        Recipe.loadRecipes();
        new Gui(this);

        new Thread(() -> {
            while(running) {
                try {
                    int counter = 300 * 1000;
                    while (counter > 0 && running){
                        counter--;
                        Thread.sleep(1);
                    }

                    System.out.println("save");
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-H");
                LocalDateTime now = LocalDateTime.now();

                String date = dtf.format(now);

                Recipe.saveRecipes(System.getenv("appdata") +"/recipesBackup/" + date);
            }

            System.exit(0);
        }).start();
    }

    public void exit(){
        running = false;
    }
}
