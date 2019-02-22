package xyz.ummo.user;

public class Progress {

    String processName;
    boolean selected;

    public Progress(String processName){

        this.processName = processName;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getProcessName() {
        return processName;
    }

    public boolean isSelected() {
        return selected;
    }
}
