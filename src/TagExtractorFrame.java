import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * TagExtractorFrame is the Swing GUI for the Tag Extractor. The user picks a
 * text file and a stop word file with JFileChoosers, extracts the tags, sees
 * them with their frequencies in a JTextArea, and can save them to a file.
 */
public class TagExtractorFrame extends JFrame
{
    private final TagExtractor extractor = new TagExtractor();
    private File textFile;
    private File stopFile;

    private final JLabel textFileLbl = new JLabel("Text file: (none chosen)");
    private final JLabel stopFileLbl = new JLabel("Stop word file: (none chosen)");
    private final JLabel summaryLbl = new JLabel(" ");
    private final JTextArea tagTA = new JTextArea(25, 40);
    private final JButton saveBtn = new JButton("Save Tags...");

    private final JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));

    public TagExtractorFrame()
    {
        super("Tag Extractor");
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Tag / Keyword Extractor", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JPanel top = new JPanel(new GridLayout(4, 1, 4, 4));
        top.add(title);
        textFileLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        top.add(textFileLbl);
        top.add(stopFileLbl);
        top.add(summaryLbl);
        main.add(top, BorderLayout.NORTH);

        tagTA.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        tagTA.setEditable(false);
        JScrollPane scroller = new JScrollPane(tagTA);
        scroller.setBorder(BorderFactory.createTitledBorder("Tags and frequencies (most frequent first)"));
        main.add(scroller, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton textBtn = new JButton("Choose Text File...");
        JButton stopBtn = new JButton("Choose Stop Word File...");
        JButton extractBtn = new JButton("Extract Tags");
        JButton quitBtn = new JButton("Quit");
        textBtn.addActionListener(e -> chooseTextFile());
        stopBtn.addActionListener(e -> chooseStopFile());
        extractBtn.addActionListener(e -> extract());
        saveBtn.addActionListener(e -> save());
        saveBtn.setEnabled(false);
        quitBtn.addActionListener(e -> System.exit(0));
        buttons.add(textBtn);
        buttons.add(stopBtn);
        buttons.add(extractBtn);
        buttons.add(saveBtn);
        buttons.add(quitBtn);
        main.add(buttons, BorderLayout.SOUTH);

        add(main);
        setSize(820, 760);
        setLocationRelativeTo(null);
    }

    public void chooseTextFile()
    {
        chooser.setDialogTitle("Choose a text file to extract tags from");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            setTextFile(chooser.getSelectedFile());
        }
    }

    public void chooseStopFile()
    {
        chooser.setDialogTitle("Choose the stop (noise) word file");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            setStopFile(chooser.getSelectedFile());
        }
    }

    public void setTextFile(File f)
    {
        textFile = f;
        textFileLbl.setText("Text file: " + f.getName());
    }

    public void setStopFile(File f)
    {
        try
        {
            int n = extractor.loadStopWords(f.toPath());
            stopFile = f;
            stopFileLbl.setText("Stop word file: " + f.getName() + "  (" + n + " stop words)");
        }
        catch (IOException ex)
        {
            error("Could not read the stop word file: " + ex.getMessage());
        }
    }

    public void extract()
    {
        if (textFile == null || stopFile == null)
        {
            error("Please choose both a text file and a stop word file first.");
            return;
        }
        try
        {
            Map<String, Integer> tags = extractor.extractTags(textFile.toPath());
            int total = tags.values().stream().mapToInt(Integer::intValue).sum();
            summaryLbl.setText("Extracting tags from " + textFile.getName() + ":  " + tags.size()
                    + " distinct tags, " + total + " tag occurrences");
            tagTA.setText(extractor.formatTags());
            tagTA.setCaretPosition(0);
            saveBtn.setEnabled(true);
        }
        catch (IOException ex)
        {
            error("Could not read the text file: " + ex.getMessage());
        }
    }

    public void save()
    {
        chooser.setDialogTitle("Save the tags");
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "Tags_" + textFile.getName()));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            saveTo(chooser.getSelectedFile());
        }
    }

    public void saveTo(File f)
    {
        try
        {
            extractor.saveTags(f.toPath(), textFile.getName());
            JOptionPane.showMessageDialog(this, "Saved " + extractor.getTags().size() + " tags to " + f.getName(),
                    "Tags Saved", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IOException ex)
        {
            error("Could not save the tags: " + ex.getMessage());
        }
    }

    private void error(String msg)
    {
        JOptionPane.showMessageDialog(this, msg, "Tag Extractor", JOptionPane.ERROR_MESSAGE);
    }
}
