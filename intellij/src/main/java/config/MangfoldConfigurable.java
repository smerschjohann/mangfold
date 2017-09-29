package config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MangfoldConfigurable implements SearchableConfigurable {
    private MangfoldConfigurableGUI gui;

    @NotNull
    @Override
    public String getId() {
        return "preferences.MangfoldConfigurable";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preferences.MangfoldConfigurable";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Mangfold Plugin";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        gui = new MangfoldConfigurableGUI();
        return gui.getRootPanel();
    }

    @Override
    public void disposeUIResources() {
        gui = null;
    }

    @Override
    public boolean isModified() {
        return gui.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        gui.apply();
    }
}
