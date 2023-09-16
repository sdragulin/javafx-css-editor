package org.sk.fxcss;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;

import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;

public class InsertSnippetComponent extends AnchorPane {
    public TextField searchField;
    public ListView<Snippet> snippetListView;
    private FilteredList<Snippet> filteredData;
    public Button closeButton;
    public Button insertButton;

    public InsertSnippetComponent(){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("insertSnippetDialog.fxml"));
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
        snippetListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Snippet> call(ListView<Snippet> snippetListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Snippet snippet, boolean b) {
                        super.updateItem(snippet, b);
                        if (!b) {
                            setText(null);
                            setGraphic(new SnippetListCell(snippet));
                        } else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        });
        try {
            ObservableList<Snippet> snippets = FXCollections.observableArrayList(SnippetManager.getInstance().findSnippet("", ""));
            filteredData=new FilteredList<>(snippets, s->true);
            searchField.textProperty().addListener((observableValue, s, t1) -> {
                if(t1!=null&&!t1.isBlank()){
                    System.out.println(t1);
                    filteredData.setPredicate(snippet -> snippet.name().contains(t1)||
                            snippet.content().contains(searchField.getText())
                    ||snippet.description().contains(searchField.getText()));
                }
            });
            snippetListView.setItems(filteredData);

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
        stage.initStyle(StageStyle.UNDECORATED);

        insertButton.setOnAction(actionEvent -> {
            selectedObject.setValue(snippetListView.getSelectionModel().getSelectedItem());
            stage.close();
        });
        closeButton.setOnAction((e)-> stage.close());
        stage.showAndWait();
        return selectedObject.get();
    }
    private static class SnippetListCell extends AnchorPane {

        private TextFlow headerFlow;
        private Text name;
        private Text description;

        private TextFlow   content;
        private GridPane gridPane;
        public SnippetListCell(Snippet snip){
            super();
            init();
            setName(snip.name());
            setDescription(snip.description());
            setContent(snip.content());
        }
        public void init(){
            this.getStyleClass().add("insert-snippet-dialog");
            headerFlow =new TextFlow();
            name=new Text();
            headerFlow.getStyleClass().add("header-text-flow");
            name.getStyleClass().add("name-text-flow");
            description=new Text();
            description.getStyleClass().add("description-text-flow");
            headerFlow.getChildren().add(name);
            headerFlow.getChildren().add(description);
            content=new TextFlow();
            content.getStyleClass().add("content-text-flow");

            gridPane=new GridPane();
            this.getChildren().addAll(gridPane);

            AnchorPane.setTopAnchor(gridPane,0.0);
            AnchorPane.setBottomAnchor(gridPane,0.0);
            AnchorPane.setLeftAnchor(gridPane,0.0);
            AnchorPane.setRightAnchor(gridPane,0.0);

            ColumnConstraints c1=new ColumnConstraints();
            c1.setFillWidth(true);
            c1.setHalignment(HPos.LEFT);

            gridPane.getColumnConstraints().add(c1);
            gridPane.addColumn(0, headerFlow,content);

            GridPane.setColumnSpan(content,2);

        }

        public void setName(String name){
            Text nameT=new Text(name);
            this.headerFlow.getChildren().add(nameT);
        }
        public void setDescription(String s){
            Text c=new Text(s);
            this.headerFlow.getChildren().add(c);
        }
        public void setContent(String s){
            Text c=new Text(s);
            this.content.getChildren().add(c);
        }
    }
}
