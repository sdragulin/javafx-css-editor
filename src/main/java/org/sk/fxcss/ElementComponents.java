package org.sk.fxcss;

import com.github.javafaker.Faker;
import javafx.beans.property.SimpleStringProperty;

import javafx.fxml.FXMLLoader;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;


import java.io.IOException;
import java.util.Random;

public class ElementComponents extends AnchorPane {

    public ListView<String> listView;
    public TreeView<String> treeView;
    public TableView<FakeDataRecord> tableView;
    public BubbleChart bubbleChart;
    public LineChart lineChart;
    public PieChart pieChart;
    public StackedBarChart stackedBarChart;
    public ScatterChart scatterChart;
    public BarChart barChart;

    public ElementComponents() {
        FXMLLoader loader=new FXMLLoader(getClass().getResource("components.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void initialize(){
        initList();
        TreeItem<String> root = initTree();
        treeView.setRoot(root);
        treeView.setShowRoot(true);
        initTable();
    }
    public void initList(){
        Faker faker=new Faker();
        for(int i=0;i<100;i++){
            listView.getItems().add(faker.chuckNorris().fact());
        }
    }
    public TreeItem<String> initTree(){
        Faker faker=new Faker();
        TreeItem<String> root=new TreeItem<>();
        root.setValue(faker.funnyName().name());
        for(int i=0;i<4;i++) {
            TreeItem<String> child=new TreeItem<>();
            child.setValue(faker.beer().name());
            root.getChildren().add(child);
            for(int j=0;j<5;j++){
                TreeItem<String> nephew=new TreeItem<>();
                nephew.setValue(faker.gameOfThrones().character());
                child.getChildren().add(nephew);
            }
        }
        return root;
    }
    public void initTable(){
        TableColumn<FakeDataRecord, String> col1=new TableColumn<>("First name");
        TableColumn<FakeDataRecord, String> col2=new TableColumn<>("Second name");
        TableColumn<FakeDataRecord, String> col3=new TableColumn<>("Quote");

        col1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().firstName()));
        col2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastName()));
        col3.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().quote()));
        Faker f=new Faker();
        tableView.getColumns().addAll(col1,col2,col3);
        for(int i=0;i<100;i++){
            tableView.getItems().add(
                    new FakeDataRecord(
                        f.name().firstName(),
                        f.name().lastName(),
                        f.lebowski().quote()
                    )
            );
        }

    }

    public record FakeDataRecord(String firstName,String lastName, String quote) {
    }

}
