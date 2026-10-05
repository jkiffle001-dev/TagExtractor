import org.junit.Before;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class TagExtractorTest
{
    private TagExtractor tx;
    private Path stop;

    @Before
    public void setUp() throws Exception
    {
        tx = new TagExtractor();
        stop = Files.createTempFile("stop", ".txt");
        Files.writeString(stop, "a\nthe\nand\nis\n");
        tx.loadStopWords(stop);
    }

    @Test
    public void normalizeRemovesNonLettersAndLowercases()
    {
        assertEquals("monster", TagExtractor.normalize("Monster!"));
        assertEquals("ill", TagExtractor.normalize("I'll"));
        assertEquals("", TagExtractor.normalize("1818,"));
    }

    @Test
    public void stopWordsAreLoadedIntoSet()
    {
        assertEquals(4, tx.getStopWords().size());
        assertTrue(tx.isStopWord("the"));
        assertFalse(tx.isStopWord("monster"));
    }

    @Test
    public void stopWordsAreNotCounted()
    {
        tx.addLine("The monster and the Monster is a MONSTER.");
        Map<String, Integer> tags = tx.getTags();
        assertEquals(1, tags.size());
        assertEquals(Integer.valueOf(3), tags.get("monster"));
        assertNull(tags.get("the"));
    }

    @Test
    public void extractFromFileAndSortByFrequency() throws Exception
    {
        Path text = Files.createTempFile("text", ".txt");
        Files.writeString(text, "Victor fled. Victor wept!\nThe creature followed Victor.\n");
        tx.extractTags(text);
        List<Map.Entry<String, Integer>> list = tx.getTagsByFrequency();
        assertEquals("victor", list.get(0).getKey());
        assertEquals(3, (int) list.get(0).getValue());
        assertEquals(5, list.size());
    }

    @Test
    public void saveWritesTagsFile() throws Exception
    {
        tx.addLine("castle castle river");
        Path out = Files.createTempFile("tags", ".txt");
        tx.saveTags(out, "sample.txt");
        String s = Files.readString(out);
        assertTrue(s.contains("sample.txt"));
        assertTrue(s.contains("castle"));
        assertTrue(s.indexOf("castle") < s.indexOf("river"));
    }
}
