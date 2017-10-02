package it.tty0.mangfold;

import it.tty0.mangfold.internal.MangfoldServer;
import it.tty0.mangfold.internal.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class MangfoldAgent {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final int port;
    private final ScriptRunner scriptRunner;
    private MangfoldServer server;

    public MangfoldAgent(int port) {
        this(port, Thread.currentThread().getContextClassLoader());
    }

    public MangfoldAgent(int port, ClassLoader classLoader) {
        this.port = port;
        this.scriptRunner = new ScriptRunner(classLoader);
    }

    public void setBindings(Map<String, Object> bindings) {
        scriptRunner.setBindings(bindings);
    }

    public MangfoldAgent putBinding(String key, Object value) {
        scriptRunner.putBinding(key, value);
        return this;
    }

    public void start() {
        try {
            server = new MangfoldServer(port, scriptRunner);
            server.init();

            log.info("Mangfold started, listening on {}", port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down mangfold agent since JVM is shutting down");
                MangfoldAgent.this.stop();
                System.err.println("*** server shut down");
            }));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final MangfoldAgent agent = new MangfoldAgent(14237, Thread.currentThread().getContextClassLoader());
        agent.start();
        agent.blockUntilShutdown();
    }
}
