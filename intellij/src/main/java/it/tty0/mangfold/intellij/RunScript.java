package it.tty0.mangfold.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import it.tty0.mangfold.ScriptResponse;


public class RunScript extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String extension = DocumentHelper.getExtension(event);
        String code = DocumentHelper.getSourceCode(event);
        intellijRunScript(extension, code);
    }

    public static void intellijRunScript(String extension, String code) {
        Connection.getSingleton().getClient().runRemote(extension, code).handle( (scriptResponse, throwable) -> {
            if (throwable == null) {
                Notifications.Bus.notify(new Notification("mangfold",
                        scriptResponse.getState() == ScriptResponse.State.ERROR ? "Mangfold Error" : "Mangfold Success",
                        "Result: " + scriptResponse.getMessage(),
                        NotificationType.INFORMATION));
            }
            return null;
        });
    }


}
