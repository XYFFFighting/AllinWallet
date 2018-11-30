package group26.cs307.allinwallet;

import android.app.Application;

public class GlobalClass extends Application{
    private String themeSelection;

    public String getThemeSelection() {
        return themeSelection;
    }

    public void setThemeSelection(String themeSelection) {
        this.themeSelection = themeSelection;
    }

}
