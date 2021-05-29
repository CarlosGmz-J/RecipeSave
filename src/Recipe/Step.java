package Recipe;

public class Step {
    private String step;

    public Step(String step){
        step = step.replaceAll(" -n- ","\n");
        this.step = step;
    }

    public Step(){
        step = "";
    }

    public void setStep(String step) {
        this.step = step;
    }
    public String getStep() {
        return step;
    }

    public String toString() {
        return step.replaceAll("\n"," -n- ");
    }
}
