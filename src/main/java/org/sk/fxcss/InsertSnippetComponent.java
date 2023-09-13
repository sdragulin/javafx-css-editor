package org.sk.fxcss;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class InsertSnippetComponent extends AnchorPane {
    public TextField searchField;
    public ListView<Snippet> snippetListView;

    public Button closeButton;
    public Button insertButton;

    public InsertSnippetComponent(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("snippetDialog.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void initialize(){
        snippetListView.setCellFactory(new Callback<ListView<Snippet>, ListCell<Snippet>>() {
            @Override
            public ListCell<Snippet> call(ListView<Snippet> snippetListView) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(Snippet snippet, boolean b) {
                        super.updateItem(snippet, b);
                        if(!b){
                            setText(null);
                            setGraphic(new SnippetListCell(snippet));
                        }else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        });
        try {
            List<Snippet> snippet = SnippetManager.getInstance().findSnippet("", "");
            snippetListView.getItems().addAll(snippet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        insertButton.disableProperty().bind(Bindings.not(snippetListView.getSelectionModel().selectedItemProperty().isNotNull()));
    }
    private final ObjectProperty<Snippet> selectedObject=new SimpleObjectProperty<>();
    public Snippet showSnippetDialog(){
        Stage stage=new Stage();
        Scene scene=new Scene(this,this.getPrefWidth(),this.getPrefHeight());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        insertButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                selectedObject.setValue(snippetListView.getSelectionModel().getSelectedItem());
                stage.close();
            }
        });
        closeButton.setOnAction((e)-> stage.close());
        stage.showAndWait();
        return selectedObject.get();
    }
    private static class SnippetListCell extends VBox {

        private TextFlow name;
        private TextFlow description;
        private TextFlow   content;
        public SnippetListCell() {
            super();
            init();
        }
        public SnippetListCell(Snippet snip){
            super();
            init();
            setName(snip.name());
            setDescription(snip.description());
            setContent(snip.content());
        }
        public void init(){

            name=new TextFlow();
            description=new TextFlow();
            content=new TextFlow();

            this.setSpacing(10);
            VBox.setVgrow(content, Priority.ALWAYS);
            HBox headerBox=new HBox();
            Separator sep=new Separator();

            headerBox.getChildren().addAll(name,sep, description);
            headerBox.setSpacing(2);
            headerBox.setAlignment(Pos.CENTER);
            this.getChildren().addAll(headerBox,content);
        }

        public void setName(String name){
            Text nameT=new Text(name);
            this.name.getChildren().add(nameT);
        }
        public void setDescription(String s){
            Text c=new Text(s);
            this.description.getChildren().add(c);
        }
        public void setContent(String s){
            Text c=new Text(s);
            this.content.getChildren().add(c);
        }
    }
}
