package it.tty0.mangfold.intellij;

import it.tty0.mangfold.intellij.config.MangfoldConfig;
import it.tty0.mangfold.intellij.network.MangfoldClient;

import java.io.IOException;

public class Connection {
    private MangfoldClient client;

    private static class StaticHolder {
        static final Connection INSTANCE = new Connection();
    }

    public static Connection getSingleton() {
        return StaticHolder.INSTANCE;
    }

    public Connection() {
        init();
    }

    public void init() {
        try {
            if(client != null) {
                client.disconnect();
            }
            client = new MangfoldClient(MangfoldConfig.getInstance().getHostname(), MangfoldConfig.getInstance().getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MangfoldClient getClient() {
        return client;
    }
}
