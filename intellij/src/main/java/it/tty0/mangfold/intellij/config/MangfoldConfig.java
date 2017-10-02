package it.tty0.mangfold.intellij.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name="MangfoldConfig", storages = {
        @Storage("MangfoldConfig.xml")
})
public class MangfoldConfig implements PersistentStateComponent<MangfoldConfig> {
    private String hostname;
    private int port;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Nullable
    @Override
    public MangfoldConfig getState() {
        return this;
    }

    @Override
    public void loadState(MangfoldConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static MangfoldConfig getInstance() {
        return ServiceManager.getService(MangfoldConfig.class);
    }
}
