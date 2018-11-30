package group26.cs307.allinwallet;

import android.app.Application;

public class GlobalClass extends Application{
    private boolean themeSelection;

    public boolean getThemeSelection() {
        return themeSelection;
    }

    public void setThemeSelection(boolean themeSelection) {
        this.themeSelection = themeSelection;
    }

}
