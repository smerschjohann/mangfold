package it.tty0.mangfold.intellij.network;

public class ScriptRequest {
    public enum Type {
        KEEP_ALIVE,
        RUN,
        CLEAR
    }
    private final Type type;
    private final int id;
    private final String language;
    private final String code;

    public ScriptRequest(int id) {
        type = Type.KEEP_ALIVE;
        this.id = id;
        this.language = null;
        this.code = null;
    }

    public ScriptRequest(Type type, int id, String language, String code) {
        this.type = type;
        this.id = id;
        this.language = language;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

    public Type getType() {
        return type;
    }
}
