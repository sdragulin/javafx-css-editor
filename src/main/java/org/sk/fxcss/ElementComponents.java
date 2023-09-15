package org.sk.fxcss;

import com.github.javafaker.Faker;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;


import java.io.IOException;


public class ElementComponents extends AnchorPane {

    public ListView<String> listView;
    public TreeView<String> treeView;
    public TableView<FakeDataRecord> tableView;
    public BubbleChart<Number,Number> bubbleChart;
    public LineChart<Number,Number> lineChart;
    public PieChart pieChart;
    public StackedBarChart<String, Number> stackedBarChart;
    public ScatterChart<Number,Number> scatterChart;
    public BarChart<String,Number> barChart;
    public StackedAreaChart<Number,Number> stackedAreaChart;

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
        initPieChart();
        initLineChart();
        initBubbleChart();
        initStackedAreaChart();
        initStackedBarChart();
        initBarChart();
        initScatterChart();
    }

    private void initStackedBarChart() {
        CategoryAxis xAxis = (CategoryAxis) stackedBarChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) stackedBarChart.getYAxis();

        xAxis.setCategories(FXCollections.observableArrayList("Romania","Bulgaria", "Hungary", "Vanuatu", "Tuvalu"));
        xAxis.setLabel("Countries");
        yAxis.setLabel("GDP");

        Faker f=new Faker();
        for (int i=0;i<5;i++){
            XYChart.Series<String,Number> series=new XYChart.Series<>();
            ObservableList<String> categories = xAxis.getCategories();
            for (String category : categories) {
                series.getData().add(new XYChart.Data<>(
                        category,
                        f.number().numberBetween(0, 100)
                ));

            }
            stackedBarChart.getData().add(series);
        }
    }

    private void initScatterChart() {
        NumberAxis xAxis = (NumberAxis) scatterChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) scatterChart.getYAxis();
        xAxis.setLabel("Sample Size");
        xAxis.setUpperBound(100);
        xAxis.setLowerBound(10);
        yAxis.setLabel("Value");
        yAxis.setLowerBound(20);
        yAxis.setUpperBound(1000);
        xAxis.setForceZeroInRange(false);
        Faker faker=new Faker();
        XYChart.Series<Number,Number> series=new XYChart.Series<>();
        series.setName(faker.funnyName().name());
        for(int i=0;i<100;i++){
            series.getData().add(new XYChart.Data<>(faker.number().numberBetween(10,100),faker.number().numberBetween(20,1000)));
        }
        XYChart.Series<Number,Number> anotherSeries=new XYChart.Series<>();
        for(int i=0;i<100;i++){
            anotherSeries.getData().add(new XYChart.Data<>(faker.number().numberBetween(10,100),faker.number().numberBetween(20,1000)));
        }
        anotherSeries.setName(faker.funnyName().name());
        scatterChart.getData().add(series);
    }

    private void initBarChart() {
        barChart.getXAxis().setLabel("Country");
        barChart.getYAxis().setLabel("Disease prevalence");
        barChart.setTitle("Disease prevalence by country");
        Faker f=new Faker();
        String name1=f.medical().diseaseName();
        String name2=f.medical().diseaseName();
        String name3=f.medical().diseaseName();
        String name4=f.medical().diseaseName();
        for(int i=0;i<4;i++){
            XYChart.Series<String,Number> firstSeries=new XYChart.Series<>();
            firstSeries.setName(f.country().name());
            firstSeries.getData().add(new XYChart.Data<>(name1,f.number().numberBetween(10,100)));
            firstSeries.getData().add(new XYChart.Data<>(name2,f.number().numberBetween(10,100)));
            firstSeries.getData().add(new XYChart.Data<>(name3,f.number().numberBetween(10,100)));
            firstSeries.getData().add(new XYChart.Data<>(name4,f.number().numberBetween(10,100)));
            barChart.getData().add(firstSeries);
        }

    }

    private void initStackedAreaChart() {
        Faker f=new Faker();
        NumberAxis yAxis = (NumberAxis) stackedAreaChart.getYAxis();
        NumberAxis xAxis = (NumberAxis) stackedAreaChart.getXAxis();
        xAxis.setLabel("Years");
        yAxis.setLabel("Value");
        stackedAreaChart.setTitle("Stacked Area");
        xAxis.setForceZeroInRange(false);
        for(int i=0;i<4;i++){
            XYChart.Series<Number,Number> series=new XYChart.Series<>();
            series.setName(f.country().name());
            for(int j=0;j<10;j++)
                series.getData().add(new XYChart.Data<>(1990+j*2,f.number().numberBetween(100,600)));
            stackedAreaChart.getData().add(series);
        }

    }

    private void initBubbleChart() {

        ((NumberAxis)(bubbleChart.getXAxis())).setLowerBound(1920);
        ((NumberAxis)(bubbleChart.getXAxis())).setUpperBound(2020);
        ((NumberAxis)(bubbleChart.getXAxis())).setForceZeroInRange(false);
        ((NumberAxis)(bubbleChart.getXAxis())).setTickUnit(10);
        ((NumberAxis)(bubbleChart.getYAxis())).setLowerBound(-100);
        bubbleChart.getXAxis().setLabel("Years");
        bubbleChart.getYAxis().setLabel("Public debt");
        bubbleChart.setTitle("Population size to public debt");
        int numSeries=4;
        Faker faker=new Faker();
        for (int i=0;i<numSeries;i++){
            XYChart.Series<Number,Number> series=new XYChart.Series<>();
            series.setName(faker.country().name());
            for(int j=0;j<10;j++) {
                series.getData().add(new XYChart.Data<>(
                        faker.number().numberBetween(1950, 1950+i*10),
                        faker.number().numberBetween(20, 30+i*2),
                        i
                ));
            }
            bubbleChart.getData().add(series);
        }
        bubbleChart.horizontalZeroLineVisibleProperty().set(false);
        bubbleChart.verticalZeroLineVisibleProperty().set(false);

    }

    private void initLineChart() {
        ((NumberAxis)(lineChart.getXAxis())).setLowerBound(1920);
        ((NumberAxis)(lineChart.getXAxis())).setUpperBound(2020);
        ((NumberAxis)(lineChart.getXAxis())).setForceZeroInRange(false);
        ((NumberAxis)(lineChart.getXAxis())).setTickUnit(10);
        ((NumberAxis)(lineChart.getYAxis())).setLowerBound(-100);
        lineChart.getXAxis().setLabel("Years");
        lineChart.getYAxis().setLabel("Public debt");
        lineChart.setTitle("Public Debt");
        Faker faker=new Faker();
        for(int j=0;j<5;j++){
            XYChart.Series<Number,Number> data=new XYChart.Series<>();
            data.setName(faker.country().name());
            for(int i=0;i<10;i++){
                data.getData().add(new XYChart.Data<>(1920+i,
                        faker.number().numberBetween(-100,100)));
            }
            lineChart.getData().add(data);
        }
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

    public void initPieChart(){
        pieChart.setClockwise(true);
        ObservableList<PieChart.Data> data= FXCollections.observableArrayList();
        Faker faker=new Faker();
        int slicesNum = faker.number().numberBetween(0, 10);

        for(int i=0;i<slicesNum;i++){
            data.add(new PieChart.Data(faker.programmingLanguage().name(),
                    faker.number().randomNumber(2,true)));
        }
        pieChart.setData(data);
        pieChart.setLabelLineLength(20);
        pieChart.setLabelsVisible(true);
        pieChart.setTitle("Programming languages");
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setLegendVisible(true);

    }
    public record FakeDataRecord(String firstName,String lastName, String quote) {
    }

}
