package it.tty0.mangfold.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import it.tty0.mangfold.ScriptResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class RunScript extends AnAction {
    private final static Logger log = LoggerFactory.getLogger(RunScript.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String extension = DocumentHelper.getExtension(event);
        String code = DocumentHelper.getSourceCode(event);
        intellijRunScript(extension, code);
    }

    public static void intellijRunScript(String extension, String code) {
        CompletableFuture<ScriptResponse> runRemote = Connection.getSingleton().getClient().runRemote(extension, code);
        ScriptResponse response = null;
        Exception throwable = null;

        try {
            response = runRemote.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            throwable = ex;
        } catch (TimeoutException ignored) {
        }

        if(response == null && throwable == null) {
            Notifications.Bus.notify(new Notification("mangfold",
                    "Mangfold waiting for completion",
                    "Execution took longer than 5 seconds, waiting for completion...",
                    NotificationType.INFORMATION));
            runRemote.handle(((scriptResponse, t) -> {
                handleResult(scriptResponse, t);
                return null;
            }));
        } else {
            handleResult(response, throwable);
        }
    }

    private static void handleResult(ScriptResponse scriptResponse, Throwable throwable) {
        if (throwable == null) {
            Notifications.Bus.notify(new Notification("mangfold",
                    scriptResponse.getState() == ScriptResponse.State.ERROR ? "Mangfold Error" : "Mangfold Success",
                    "Result: " + scriptResponse.getMessage(),
                    NotificationType.INFORMATION));
        } else {
            log.error("error occured", throwable);
        }
    }


}
