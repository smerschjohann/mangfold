package config;

import com.google.common.base.Strings;

import javax.swing.*;

public class MangfoldConfigurableGUI {
    private final MangfoldConfig config;
    private JTextField textHostname;
    private JTextField textPort;
    private JPanel rootPanel;

    public MangfoldConfigurableGUI() {
        config = MangfoldConfig.getInstance();
        String hostname = config.getHostname();
        int port = config.getPort();
        if(Strings.isNullOrEmpty(hostname)) {
            hostname = "localhost";
        }
        if(port == 0) {
            port = 14237;
        }
        textHostname.setText(hostname);
        textPort.setText(Integer.toString(port));
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified() {
        return Integer.parseInt(textPort.getText()) != config.getPort()
                || !textHostname.getText().equals(config.getHostname());
    }

    public void apply() {
        config.setHostname(textHostname.getText());
        config.setPort(Integer.parseInt(textPort.getText()));
    }
}
