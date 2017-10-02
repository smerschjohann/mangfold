package it.tty0.mangfold.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static it.tty0.mangfold.internal.ScriptResponse.State.ERROR;

public class Session implements Runnable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Gson gson = new GsonBuilder().create();

    private final MangfoldService service;
    private final Socket socket;

    ExecutorService sender = Executors.newSingleThreadExecutor();
    private boolean stopped;

    public Session(MangfoldService service, Socket socket) {
        this.service = service;
        this.socket = socket;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        while(!stopped) {
            InputStream in = null;
            try {
                in = socket.getInputStream();
                DataInputStream inputStream = new DataInputStream(in);

                int len = inputStream.readInt();
                byte[] data = new byte[len];
                if (len > 0) {
                    inputStream.readFully(data);
                }

                consumeMessage(socket, data);
            } catch (IOException e) {
                log.error("io error", e);
                return;
            }
        }
    }

    private void consumeMessage(Socket socket, byte[] data) {
        ScriptRequest request = gson.fromJson(new String(data, Charset.forName("UTF-8")), ScriptRequest.class);

        if(request.getType() == ScriptRequest.Type.KEEP_ALIVE || request.getLanguage() == null) {
            sendResponse(socket, new ScriptResponse(request.getId(), ScriptResponse.State.OK, ""));
        } else {
            service.runScript(request).handle((response, throwable) -> {
                ScriptResponse resp = response;
                if (throwable != null) {
                    resp = new ScriptResponse(request.getId(), ERROR, throwable.getMessage());
                }
                sendResponse(socket, resp);
                return null;
            });
        }
    }

    private void sendResponse(Socket socket, ScriptResponse resp) {
        String answer = gson.toJson(resp);
        sender.submit(() -> {
            try {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                final byte[] dataOut = answer.getBytes("UTF-8");
                outputStream.writeInt(dataOut.length);
                outputStream.write(dataOut);
            } catch (IOException e) {
                log.error("", e);
            }
        });
    }

}
