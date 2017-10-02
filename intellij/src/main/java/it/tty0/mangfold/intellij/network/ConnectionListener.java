package it.tty0.mangfold.intellij.network;

public interface ConnectionListener {
    void connectionEstablished();
    void connectionLost();
}
