package it.tty0.mangfold.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MangfoldServer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ServerSocket server;
    final MangfoldService service;
    ExecutorService executor = Executors.newCachedThreadPool();
    private boolean running;
    private CompletableFuture<Void> finished = new CompletableFuture<>();

    public MangfoldServer(int port, ScriptRunner scriptRunner) throws IOException {
        server = new ServerSocket(port);
        server.setReuseAddress(true);
        server.setSoTimeout(10000);

        service = new MangfoldService(scriptRunner);
    }

    public void init() {
        new Thread(this::acceptNewConnections, "SocketAcceptor").start();
    }

    public void shutdown() {
        log.info("shutdown()");
        this.running = false;
    }

    public void awaitTermination() throws InterruptedException {
        try {
            finished.get();
        } catch (ExecutionException e) {
            throw new InterruptedException();
        }
    }

    private void acceptNewConnections() {
        running = true;
        while (running) {
            try {
                Socket socket = server.accept();
                log.info("{} connected", socket.getRemoteSocketAddress());
                socket.setSoTimeout(55000);
                startSession(socket);
            }
            catch(SocketTimeoutException ignored) {
            }
            catch (IOException e) {
                log.error("IOException in connection", e);
            }
        }
        log.info("no new connections will be accepted");
        finished.complete(null);
    }

    void startSession(final Socket socket) {
        new Thread(new Session(service, socket),
                "Receiver#"+socket.getRemoteSocketAddress()).start();
    }
}
