package org.sk.fxcss;

import com.github.javafaker.Faker;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

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

public class CodeEditorTab extends Tab {
    VirtualizedScrollPane<CodeArea> scrollPane;
    private CodeArea mainEditor;
    private final KeyCombination cutKC=new KeyCodeCombination(KeyCode.X,KeyCombination.CONTROL_DOWN);
    private final KeyCombination copyKC=new KeyCodeCombination(KeyCode.C,KeyCombination.CONTROL_DOWN);
    private final KeyCombination pasteKC=new KeyCodeCombination(KeyCode.V,KeyCombination.CONTROL_DOWN);
    private final KeyCombination undoKC=new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN);
    private final KeyCombination redoKC=new KeyCodeCombination(KeyCode.Y,KeyCombination.CONTROL_DOWN);
    private final KeyCombination insertSnippetKC=new KeyCodeCombination(KeyCode.I,KeyCombination.CONTROL_DOWN);
    private final KeyCombination saveSnippetKC=new KeyCodeCombination(KeyCode.D,KeyCombination.CONTROL_DOWN);


    private final BooleanProperty changedProperty=new SimpleBooleanProperty(true);
    private final BooleanProperty savedProperty =new SimpleBooleanProperty(false);
    private File file;
    public CodeEditorTab(File f){
        super();
        init(f);
    }
    public CodeEditorTab(){
        File f ;
        try {
            Faker faker=new Faker();
            f = File.createTempFile(AppState.TEMP_FILE_PATH+faker.number().randomDigitNotZero()+faker.number().randomDigitNotZero()
                    ,".css");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        f.deleteOnExit();
        setText("untitled");
        init(f);
    }
    public void init(File f){

        file=f;
        mainEditor=new CodeArea();
        scrollPane=new VirtualizedScrollPane<>(mainEditor);
        AnchorPane.setLeftAnchor(scrollPane,0.0);
        AnchorPane.setBottomAnchor(scrollPane,0.0);
        AnchorPane.setRightAnchor(scrollPane,0.0);
        AnchorPane.setTopAnchor(scrollPane,0.0);
        initMenu();
        try {
            loadFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainEditor.textProperty().addListener((observableValue, s, t1) -> changedProperty.setValue(true));

        mainEditor.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(keyEvent.isControlDown()){
                //Switch, not if. I will add others in the future.
                switch (keyEvent.getCode()) {
                    case SLASH -> {
                        IndexRange selection = mainEditor.getSelection();
                        if (selection.getLength() != 0) {
                            mainEditor.insertText(selection.getStart(), "/*");

                            mainEditor.insertText(selection.getEnd() + 2, "*/");
                        }
                    }
                    case F -> searchInStyle();
                    default -> {
                    }
                }
            }
        });
        this.setContent(scrollPane);
    }

    private void searchInStyle() {
        TextField searchField=new TextField();
        searchField.setPromptText("Enter to search, Esc to close");
        Stage stage=new Stage();
        Scene scene=new Scene(searchField,100,30);
        stage.setScene(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED,(e)->{
            if(e.getText().equals(KeyCode.ESCAPE.getChar()))
                stage.close();
        });
        searchField.textProperty().addListener((observableValue, s, search) -> {
            if(search!=null&&!search.isBlank()){
                mainEditor.getParagraphs().stream().filter((p)-> p.getText().contains(search)).forEach((p)->{
//TODO implement search
                });
            }
        });

        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }
    private void initMenu(){
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
        undo.setOnAction((e)->mainEditor.undo());
        undo.setAccelerator(undoKC);
        MenuItem redo=new MenuItem("Redo");
        redo.setOnAction((e)->mainEditor.redo());
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
                        saveSnippetMenu.setDisable(t1==null||t1.isBlank()));

        menu.getItems().addAll(insertSnippet,saveSnippetMenu);
        mainEditor.setContextMenu(menu);
        mainEditor.setParagraphGraphicFactory(LineNumberFactory.get(mainEditor));

    }
    private void showSnippetDialog() {
        InsertSnippetComponent snip=new InsertSnippetComponent();
        int caretPosition = mainEditor.getCaretPosition();
        Snippet snippet = snip.showSnippetDialog();
        mainEditor.insertText(caretPosition,snippet.content());
    }
    private void showSaveSnippetDialog() {
        String value = mainEditor.selectedTextProperty().getValue();
        try {
            SaveSnippetComponent dialog=new SaveSnippetComponent(value);
            dialog.showSaveSnippetDialog();
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }


    private URI getFileURI() throws URISyntaxException {
        String protocol="file://";
        return new URI(protocol+file.getAbsolutePath());
    }
    private void writeToFile(String s) throws URISyntaxException, IOException {
        URI fileURI = getFileURI();
        Files.writeString(Path.of(fileURI),s);
    }
    private void loadFile() throws IOException {
        if(file==null) return;
        List<String> strings = Files.readAllLines(Path.of(file.getPath()));
        strings.forEach((s)->mainEditor.appendText(s+"\n"));
    }

    public void undo() {
        mainEditor.undo();
    }

    public void redo() {
        mainEditor.redo();
    }
    public void loadTemplate(String t1) {
        String template=resolveTemplate(t1);
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


    public void saveToFile(File f) throws IOException {
        if(f!=null){
            if(!f.exists())
                if(!f.createNewFile()) {
                    throw new RuntimeException("Cannot create the file " + file.getName());
                }
            String text = mainEditor.getText();
            file=f;
            try {
                writeToFile(text);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            this.setText(f.getName());
        }
    }

    public URL getStylesheet() throws URISyntaxException, IOException {
        URI fileURI=getFileURI();
        URL url = fileURI.toURL();
        if(changedProperty.get())
            Files.writeString(Path.of(fileURI), mainEditor.getText());
        return url;
    }

    public void setFile(File file) throws IOException {
        this.file=file;
        loadFile();
    }
}
