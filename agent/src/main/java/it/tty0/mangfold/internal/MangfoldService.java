package it.tty0.mangfold.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static it.tty0.mangfold.internal.ScriptResponse.State.ERROR;
import static it.tty0.mangfold.internal.ScriptResponse.State.OK;


public class MangfoldService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ScriptRunner scriptRunner;
    private Gson gson = new GsonBuilder().create();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MangfoldService(ScriptRunner scriptRunner) {
        this.scriptRunner = scriptRunner;
    }

    public CompletableFuture<ScriptResponse> runScript(ScriptRequest request) {
        int id = request.getId();
        log.info("> runScript() request={} id={}", request, id);
        try {
            Optional<ScriptEngine> scriptEngine = scriptRunner.getScriptEngine(request.getLanguage());
            if (!scriptEngine.isPresent()) {
                String description = "language " + request.getLanguage() + " not supported";
                log.warn(description);
                return CompletableFuture.completedFuture(new ScriptResponse(id, ERROR, description));
            } else {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Object resultObject = scriptEngine.get().eval(request.getCode());

                        String result = toResultString(resultObject);
                        log.info("< runScript() id={}, result={}", id, result);
                        return new ScriptResponse(id, OK, result);
                    } catch (ScriptException e) {
                        log.error("< runScript() id={}, error in script ", id, e);
                        return new ScriptResponse(id, ERROR, ExceptionUtils.getStackTrace(e));
                    }
                }, executorService);
            }
        } catch (Exception ex) {
            log.error("exception", ex);
            return CompletableFuture.completedFuture(new ScriptResponse(id, ERROR, "error: " + ex.getMessage()));
        }
    }

    private String toResultString(Object resultObject) {
        String result = "";
        if (resultObject != null) {
            if (resultObject instanceof String || isWrapperType(resultObject.getClass())) {
                result = resultObject.toString();
            } else {
                try {
                    result = gson.toJson(resultObject);
                } catch (Exception ex) {
                    log.info("runScript(): could not serialize to json {}", resultObject);
                    result = resultObject.toString();
                }
            }
        }
        return result;
    }

    private static final Set<Class> WRAPPER_TYPES = new HashSet<>(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));
    public static boolean isWrapperType(Class clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }
}
