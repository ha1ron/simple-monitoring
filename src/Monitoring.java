import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Monitoring {

    private static ArrayDeque<UrlSets> states = new ArrayDeque<>();
    private static Map<String, Integer> dictionary = new LinkedHashMap<>();
    private static HashSet<String> indexingUrls = new HashSet<>();

    public static void main(String[] args) throws Exception {

        Monitoring monitoring = new Monitoring();

        monitoring.initUrls();

        int iterator = 0;
        while (states.peek() != null) {

            UrlSets urlSets = states.poll();

            if (indexingUrls.contains(urlSets.getUrl())) {
                continue;
            }
            indexingUrls.add(urlSets.getUrl());

            try {

                if (urlSets.getDeep() == 4) {
                    monitoring.saveState();
                    break;
                }

                Document doc = Jsoup.connect(urlSets.getUrl())
                        .timeout(1000 * 10).get();
                Elements elements = doc.select("a");
                for (Element element : elements) {

                    if (!element.absUrl("href").contains("#")) {
                        UrlSets urlSetsL = new UrlSets(element.absUrl("href"), urlSets.getDeep() + 1);
                        states.add(urlSetsL);
                    }
                }

                Scanner in = new Scanner(doc.body().text());
                int numberOfWords = 0;

                Map<String, Integer> listIndex = new LinkedHashMap<String, Integer>(dictionary);

                while (in.hasNextLine()) {
                    String line = in.nextLine().toLowerCase();
                    String[] lineWords = line.split("\\W+");
                    numberOfWords += lineWords.length;
                    for (String word : lineWords) {
                        if (listIndex.containsKey(word)) {
                            listIndex.put(word, listIndex.get(word) + 1);
                        }
                    }
                }

                listIndex = monitoring.sortIndex(listIndex);

                monitoring.calcFrequency((double) numberOfWords, listIndex, urlSets.getUrl(), doc.title());

                iterator++;
                if (iterator == 100) {
                    monitoring.saveState();
                    iterator = 0;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void initUrls() throws Exception {
        Scanner txt = new Scanner(new File("url_list.txt"));
        String url;
        while (txt.hasNextLine()) {
            url = txt.nextLine();
            UrlSets urlSets = new UrlSets(url, 1);
            states.add(urlSets);
        }

        txt = new Scanner(new File("dictionary.txt"));
        while (txt.hasNextLine()) {
            dictionary.put(txt.nextLine(), 0);
        }
        txt = new Scanner(new File("IdexingUrls.txt"));
        while (txt.hasNextLine()) {
            indexingUrls.add(txt.nextLine());
        }
    }

    public Map<String, Integer> sortIndex(Map<String, Integer> listIndex) {

        List<Map.Entry<String, Integer>> list = new ArrayList<>(listIndex.entrySet());
        list.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void calcFrequency(Double numberOfWords, Map<String, Integer> frequencyList, String url, String title) throws Exception {
        Double termFrequency;
        Set set = frequencyList.entrySet();

        Map<Double, String> termList = new TreeMap<>();
        FileWriter writer = new FileWriter("MyFile.txt", true);
        BufferedWriter bw = new BufferedWriter(writer);


        for (Object aSet : set) {
            Map.Entry entry = (Map.Entry) aSet;
            int term = (int) entry.getValue();
            termFrequency = ((double) term) / numberOfWords;
            if (termFrequency > 0.01) {
                //       System.out.println(termFrequency + " " + entry.getKey() + " " + url);
                termList.put(termFrequency, (String) entry.getKey());
            }
        }

        double sum = 0;
        for (Double l : termList.keySet()) {
            sum += l;
        }
        if (sum >= 0.03) {
            bw.write(new Date().toString());
            bw.write("\r\n");
            bw.write(title);
            bw.write("\r\n");
            bw.write(url);
            bw.write("\r\n");
            bw.write(termList.toString());
            bw.write("\r\n");
        }
        bw.flush();
    }
    public void saveState() throws IOException {
        BufferedWriter iu = new BufferedWriter(new FileWriter("url_list.txt", false));
        BufferedWriter niu = new BufferedWriter(new FileWriter("IdexingUrls.txt", true));
        Set<String> notIndexingUrl = new LinkedHashSet<>(states.stream().map(UrlSets::getUrl).limit(200)
                .filter(s -> !s.equals("")).collect(Collectors.toList()));

        for (String s : notIndexingUrl) {
            iu.write(s);
            iu.write("\r\n");
        }
        for (String s : indexingUrls) {
            niu.write("\r\n");
            niu.write(s);

        }
        iu.flush();
        niu.flush();

    }
}
