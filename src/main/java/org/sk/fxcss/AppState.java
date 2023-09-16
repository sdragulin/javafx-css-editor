package org.sk.fxcss;

import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppState {
    private Stage mainStage;
    private static final AppState _instance=new AppState();
    public static final String TEMPLATE_FOLDER="templates";
    public static final String TEMP_FILE_PATH ="tmp";
    private final ExecutorService service;

    private AppState(){
        service= Executors.newFixedThreadPool(3);
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
    public void execute(Task task){
        service.execute(task);
    }

    public void shutdown() {
        service.shutdown();
    }
}
