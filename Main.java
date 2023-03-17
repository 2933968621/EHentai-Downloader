import javax.swing.*;

public class Main {
    public static void main(String[] args)
    {
        System.setProperty("java.net.useSystemProxies", "true");
        SwingUtilities.invokeLater(() -> new UI().initComponent());
    }
}
