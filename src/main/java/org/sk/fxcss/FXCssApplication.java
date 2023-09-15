package org.sk.fxcss;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FXCssApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FXCssApplication.class.getResource("mainWindow.fxml"));
        AppState state=AppState.getInstance();
        state.setMainStage(stage);
        stage.setMaximized(true);
        Scene scene = new Scene(fxmlLoader.load(), 1200, 900);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        FXCssController controller = fxmlLoader.getController();
        
        controller.setState(state);
        controller.setAccelerators(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}