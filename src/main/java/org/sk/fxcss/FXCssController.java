package org.sk.fxcss;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Optional;
import java.util.concurrent.Executors;

public class FXCssController {
    public TitledPane containerTitledPane;
    public ButtonBar containerButtonBar;

    private final SimpleObjectProperty<CodeEditorTab> activeEditor=new SimpleObjectProperty<>();
    public ElementComponents components;
    public ComboBox<String> templates;
    public Button applyButton;

    public MenuItem redoMenuItem;
    public MenuItem undoMenuItem;
    public MenuItem closeMenuItem;
    public MenuItem saveAsMenuItem;
    public MenuItem openSheetMenuItem;
    public MenuItem newSheetMenuItem;
    public MenuItem aboutMenuItem;
    public AnchorPane containerPane;
    public Button loadFXMLButton;
    public Button reloadFXMLButton;
    public TabPane editorContainerTabPane;
    private AppState state;
    private final ObjectProperty<File> fxmlFileProperty=new SimpleObjectProperty<>();

    private final KeyCombination closeKC=new KeyCodeCombination(KeyCode.Q,KeyCombination.CONTROL_DOWN);
    private final KeyCombination newKC=new KeyCodeCombination(KeyCode.N,KeyCombination.CONTROL_DOWN);

    private final KeyCombination applyStyleKC=new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN);

    private final KeyCombination saveAsKC=new KeyCodeCombination(KeyCode.W,KeyCombination.CONTROL_DOWN);
    private final KeyCombination openKC=new KeyCodeCombination(KeyCode.O,KeyCombination.CONTROL_DOWN);
    private final KeyCombination aboutKC=new KeyCodeCombination(KeyCode.H,KeyCombination.CONTROL_DOWN);

    private final BooleanProperty savedProperty =new SimpleBooleanProperty(false);

    private final ObjectProperty<File> editingFileProperty=new SimpleObjectProperty<>();
    public FXCssController() {

    }
    public void initialize(){
        reloadFXMLButton.setDisable(true);
        reloadFXMLButton.disableProperty().bind(fxmlFileProperty.isNull());

        editorContainerTabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, tab, t1) -> {
            if(!(t1 instanceof CodeEditorTab))
                throw new UnsupportedOperationException("Only CodeEditor tabs are supported");
            activeEditor.setValue((CodeEditorTab) t1);
        });
        editorContainerTabPane.getTabs().add(new CodeEditorTab());
        fxmlFileProperty.addListener((observableValue, file, t1) -> {
            if(t1==null) {
                components = new ElementComponents();
                containerPane.getChildren().add(components);
            }else{
                try {
                    reloadFXML();
                } catch (IOException e) {
                    showErrorWarning("Could not open FXML file",e);
                    throw new RuntimeException(e);
                }

            }
        });
        templates.valueProperty().addListener((observableValue, s, t1) -> activeEditor.get().loadTemplate(t1));
        closeMenuItem.setAccelerator(closeKC);
        saveAsMenuItem.setAccelerator(saveAsKC);
        openSheetMenuItem.setAccelerator(openKC);
        newSheetMenuItem.setAccelerator(newKC);
        aboutMenuItem.setAccelerator(aboutKC);
        containerTitledPane.setContentDisplay(ContentDisplay.RIGHT);
        final double graphicMarginRight = 10; //change if needed

        containerButtonBar.translateXProperty().bind(Bindings.createDoubleBinding(
                () -> containerTitledPane.getWidth() - containerButtonBar.getLayoutX() - containerButtonBar.getWidth() - graphicMarginRight,
                containerTitledPane.widthProperty())
        );

        editingFileProperty.addListener((observableValue, file, t1) -> {
            try {
                applyStyle();
            } catch (IOException e) {
                showErrorWarning("Cannot open file "+t1);
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                showErrorWarning("File URI is ill formed "+t1+"\n"+e.getMessage());
                throw new RuntimeException(e);
            }
        });
//        mainEditor.textProperty().addListener((observableValue, s, t1) -> changedProperty.setValue(true));
    }

    public void setAccelerators(Scene scene){
        scene.getAccelerators()
                .put(applyStyleKC,()->applyButton.fire());
        scene.getAccelerators()
                .put(saveAsKC,()-> {
                    try {
                        saveAsStyle();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void undo(){
        activeEditor.get().undo();
    }
    public void redo(){
        activeEditor.get().redo();
    }
    public void saveAsStyle() throws IOException {

        Stage mainStage = state.getMainStage();
        String property = System.getProperty("user.dir");
        FileChooser chooser=new FileChooser();
        chooser.setInitialDirectory(new File(property));
        chooser.setTitle("Save file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Css files","*.css"));
        File file = chooser.showSaveDialog(mainStage);
        activeEditor.get().saveToFile(file);

    }
    public void applyStyle() throws IOException, URISyntaxException {
        URL stylesheet = activeEditor.get().getStylesheet();
        containerPane.getStylesheets().remove(stylesheet.toExternalForm());
        containerPane.getStylesheets().add(stylesheet.toExternalForm());
    }
    public void setState(AppState state) {
        this.state=state;
    }

    /***
     * MENU ACTIONS
     *
     **/
    public void close() {
        Platform.exit();
    }
    public void openStyle() {
        FileChooser fileChooser=new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSS","*.css"));
        File file = fileChooser.showOpenDialog(state.getMainStage());

        CodeEditorTab tab=new CodeEditorTab(file);
        editorContainerTabPane.getTabs().add(tab);
        editorContainerTabPane.getSelectionModel().select(tab);
//            activeEditor.get().setFile(file);
    }
    public void newStyle(){
        if(!savedProperty.get()){
            Alert confirmation=new Alert(Alert.AlertType.CONFIRMATION);
            ButtonType saveFileFirst=new ButtonType("Save First", ButtonBar.ButtonData.YES);
            ButtonType proceedAnyway=new ButtonType("Open new anyway",ButtonBar.ButtonData.NO);
            ButtonType cancel=new ButtonType("Cancel save",ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmation.getButtonTypes().addAll(saveFileFirst,proceedAnyway,cancel);

            confirmation.setContentText("Do you want to save the current file?");
            Optional<ButtonType> buttonType = confirmation.showAndWait();
            if(buttonType.isEmpty()) return;
            ButtonBar.ButtonData buttonData = buttonType.get().getButtonData();

            if(buttonData==ButtonBar.ButtonData.CANCEL_CLOSE) return;
            if(buttonData== ButtonBar.ButtonData.YES) {
                try {
                    saveAsStyle();
                } catch (IOException e) {
                    showErrorWarning("Could not save to file");
                    throw new RuntimeException(e);
                }
            }
        }
        CodeEditorTab tab=new CodeEditorTab();
        editorContainerTabPane.getTabs().add(tab);

    }
    public void showAbout(){
        HelpComponent help=new HelpComponent();
        help.show();
    }
    private void showErrorWarning(String message){
        Notifications.create()
                .title("Error encountered")
                .text(message)
                .darkStyle()
                .showError();
    }
    private void showErrorWarning(String message, Exception e) {
        String content= message + "\nException thrown:"+e.getMessage()+"\n"+
                e.getClass()+" "+e.getCause();
        showErrorWarning(content);

    }
    private void showSuccess(String message){
        Notifications.create()
                .title("Success")
                .text(message)
                .darkStyle()
                .showInformation();

    }
    public void loadFXML()   {
        FileChooser chooser=new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "FXML","*.fxml"
        ));
        fxmlFileProperty.setValue(chooser.showOpenDialog(state.getMainStage()));
    }


    public void reloadFXML() throws IOException {
        Task<Void> t= new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (!fxmlFileProperty.isNull().get()) {
                    FXMLLoader loader = new FXMLLoader(new URL("file://" + fxmlFileProperty.get().getAbsolutePath()));
                    Parent fxmlContent = loader.load();
                    Platform.runLater(() -> containerPane.getChildren().add(fxmlContent));
                }
                return null;
            }
        };

        Executors.newSingleThreadExecutor().execute(t);

    }
}