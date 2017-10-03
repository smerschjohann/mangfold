package it.tty0.mangfold.internal;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScriptRunner {
    private final ScriptEngineFactory scriptEngineFactory;
    private final Map<String, ScriptEngine> runningScriptEngines = new HashMap<>();
    private final Map<String, Object> bindings = new HashMap<>();

    public ScriptRunner(ClassLoader classLoader) {
        this.scriptEngineFactory = new ScriptEngineFactory(classLoader);
    }

    public ScriptRunner setBindings(Map<String, Object> bindings) {
        this.bindings.clear();
        this.bindings.putAll(bindings);

        runningScriptEngines.values().forEach(engine -> scriptEngineFactory.scopeValues(engine, bindings));
        return this;
    }

    public ScriptRunner putBinding(String key, Object value) {
        this.bindings.put(key, value);
        Map<String, Object> objectMap = new HashMap<>(1);
        objectMap.put(key, value);
        runningScriptEngines.values().forEach(engine -> scriptEngineFactory.scopeValues(engine, objectMap));
        return this;
    }

    public void clearEngine(String language) {
        runningScriptEngines.remove(language);
    }

    public Optional<ScriptEngine> getScriptEngine(String language) {
        ScriptEngine engine = runningScriptEngines.get(language);
        if(engine != null)
            return Optional.of(engine);

        engine = this.scriptEngineFactory.createScriptEngine(language);
        if(engine != null) {
            runningScriptEngines.put(language, engine);
            scriptEngineFactory.scopeValues(engine, bindings);
            return Optional.of(engine);
        }
        return Optional.empty();
    }


}
