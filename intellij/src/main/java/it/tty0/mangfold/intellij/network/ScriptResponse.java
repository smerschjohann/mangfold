package it.tty0.mangfold;

public class ScriptResponse {
    public enum State {
        OK,
        ERROR
    }
    private final int id;
    private final State state;
    private final String message;

    public ScriptResponse(int id, State state, String message) {
        this.id = id;
        this.state = state;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}
