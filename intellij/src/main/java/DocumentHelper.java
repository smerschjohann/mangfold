import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class DocumentHelper {
    public DocumentHelper() {
    }

    @NotNull
    static String getSourceCode(AnActionEvent event) {
        Document document = event.getData(PlatformDataKeys.EDITOR).getDocument();
        return document.getText();
    }

    @NotNull
    static String getSelectedSourceCode(AnActionEvent event) {
        String result = "";
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);

        String selectedText = null;
        if (editor != null) {
            selectedText = editor.getSelectionModel().getSelectedText();
            if(Strings.isNullOrEmpty(selectedText)) {
                editor.getSelectionModel().selectLineAtCaret();
                selectedText = editor.getSelectionModel().getSelectedText();
            }
        }

        return Strings.nullToEmpty(selectedText);
    }
}