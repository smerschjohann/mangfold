package it.tty0.mangfold.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScriptEngineFactory {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ScriptEngineManager engineManager;

    public ScriptEngineFactory(ClassLoader classLoader) {
        this.engineManager = new ScriptEngineManager(classLoader);

        for (javax.script.ScriptEngineFactory f : engineManager.getEngineFactories()) {
            log.info("Activated scripting support for {}", f.getLanguageName());
            log.debug("Activated scripting support with engine {}({}) for {}({}) with mimetypes {} and file extensions {}",
                    f.getEngineName(), f.getEngineVersion(), f.getLanguageName(), f.getLanguageVersion(),
                    f.getMimeTypes(), f.getExtensions());
        }
    }

    public List<String> getLanguages() {
        return engineManager.getEngineFactories().stream()
                .map(javax.script.ScriptEngineFactory::getExtensions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void scopeValues(ScriptEngine engine, Map<String, Object> scopeValues) {
        if(Stream.of("js", "javascript", "nashorn")
                .anyMatch(s -> s.equals(engine.getFactory().getLanguageName()))) {
            scopeValuesNashorn(engine, scopeValues);
        } else {
            scopeValuesNormal(engine, scopeValues);
        }
    }

    private void scopeValuesNashorn(ScriptEngine engine, Map<String, Object> scopeValues) {
        Set<String> expressions = new HashSet<String>();

        for (Map.Entry<String, Object> entry : scopeValues.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());

            if (entry.getValue() instanceof Class) {
                expressions.add(String.format("%s = %s.static;", entry.getKey(), entry.getKey()));
            }
        }
        String scriptToEval = String.join("\n", expressions);
        try {
            engine.eval(scriptToEval);
        } catch (ScriptException e) {
            log.error("ScriptException while importing scope: {}", e.getMessage());
        }
    }

    private void scopeValuesNormal(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        for (Map.Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
    }

    public ScriptEngine createScriptEngine(String fileExtension) {
        ScriptEngine engine = engineManager.getEngineByExtension(fileExtension);

        if (engine == null) {
            engine = engineManager.getEngineByName(fileExtension);
        }

        if (engine == null) {
            engine = engineManager.getEngineByMimeType(fileExtension);
        }

        return engine;
    }
}
