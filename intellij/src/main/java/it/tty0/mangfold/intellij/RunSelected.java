package it.tty0.mangfold.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

public class RunSelected extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String extension = DocumentHelper.getExtension(event);
        String code = DocumentHelper.getSelectedSourceCode(event);

        RunScript.intellijRunScript(extension, code);
    }
}
