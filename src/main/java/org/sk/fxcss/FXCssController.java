package org.sk.fxcss;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;


import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import org.fxmisc.richtext.CodeArea;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FXCssController {
    public CodeArea mainEditor;
    public ElementComponents components;
    public ComboBox<String> templates;
    public Button applyButton;
    public Button saveButton;
    public MenuItem redoMenuItem;
    public MenuItem undoMenuItem;
    public MenuItem closeMenuItem;
    public MenuItem saveAsMenuItem;
    public MenuItem openSheetMenuItem;
    public MenuItem newSheetMenuItem;
    public MenuItem aboutMenuItem;
    private AppState state;

    private final KeyCombination closeKC=new KeyCodeCombination(KeyCode.Q,KeyCombination.CONTROL_DOWN);
    private final KeyCombination newKC=new KeyCodeCombination(KeyCode.N,KeyCombination.CONTROL_DOWN);
    private final KeyCombination cutKC=new KeyCodeCombination(KeyCode.X,KeyCombination.CONTROL_DOWN);
    private final KeyCombination copyKC=new KeyCodeCombination(KeyCode.C,KeyCombination.CONTROL_DOWN);
    private final KeyCombination pasteKC=new KeyCodeCombination(KeyCode.V,KeyCombination.CONTROL_DOWN);
    private final KeyCombination undoKC=new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN);
    private final KeyCombination redoKC=new KeyCodeCombination(KeyCode.Y,KeyCombination.CONTROL_DOWN);
    private final KeyCombination insertSnippetKC=new KeyCodeCombination(KeyCode.I,KeyCombination.CONTROL_DOWN);
    private final KeyCombination saveSnippetKC=new KeyCodeCombination(KeyCode.D,KeyCombination.CONTROL_DOWN);
    private final KeyCombination applyStyleKC=new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN);
    private final KeyCombination saveAsKC=new KeyCodeCombination(KeyCode.W,KeyCombination.CONTROL_DOWN);
    private final KeyCombination openKC=new KeyCodeCombination(KeyCode.O,KeyCombination.CONTROL_DOWN);
    private final KeyCombination aboutKC=new KeyCodeCombination(KeyCode.H,KeyCombination.CONTROL_DOWN);


    private final BooleanProperty savedProperty =new SimpleBooleanProperty(false);
    /*
    * while the user is still writing to a temp file, this should not change
    *
    */
    private final BooleanProperty changedProperty=new SimpleBooleanProperty(true);
    private final ObjectProperty<File> editingFileProperty=new SimpleObjectProperty<>();
    public FXCssController() {
        initEditor();

    }
    public void initialize(){
        templates.valueProperty().addListener((observableValue, s, t1) -> {
            String template=resolveTemplate(t1);
            loadTemplate(template);
        });
        redoMenuItem.setAccelerator(redoKC);
        undoMenuItem.setAccelerator(undoKC);
        closeMenuItem.setAccelerator(closeKC);
        saveAsMenuItem.setAccelerator(saveAsKC);
        openSheetMenuItem.setAccelerator(openKC);
        newSheetMenuItem.setAccelerator(newKC);
        aboutMenuItem.setAccelerator(aboutKC);
        mainEditor.setPadding(new Insets(0.0,0.0,0.0,10.0));
        ContextMenu menu=new ContextMenu();

        MenuItem copy=new MenuItem("Copy");
        copy.setOnAction((e)->mainEditor.copy());
        copy.setAccelerator(copyKC);

        MenuItem cut=new MenuItem("Cut");
        cut.setAccelerator(cutKC);
        cut.setOnAction((e)->mainEditor.cut());
        MenuItem paste=new MenuItem("Paste");
        paste.setOnAction((e)->mainEditor.paste());
        paste.setAccelerator(pasteKC);

        MenuItem undo=new MenuItem("Undo");
        undo.setOnAction((e)->undo());
        undo.setAccelerator(undoKC);
        MenuItem redo=new MenuItem("Redo");
        redo.setOnAction((e)->redo());
        redo.setAccelerator(redoKC);
        menu.getItems().addAll(copy,cut,paste);

        SeparatorMenuItem separatorMenuItem=new SeparatorMenuItem();
        menu.getItems().add(separatorMenuItem);
        menu.getItems().addAll(undo,redo);

        SeparatorMenuItem separator=new SeparatorMenuItem();
        menu.getItems().add(separator);

        MenuItem insertSnippet=new MenuItem("Insert snippet");
        insertSnippet.setOnAction((e)-> showSnippetDialog());
        insertSnippet.setAccelerator(insertSnippetKC);
        MenuItem saveSnippetMenu=new MenuItem("Save snippet");
        saveSnippetMenu.setOnAction((e)->showSaveSnippetDialog());
        saveSnippetMenu.setAccelerator(saveSnippetKC);
        saveSnippetMenu.setDisable(true);
        mainEditor.selectedTextProperty()
                .addListener((observableValue, s, t1) ->
                        saveSnippetMenu.setDisable(t1.isBlank()));

        menu.getItems().addAll(insertSnippet,saveSnippetMenu);
        mainEditor.setContextMenu(menu);
        editingFileProperty.addListener((observableValue, file, t1) -> {
            try {
                loadFile(t1);
                applyStyle();
                changedProperty.set(false);
                savedProperty.set(true);
            } catch (IOException e) {
                showErrorWarning("Cannot open file "+t1);
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                showErrorWarning("File URI is ill formed "+t1+"\n"+e.getMessage());
                throw new RuntimeException(e);
            }
        });
        mainEditor.textProperty().addListener((observableValue, s, t1) -> changedProperty.setValue(true));
    }
    private void showSaveSnippetDialog() {
        String value = mainEditor.selectedTextProperty().getValue();
        try {
            SaveSnippetComponent dialog=new SaveSnippetComponent(value);
            dialog.showSaveSnippetDialog();
        } catch (IOException e) {
            showErrorWarning("Could not save snippet");
            throw new RuntimeException(e);
        }
        showSuccess("Snippet Saved");
    }
    private void showSnippetDialog() {
        InsertSnippetComponent snip=new InsertSnippetComponent();
        int caretPosition = mainEditor.getCaretPosition();
        Snippet snippet = snip.showSnippetDialog();
        mainEditor.insertText(caretPosition,snippet.content());
    }

    /**
     *
     * @return The file URI
     *
     */
    private URI getFileURI() throws URISyntaxException {
        String protocol="file://";
        return new URI(protocol+editingFileProperty.get().getAbsolutePath());
    }
    private void writeToFile(String s) throws URISyntaxException, IOException {
        URI fileURI = getFileURI();
        Files.writeString(Path.of(fileURI),s);
    }

    public void setAccelerators(Scene scene){
        scene.getAccelerators()
                .put(applyStyleKC,()->applyButton.fire());
        scene.getAccelerators()
                .put(saveAsKC,()->saveButton.fire());
    }

    private void loadTemplate(String template) {
        File f=new File(Objects.requireNonNull(getClass()
                .getResource(AppState.TEMPLATE_FOLDER + File.separator + template + ".css")).getFile());
        if(f.exists()){
            try {
                List<String> strings = Files.readAllLines(Path.of(f.toURI()));
                strings.forEach((s -> mainEditor.appendText(s+"\n")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String resolveTemplate(String string) {
        final String regex = "(?=[A-Z][a-z])";
        final String subst = "-";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        // The substituted value will be contained in the result variable
        final String result = matcher.replaceAll(subst);
        return result.toLowerCase().substring(1);
    }

    public void undo(){
        mainEditor.undo();
    }
    public void redo(){
        mainEditor.redo();
    }
    public void saveAsStyle() throws IOException {

        Stage mainStage = state.getMainStage();
        String property = System.getProperty("user.dir");
        FileChooser chooser=new FileChooser();
        chooser.setInitialDirectory(new File(property));
        chooser.setTitle("Save file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Css files","*.css"));
        File file = chooser.showSaveDialog(mainStage);
        if(file!=null){
            if(!file.exists())
                if(!file.createNewFile()) {
                    showErrorWarning("Cannot create file "+file.getName()+"\n at "+file.getPath());
                    throw new RuntimeException("Cannot create the file " + file.getName());
                }
            String text = mainEditor.getText();
            editingFileProperty.setValue(file);
            try {
                writeToFile(text);
            } catch (URISyntaxException e) {
                showErrorWarning("URI Location is not valid\n"+e.getMessage());
                throw new RuntimeException(e);
            }

        }
    }
    public void applyStyle() throws IOException, URISyntaxException {
        String text = mainEditor.getText();
        URI fileURI=getFileURI();
        URL url = fileURI.toURL();
        if(changedProperty.get()) {
            Files.writeString(Path.of(fileURI), text);
            components.getStylesheets().clear();
            components.getStylesheets().add(url.toExternalForm());
        }
    }
    public void setState(AppState state) {
        this.state=state;
    }
    private void loadFile(File file) throws IOException {
        List<String> strings = Files.readAllLines(Path.of(file.getPath()));
        strings.forEach((s)->mainEditor.appendText(s+"\n"));
    }
    private void initEditor(){
        if(components!=null) {
            //clear styles
            components.getStylesheets().clear();
            //refresh editor
            mainEditor.clear();
        }
        File f=new File(AppState.TEMP_FILE_PATH);
        if(f.exists()) {
            f.delete();
        }
        try {
            boolean newFile = f.createNewFile();
            if(!newFile) {
                showErrorWarning("Could not create temp file");
            }
        } catch (IOException e) {
            showErrorWarning("Could not create new file");
            throw new RuntimeException(e);
        }
        editingFileProperty.set(f);
    }

    /***
     * MENU ACTIONS
     *
     **/
    public void close() {
        System.exit(0);
    }
    public void openStyle() {
        FileChooser fileChooser=new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSS","*.css"));
        File file = fileChooser.showOpenDialog(state.getMainStage());
        editingFileProperty.setValue(file);
    }
    public void newStyle(){
        if(!savedProperty.get()){
            try {
                saveAsStyle();
            } catch (IOException e) {
                showErrorWarning("Could not save to file");
                throw new RuntimeException(e);
            }
        }
        initEditor();
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
    private void showSuccess(String message){
        Notifications.create()
                .title("Success")
                .text(message)
                .darkStyle()
                .showInformation();

    }
}