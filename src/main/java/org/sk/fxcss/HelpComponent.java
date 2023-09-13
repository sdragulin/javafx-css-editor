package org.sk.fxcss;


import javafx.scene.control.DialogPane;

public class HelpComponent extends DialogPane {

    public HelpComponent(){
        this.setHeaderText("Quick'N Dirty JavaFX CSS Editor");
        this.setContentText("""
                Shortcuts:
                CTRL+N: New stylesheet
                CTRL+S: Applies current stylesheet and automatically saves it.
                        Due to how JavaFX manages stylesheets, they have to be loaded from a file, so
                        they have to be saved first.
                CTRL+W: Save current stylesheet to a different file and change the editing file to the
                        new location. All modifications will be saved here.
                CTRL+D: Open the dialog to save the current selection as a snippet.
                CTRL+I: Insert a previously saved snippet.
                CTRL+Z: Undo edit
                CTRL+Y: Redo edit
                CTRL+H: Show this help
                CTRL+O: Open stylesheet.
                CTRL+Q: Exit.
                """);
    }



    public void show() {
        this.layout();
    }

}
