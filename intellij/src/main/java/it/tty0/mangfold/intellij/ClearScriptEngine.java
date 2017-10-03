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
import java.util.function.BiFunction;


public class ClearScriptEngine extends AnAction {
    private final static Logger log = LoggerFactory.getLogger(ClearScriptEngine.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String extension = DocumentHelper.getExtension(event);

        Connection.getSingleton().getClient().clearEngine(extension).handle(ClearScriptEngine::handleResult);
    }

    private static Object handleResult(ScriptResponse scriptResponse, Throwable throwable) {
        if (throwable == null) {
            Notifications.Bus.notify(new Notification("mangfold",
                    scriptResponse.getState() == ScriptResponse.State.ERROR ? "Mangfold Error" : "Mangfold Cleared",
                    "Result: " + scriptResponse.getMessage(),
                    NotificationType.INFORMATION));
        } else {
            log.error("error occured", throwable);
        }
        return null;
    }


}
