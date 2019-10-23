import java.io.File;
import java.util.*;

/**
 * This program calculates page rank of websites based
 * on how many outgoing and incoming references it has.
 * It prompts the user to search for a word, and returns
 * the page rank of (up to 20) websites containing that word.
 *
 * @author Ryan Kirsch
 * @version February 2019
 */

public class PageRank {
    // map instance variable (Key is current website's URL)
    private static HashMap<String, Info> map;

    /**
     * Inner class Info contains map key's URL info:
     * contains a HashSet of the URL's text, a HashSet of the outgoing URLs,
     * the URL's page rank, and the URL itself (used later for displaying results)
     */
    public static class Info {
        // Info instance variables
        private HashSet<String> textSet;
        private HashSet<String> outSet;
        private double rank;
        private String url;

        /**
         * Info constructor - instantiates instance variables
         */
        public Info(HashSet<String> textSet, HashSet<String> outSet, double rank, String url) {
            this.textSet = textSet;
            this.outSet = outSet;
            this.rank = rank;
            this.url = url;
        }

        // Info getters and setters:
        public double getRank() { return rank; }

        public HashSet<String> getText() { return textSet; }

        public HashSet<String> getOut() { return outSet; }

        public String getURL() { return url; }

        public void setRank(double rank) { this.rank = rank; }
    }

    /**
     * PageRank Constructor - instantiates map data structure and
     * populates it by calling populate(), calculates page ranks
     * by calling calcRank()
     */
    public PageRank() {
        // instantiate map data structure
        map = new HashMap<>();
        // populate map data structure
        populate();
        // calculate page ranks
        calcRank();
    }

    /**
     * populate() - reads file, populates map data structure
     */
    public void populate() {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter file name: ");
        String fileName = keyboard.next();

        try {
            // Scan file
            Scanner file = new Scanner(new File(fileName));
            // skip first line (first "PAGE")
            file.nextLine();

            // end of file (eof) value = false
            boolean eof = false;
            // page rank = 0.0 (default rank assigned in calcRank())
            double rank = 0.0;

            // read until fine is empty
            while (file.hasNext()) {
                // get key URL
                String key = file.nextLine();
                // get URL text
                String text = file.nextLine();
                // change to lowercase, remove punctuation and
                // spit around whitespace (get individual words into arr[])
                String[] arr = text.toLowerCase().split("[.,!?:;'\"-]|\\s+");
                // create textSet HashSet and add arr elements asList
                HashSet<String> textSet = new HashSet<>(Arrays.asList(arr));

                // create outSet HashSet to add outgoing URLS to
                HashSet<String> outSet = new HashSet<>();

                if (file.hasNextLine()) {
                    // get next outgoing URL
                    String line = file.nextLine();
                    // read until eof or next "PAGE"
                    while (!eof && !(line.equals("PAGE"))) {
                        // add outgoing URLS to hashSet outSet
                        outSet.add(line);
                        // if file has nextLine, set line = nextLine
                        if (file.hasNextLine()) {
                            line = file.nextLine();
                        } else {
                            // set eof = true (breaks out of while loops)
                            eof = true;
                        }
                    }
                }
                // update map
                map.put(key, new Info(textSet, outSet, rank, key));
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * calcRank() - assigns default page ranks,
     * calculates page ranks (updates map key's value "rank")
     */
    public void calcRank() {
        // set default rank of every URL
        for (String key : map.keySet()) {
            Info info = map.get(key);
            info.setRank(1.0 / map.size());
        }

        // Calculate left side of equation (lse)
        double lse = (1 - 0.15) / map.size();

        // calculate and update page ranks 50 times to stabilize values
        for (int x = 0; x < 50; x++) {
            // foreach key in keySet:
            for (String key : map.keySet()) {
                // get specified key's value (Info object)
                Info info = map.get(key);

                // Create new HashMap to store URLS that link to key (value = number of outgoing links)
                HashMap<String, Integer> linkingURLS = new HashMap<>();

                // check if url (key) is linked to in any other urls
                for (String linked : map.keySet()) {
                    Info linkedInfo = map.get(linked);
                    // search linkedInfo.getOut() for mentions of key
                    if (linkedInfo.getOut().contains(key)) {
                        // add to list for calculations (key = URL, value = number of URLs key links to
                        linkingURLS.put(linked, linkedInfo.outSet.size());
                    }
                }

                // create new variable for right side of equation (rse)
                double rse = 0.0;
                // add each linkedURL's rank / number of linked to URLS
                for (String linked : linkingURLS.keySet()) {
                    Info linkedInfo = map.get(linked);
                    rse += (linkedInfo.getRank() / linkedInfo.getOut().size());
                }

                // calculate and set rank
                info.setRank(lse + (0.15 * rse));
            }
        }
    }

    /**
     * MAIN: gets user input for search term, prints number of URLS
     * term appears in, page rank of URLS in descending order
     */
    public static void main(String[] args) {
        PageRank pr = new PageRank();

        // Arraylist of Info objects
        ArrayList<Info> map2 = new ArrayList<>();
        // int count to keep track of printing only 20 results
        int count = 0;

        // get user input
        Scanner read = new Scanner(System.in);
        System.out.println("Enter your search term: ");
        String word = read.nextLine();
        word = word.toLowerCase();

        // while !equal("x")
        while (!word.equals("x")) {
            // search map outText for word
            for (String key : map.keySet()) {
                Info info = map.get(key);
                // if map text contains word
                if (info.getText().contains(word)) {
                    // add to map2
                    map2.add(new Info(info.getText(), info.getOut(), info.getRank(), info.getURL()));
                }
            }

            // print number of locations word appears in:
            System.out.println("The term \"" + word + "\" appears in " + map2.size() + " location(s).");
            // if map > 20, print message
            if (map2.size() > 20){
                System.out.println("Here are the 20 highest ranking results:");
            }
            System.out.println("Page Rank:              URL:");

            // sort map2 by rank in descending order (using a Lambda expression!)
            Collections.sort(map2, (s1, s2) -> Double.compare(s2.getRank(), s1.getRank()));

            // Print URLS containing word and their page ranking (up to 20)
            for (Info info : map2) {
                if (count < 20) {
                    System.out.println(info.getRank() + " " + info.getURL());
                }
                count++;
            }

            // clear map2 before searching for next word
            map2.clear();
            // reset count
            count = 0;

            System.out.println();
            // prompt user for more text or to exit
            System.out.println("Enter another search term (\"x\" to exit): ");
            word = read.nextLine();
            word = word.toLowerCase();
        }
    }
}