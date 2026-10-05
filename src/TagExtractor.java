import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * TagExtractor does the computational-linguistics part of the lab with no
 * GUI code: it loads the stop (noise) words into a Set, scans a text file
 * into a Map of word -> frequency while skipping stop words, and saves the
 * tags to a file.
 */
public class TagExtractor
{
    private final TreeSet<String> stopWords = new TreeSet<>();
    private final TreeMap<String, Integer> tags = new TreeMap<>();

    /**
     * Loads the stop words file (one word per line) into the stop word Set.
     *
     * @return the number of stop words loaded
     */
    public int loadStopWords(Path file) throws IOException
    {
        stopWords.clear();
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                String word = normalize(line);
                if (!word.isEmpty())
                {
                    stopWords.add(word);
                }
            }
        }
        return stopWords.size();
    }

    /**
     * Removes every non-letter character and forces the word to lowercase.
     */
    public static String normalize(String word)
    {
        return word.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }

    /** @return true if the word is a stop (noise) word */
    public boolean isStopWord(String word)
    {
        return stopWords.contains(word);
    }

    /**
     * Scans the text file and counts how often every non-stop word occurs.
     *
     * @return the map of tag -> frequency
     */
    public Map<String, Integer> extractTags(Path file) throws IOException
    {
        tags.clear();
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                addLine(line);
            }
        }
        return tags;
    }

    /**
     * Adds the words of one line of text to the tag map.
     */
    public void addLine(String line)
    {
        for (String raw : line.split("\\s+"))
        {
            String word = normalize(raw);
            if (word.isEmpty() || isStopWord(word))
            {
                continue;
            }
            tags.merge(word, 1, Integer::sum);
        }
    }

    public Map<String, Integer> getTags()
    {
        return tags;
    }

    public Set<String> getStopWords()
    {
        return stopWords;
    }

    /**
     * @return the tags sorted by frequency (highest first), ties alphabetical
     */
    public List<Map.Entry<String, Integer>> getTagsByFrequency()
    {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(tags.entrySet());
        list.sort((a, b) -> b.getValue().equals(a.getValue()) ? a.getKey().compareTo(b.getKey())
                : b.getValue() - a.getValue());
        return list;
    }

    /**
     * Formats the tags as "word    count" lines, most frequent first.
     */
    public String formatTags()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : getTagsByFrequency())
        {
            sb.append(String.format("%-20s %6d%n", e.getKey(), e.getValue()));
        }
        return sb.toString();
    }

    /**
     * Saves the extracted tags and frequencies to a text file.
     */
    public void saveTags(Path file, String sourceName) throws IOException
    {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8)))
        {
            out.println("Tags extracted from: " + sourceName);
            out.println("Distinct tags: " + tags.size());
            out.println();
            out.print(formatTags());
        }
    }
}
