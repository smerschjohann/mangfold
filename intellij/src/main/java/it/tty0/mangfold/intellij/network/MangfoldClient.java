package it.tty0.mangfold.intellij.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.tty0.mangfold.ScriptResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class MangfoldClient {
    private static final long LIFE_MESSAGE_INTERVAL = 50;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String server;
    private final int port;

    ExecutorService sender = Executors.newSingleThreadExecutor();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int CONNECT_TIMEOUT = 5 * 1000;
    private ArrayList<ConnectionListener> listeners = new ArrayList<>();
    private Socket clientSocket;
    private ScheduledFuture<?> keepAliveTimer;
    private boolean shutdown = false;

    private int incrementalCounter = 0;
    private Gson gson = new GsonBuilder().create();

    class QueueEntry {
        ScriptRequest request;
        CompletableFuture<ScriptResponse> future;
    }
    private LinkedBlockingQueue<QueueEntry> queue = new LinkedBlockingQueue<>();
    private HashMap<Integer, CompletableFuture<ScriptResponse>> awaitReceive = new HashMap<>();

    public MangfoldClient(String server, int port) throws IOException {
        this.server = server;
        this.port = port;
    }

    public CompletableFuture<ScriptResponse> clearEngine(String language) {
        return sendMessage(new ScriptRequest(ScriptRequest.Type.CLEAR, 0, language, null));
    }

    public CompletableFuture<ScriptResponse> runRemote(String language, String code) {
        return sendMessage(new ScriptRequest(ScriptRequest.Type.RUN, 0, language, code));
    }

    private CompletableFuture<ScriptResponse> sendMessage(ScriptRequest scriptRequest) {
        if(connect()) {
            QueueEntry entry = new QueueEntry();
            entry.request = new ScriptRequest(scriptRequest.getType(), incrementalCounter++, scriptRequest.getLanguage(), scriptRequest.getCode());
            entry.future = new CompletableFuture<>();
            queue.add(entry);
            return entry.future;
        } else {
            return CompletableFuture.completedFuture(
                    new ScriptResponse(0, ScriptResponse.State.ERROR, String.format("Could not connect to %s:%s",
                            server, port)));
        }
    }

    private boolean connect() {
        shutdown = false;
        if(clientSocket == null) {
            clientSocket = new Socket();
            try {
                clientSocket.connect(new InetSocketAddress(server, port), CONNECT_TIMEOUT);
                connectionEstablished();
            } catch (Exception e) {
                log.info("could not connect to {}:{}", server, port);
                clientSocket = null;
            }
        }
        return clientSocket != null;
    }

    public void disconnect() {
        if(keepAliveTimer != null) {
            keepAliveTimer.cancel(true);
            keepAliveTimer = null;
        }

        if(clientSocket != null) {
            disconnected();
            shutdown = true;
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("", e);
            }
            clientSocket = null;
        }
    }

    private void connectionEstablished() {
        keepAliveTimer = scheduler.scheduleAtFixedRate(() -> {
            sender.submit(() -> {
                if(queue.isEmpty() && awaitReceive.isEmpty()) {
                    disconnect();
                } else {
                    sendMessage(new ScriptRequest(0));
                }
            });
        }, LIFE_MESSAGE_INTERVAL, LIFE_MESSAGE_INTERVAL, TimeUnit.SECONDS);

        listeners.forEach(listener -> {
            try {
                listener.connectionEstablished();
            } catch(Exception warn) {
                log.warn("could not notify", warn);
            }
        });

        new Thread(this::receive, "ReceiveQueue").start();
        new Thread(this::sendQueue, "SendQueue").start();
    }

    private void sendQueue() {
        try {
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

            while (clientSocket.isConnected() && !shutdown) {
                QueueEntry entry = queue.poll(2, TimeUnit.SECONDS);
                if (entry != null) {
                    String request = gson.toJson(entry.request);
                    final byte[] utf8Bytes = request.getBytes("UTF-8");
                    outputStream.writeInt(utf8Bytes.length);
                    outputStream.write(utf8Bytes);
                    awaitReceive.put(entry.request.getId(), entry.future);
                }
            }
        } catch(Exception ex) {
            log.info("disconnected");
            clearState();
        }
    }

    private void receive() {
        try {
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

            while (clientSocket.isConnected() && !shutdown) {
                int len = inputStream.readInt();
                byte[] data = new byte[len];
                if (len > 0) {
                    inputStream.readFully(data);
                }

                consumeMessage(data);
            }
        } catch(Exception ex) {
            log.info("possibly disconnected", ex);
            clearState();
        }
    }

    private void disconnected() {
        listeners.forEach(listener -> {
            try {
                listener.connectionLost();
            } catch(Exception ex) {
                log.error("failed notify connectionLost", ex);
            }
        });

        if(keepAliveTimer != null) {
            keepAliveTimer.cancel(true);
        }
    }

    private void consumeMessage(byte[] data) {
        ScriptResponse response = gson.fromJson(new String(data, Charset.forName("UTF-8")), ScriptResponse.class);

        int id = response.getId();
        CompletableFuture<ScriptResponse> future = awaitReceive.get(id);
        if(future != null) {
            future.complete(response);
        } else {
            log.error("unexpected message: {}", response);
        }
    }

    private void clearState() {
        queue.clear();
        disconnect();
    }

}
