import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Create a glossary page(HTML formated) out of an input file. input file is
 * already saved in data folder : data/terms.txt .
 * 
 * @author Orfilia Qiu
 *
 */

public final class Glossary {

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        //asking user for input file
        out.println("Enter a file containing words with their meanings:");
        String inputFile = in.nextLine();
        SimpleReader fileIn = new SimpleReader1L(inputFile);
        //asking user for a folder to store html pages
        out.println("Enter a folder where the output file goes:");
        String folderName = in.nextLine();

        Map<String, String> mapFromFile = contentFromInput(fileIn);

        Set<String> setFromMap = keyToSet(mapFromFile);

        Comparator<String> order = new CompareString();

        Queue<String> sortedWords = sortAlphabetically(setFromMap, order);

        printToIndexHTML(folderName, sortedWords);

        printIndividualPage(folderName, mapFromFile, sortedWords);

        in.close();
        out.close();
        fileIn.close();

    }

    /**
     * Given an input file, containing word accompanied with its definition,
     * individual word&definition separated by a blank line. Store word as key,
     * definition as value into a given Map.
     *
     * @param input
     *            input stream
     *
     * @return Map with words as key and definition as value
     */

    public static Map<String, String> contentFromInput(SimpleReader input) {
        Map<String, String> inputFromFile = new Map1L<>();
        String everything = "";
        //save everything from input file to a single string
        while (!input.atEOS()) {
            everything = everything + input.nextLine() + "*";
        }

        /*
         * getting rid of the "*" at the end of string everything to solve index
         * out of bound issue
         */
        everything = everything.substring(0, everything.length() - 1);

        int index = 0;
        StringBuilder s = new StringBuilder(everything);
        //extracting word and definition out of string{everything} and store to Map
        while (s.length() > 0) {
            index = s.indexOf("**");
            if (index > 0) {
                int idxOfSpace = s.indexOf("*");
                String word = s.substring(0, idxOfSpace);
                String def = s.substring(idxOfSpace + 1, index);
                inputFromFile.add(word, def);
                s.delete(0, index + 2);
            } else {
                int idxOfSpace = s.indexOf("*");
                String word = s.substring(0, idxOfSpace);
                String def = s.substring(idxOfSpace + 1, s.length());
                s = s.delete(0, s.length());
                inputFromFile.add(word, def);
            }

        }

        return inputFromFile;
    }

    /**
     * Given a map with keys and definitions, store keys into a Set and return
     * the Set containing only the keys.
     *
     * @param map
     *            whose keys are to be saved to Set
     * @ensures map=this
     * @return a Set with keys(words) extracted from Map
     */
    public static Set<String> keyToSet(Map<String, String> map) {
        Set<String> keys = new Set1L<>();
        Map<String, String> sub = map.newInstance();

        for (Map.Pair<String, String> m : map) {

            String key = m.key();
            keys.add(key);
            sub.add(m.key(), m.value());
        }
        map.transferFrom(sub);
        return keys;
    }

    /**
     * Given a Set with keys, sort the Set alphabetically and store it in Queue,
     * return the Queue with keys sort alphabetically.
     *
     * @param words
     *            to be sorted alphabetically
     * @param order
     *            comparator for sorting
     * @ensures |words| = |this|
     * @return Queue containing words sorted in alphabetical order
     */
    public static Queue<String> sortAlphabetically(Set<String> words,
            Comparator<String> order) {
        Queue<String> sortedWords = new Queue1L<>();
        while (words.size() > 0) {

            sortedWords.enqueue(words.removeAny());
        }
        sortedWords.sort(order);
        return sortedWords;
    }

    /**
     * Compare {@code String}s in order.
     */
    private static class CompareString implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Given a folder name and a Queue containing individual words, Create an
     * index page for the glossary with individual word listed by bullet points
     * and store the page into given folder.
     *
     * @param folderName
     *            name of the folder where html page is saved
     * @param words
     *            to be listed in bullet points
     * @ensures words = #words
     */
    public static void printToIndexHTML(String folderName,
            Queue<String> words) {
        SimpleWriter outputTo = new SimpleWriter1L(folderName + "/index.html");
        Queue<String> subQ = words.newInstance();
        outputTo.println("<!DOCTYPE HTML>");
        outputTo.println("<html>");
        outputTo.println("<head><title>Glossary</title></head>");
        outputTo.println(
                "<body><h1 style='color:red; border-bottom: 1px solid black; ");
        outputTo.println("font-style:italic'>Glossary: Index Page</h1><ul>");
        while (words.length() > 0) {
            String subWord = words.dequeue();
            outputTo.println(
                    "<li style='font-family:courier; font-size:120%'><a href='"
                            + subWord + ".html'>" + subWord + "</a></li>");
            subQ.enqueue(subWord);
        }
        words.transferFrom(subQ);
        outputTo.println("</ul>");
        outputTo.println("</body></html>");
        outputTo.close();
    }

    /**
     * Given a folder name, Map containing words and definition, Queue with
     * words, create separate html page for individual word with its definition.
     * Store each html page into given folder.
     *
     * @param folderName
     *            name of the folder where html page is saved
     * @param map
     *            whose content to be displayed in html format
     * @param q
     *            words of the glossary
     * @ensures map=#map, q=#q
     */
    public static void printIndividualPage(String folderName,
            Map<String, String> map, Queue<String> q) {

        Map<String, String> subMap = map.newInstance();
        Queue<String> subQ = q.newInstance();
        String hyperlink = "";
        while (map.size() > 0) {
            Map.Pair<String, String> p = map.removeAny();

            String word = p.key();
            String definition = p.value();
            subMap.add(p.key(), p.value());
            int index = 0;
            while (q.length() > 0) {
                String que = q.dequeue();
                int lengthQ = que.length();
                int lengthOfDefinition = definition.length();
                //start checking if there is word(s) in the definition
                //if yes, put a hyper-link
                if (definition.indexOf(que) > 0) {
                    index = definition.indexOf(que);
                    //found the word in the definition, add hyper-link
                    hyperlink = definition.substring(0, index) + "<a href='"
                            + que + ".html'>"
                            + definition.substring(index, index + lengthQ)
                            + "</a>" + definition.substring(index + lengthQ,
                                    lengthOfDefinition);
                    //replace definition so the loop can start again
                    //for condition where definition contains multiple words
                    definition = hyperlink;
                }
                subQ.enqueue(que);
            }
            q.transferFrom(subQ);
            SimpleWriter outputTo = new SimpleWriter1L(
                    folderName + "/" + word + ".html");
            outputTo.println("<!DOCTYPE HTML><html><head><title>" + word
                    + "</title></head><body><h1 style='color:red; font-size:200%;");
            outputTo.println("font-style:italic '>" + word);
            outputTo.println("</h1><p style='padding-bottom: 30px; ");
            outputTo.println("border-bottom: 1px solid black'><b>");
            outputTo.println(
                    definition + "</b></p><footer style='padding-top: 30px'>");
            outputTo.println(
                    "<a href = 'index.html'>&larr; Back To Index Page</a>");
            outputTo.println("</footer></body></html>");
            outputTo.close();
        }
        map.transferFrom(subMap);
    }
}
