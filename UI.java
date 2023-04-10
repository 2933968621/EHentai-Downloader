import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class UI implements ActionListener, ComponentListener {
    private final JFrame frame = new JFrame();

    private final JPanel panel = new JPanel();

    public static final JTextPane message_text = new JTextPane();

    private final JScrollPane scrollPane = new JScrollPane();

    private final JTextField url_text = new JTextField(0);

    private final JCheckBox retry_checkbox = new JCheckBox("Retry failed", true);

    private final JCheckBox overwrite_checkbox = new JCheckBox("Overwrite existing", false);

    public static final JCheckBox original_checkbox = new JCheckBox("Original", false);

    private final JButton download_button = new JButton("Download");

    private final JButton stop_button = new JButton("Stop");

    private final JLabel url_label = new JLabel("URL:");
    public static final JProgressBar progressBar = new JProgressBar();

    private Thread curThread = null;

    private File backupSavePath = null;

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
       // message_text.setAutoscrolls(true);

        scrollPane.setBounds(0, 0, frame.getWidth() - 22, frame.getHeight() - 200);
        scrollPane.getViewport().add(message_text);
        //scrollPane.setAutoscrolls(true);

        url_text.setBounds(50, message_text.getHeight() + 5, frame.getWidth() - 75, 25);
        url_text.setFont(new Font("Serif", Font.PLAIN, 16));

        url_label.setBounds(2, message_text.getHeight() + 5, 100, 20);

        download_button.setVisible(true);
        download_button.addActionListener(this);
        download_button.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
        download_button.setBounds(3, message_text.getHeight() + url_text.getHeight() + 10, frame.getWidth() - 28, 25);

        stop_button.setVisible(true);
        stop_button.setEnabled(!download_button.isEnabled());
        stop_button.addActionListener(this);
        stop_button.setBounds(3, message_text.getHeight() + url_text.getHeight() + download_button.getHeight() + 15, frame.getWidth() - 28, 25);

        progressBar.setVisible(false);
        progressBar.setBounds(3, message_text.getHeight() + url_text.getHeight() + 10, frame.getWidth() - 28, 25);
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);

        retry_checkbox.setBounds(3, message_text.getHeight() + url_text.getHeight() + download_button.getHeight() + stop_button.getHeight() + 20, 110, 25);
        overwrite_checkbox.setBounds(120, message_text.getHeight() + url_text.getHeight() + download_button.getHeight() + stop_button.getHeight() + 20, 150, 25);
        original_checkbox.setBounds(270, message_text.getHeight() + url_text.getHeight() + download_button.getHeight() + stop_button.getHeight() + 20, 150, 25);

        panel.add(scrollPane);
        panel.add(url_text);
        panel.add(download_button);
        panel.add(stop_button);
        panel.add(retry_checkbox);
        panel.add(overwrite_checkbox);
        panel.add(original_checkbox);
        panel.add(url_label);
        panel.add(progressBar);
        panel.setLayout(null);

        frame.setLayout(null);
        frame.setTitle("E-Hentai Downloader");
        frame.setResizable(false);
        frame.setVisible(true);
        frame.validate();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new Thread(this::thread).start();
    }

    public void thread()
    {
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        if (scrollBar != null) {
            scrollBar.setValue(scrollBar.getMaximum());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == download_button && !url_text.getText().isEmpty())
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (backupSavePath != null)
                fileChooser.setCurrentDirectory(backupSavePath);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                this.curThread = new Thread(() -> {
                    download_button.setVisible(false);
                    stop_button.setEnabled(true);
                    progressBar.setValue(0);
                    progressBar.setVisible(true);
                    Core.downloadImageSet(url_text.getText(), backupSavePath = fileChooser.getSelectedFile(), retry_checkbox.isSelected(), overwrite_checkbox.isSelected(), original_checkbox.isSelected());
                    download_button.setVisible(true);
                    stop_button.setEnabled(false);
                    progressBar.setVisible(false);
                    this.curThread = null;
                });

                this.curThread.start();
            }
        }

        if (e.getSource() == stop_button && this.curThread != null)
        {
            this.curThread.interrupt();
            this.curThread = null;
            download_button.setVisible(true);
            stop_button.setEnabled(false);
            progressBar.setVisible(false);
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
