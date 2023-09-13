package org.sk.fxcss;

import javafx.stage.Stage;

public class AppState {
    private Stage mainStage;
    private static final AppState _instance=new AppState();
    public static final String TEMPLATE_FOLDER="templates";
    public static final String TEMP_FILE_PATH ="tmp.css";

    private AppState(){

    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
    public static AppState getInstance(){
        return _instance;
    }

}
