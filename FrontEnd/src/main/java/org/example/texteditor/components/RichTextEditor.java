package org.example.texteditor.components;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.MultipleCaretSelection;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import java.util.HashMap;
import java.util.Map;

public class RichTextEditor extends CodeArea {
    private final MultipleCaretSelection<Object, String, Object> multiCaret;
    private final Map<String, MultipleCaretSelection.Caret<Object, String, Object>> userCarets;
    
    public RichTextEditor() {
        this.multiCaret = this.addMultiCaretSelection();
        this.userCarets = new HashMap<>();
        this.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 14;");
    }
    
    public void addUserCaret(String userId, Color color) {
        MultipleCaretSelection.Caret<Object, String, Object> caret = multiCaret.addCaret();
        caret.setCaretFactory(__ -> createCaretNode(color));
        userCarets.put(userId, caret);
    }
    
    public void updateCaretPosition(String userId, int position) {
        MultipleCaretSelection.Caret<Object, String, Object> caret = userCarets.get(userId);
        if (caret != null) {
            caret.moveTo(position);
        }
    }
    
    public void removeUserCaret(String userId) {
        MultipleCaretSelection.Caret<Object, String, Object> caret = userCarets.remove(userId);
        if (caret != null) {
            multiCaret.removeCaret(caret);
        }
    }
    
    private Node createCaretNode(Color color) {
        Rectangle caret = new Rectangle(2, 20);
        caret.setFill(color);
        return caret;
    }
}