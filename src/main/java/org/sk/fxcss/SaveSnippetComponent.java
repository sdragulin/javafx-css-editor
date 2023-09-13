package org.sk.fxcss;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.IOException;
import java.util.UUID;

public class SaveSnippetComponent extends AnchorPane {

    public TextField nameField;
    public TextField elementTypeField;
    public TextField descriptionField;
    public CodeArea contentField;
    public Button saveButton;
    public Button cancelButton;
    private String snippet;

    public SaveSnippetComponent(String snippet) throws IOException {
        this.snippet=snippet;
        FXMLLoader loader=new FXMLLoader(getClass().getResource("saveSnippetDialog.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
    }

    public void initialize(){
        saveButton.setDisable(true);
        contentField.appendText(snippet);
        saveButton.disableProperty().bind(
                Bindings.isEmpty(nameField.textProperty()).or(Bindings.isEmpty(elementTypeField.textProperty()))
        );

    }
    public void saveSnippet() throws IOException {
        Snippet snip=new Snippet(
                UUID.randomUUID().toString(),
                nameField.getText(),
                elementTypeField.getText(),
                descriptionField.getText(),
                contentField.getText()
        );
        SnippetManager.getInstance().saveSnippet(snip);
        stage.close();
    }
    public void cancel(){
        stage.close();
    }
    private Stage stage=new Stage();
    public void showSaveSnippetDialog(){

        Scene scene=new Scene(this,this.getPrefWidth(),this.getPrefHeight());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }
}
