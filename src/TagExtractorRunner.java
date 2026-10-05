import javax.swing.JFrame;

/** Starts the Tag Extractor GUI. */
public class TagExtractorRunner
{
    public static void main(String[] args)
    {
        TagExtractorFrame frame = new TagExtractorFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
