import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI implements ActionListener, ComponentListener {
    private final JFrame frame = new JFrame();

    private final JPanel panel = new JPanel();

    public static final JTextPane message_text = new JTextPane();

    private final JScrollPane scrollPane = new JScrollPane();

    private final JTextField url_text = new JTextField(0);

    private final JCheckBox retry_text = new JCheckBox("Retry failed", true);

    private final JCheckBox overwrite_text = new JCheckBox("Overwrite existing", false);

    private final JButton download_button = new JButton("Download");

    private final JLabel url_label = new JLabel("URL:");

    public void initComponent()
    {
        frame.addComponentListener(this);
        frame.add(panel);
        frame.setSize(1024, 600);
        frame.setLocationRelativeTo(null);

        panel.setSize(frame.getSize());

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        message_text.setBounds(0, 0, frame.getWidth() - 22, frame.getHeight() - 200);
        message_text.setEditable(false);
        message_text.setCaretPosition(message_text.getDocument().getLength());
        message_text.setAutoscrolls(true);

        scrollPane.setBounds(0, 0, frame.getWidth() - 22, frame.getHeight() - 200);
        scrollPane.getViewport().add(message_text);
        scrollPane.setAutoscrolls(true);

        url_text.setBounds(50, message_text.getHeight() + 5, frame.getWidth() - 75, 25);
        url_text.setFont(new Font("Serif", Font.PLAIN, 16));

        url_label.setBounds(2, message_text.getHeight() + 5, 100, 20);

        download_button.setVisible(true);
        download_button.addActionListener(this);
        download_button.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
        download_button.setBounds(3, message_text.getHeight() + url_text.getHeight() + 10, frame.getWidth() - 28, 25);

        retry_text.setBounds(3, message_text.getHeight() + url_text.getHeight() + download_button.getHeight() + 10, 150, 25);
        overwrite_text.setBounds(150, message_text.getHeight() + url_text.getHeight() + download_button.getHeight() + 10, 300, 25);

        panel.add(scrollPane);
        panel.add(url_text);
        panel.add(download_button);
        panel.add(retry_text);
        panel.add(overwrite_text);
        panel.add(url_label);
        panel.setLayout(null);

        frame.setLayout(null);
        frame.setTitle("E-Hentai Downloader");
        frame.setResizable(false);
        frame.setVisible(true);
        frame.validate();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == download_button)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(null);

            new Thread(() -> {
                download_button.setEnabled(false);
                Core.downloadImageSet(url_text.getText(), fileChooser.getSelectedFile(), retry_text.isSelected(), overwrite_text.isSelected());
                download_button.setEnabled(true);
            }).start();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    public static String addConsoleMessage(String message)
    {
        message_text.setText(message_text.getText() + message + "\n");
        return message;
    }
}
