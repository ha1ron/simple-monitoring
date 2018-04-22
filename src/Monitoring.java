import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class Monitoring {

    private ArrayDeque<UrlSets> states = new ArrayDeque<>();
    private Map<String, Integer> dictionary = new LinkedHashMap<>();
    private HashSet<String> indexingUrls = new HashSet<>();



    public static void main(String[] args) throws Exception {

        Monitoring monitoring = new Monitoring();

        monitoring.initUrls();

        while (monitoring.states.size() > 0) {

            int blockSize = monitoring.states.size();

            while (monitoring.states.peek() != null) {

                UrlSets urlSets = monitoring.states.poll();

                if (monitoring.indexingUrls.contains(urlSets.getUrl())) {
                    continue;
                }

                monitoring.indexingUrls.add(urlSets.getUrl());

                try {

                    if (urlSets.getDeep() == 4) {
                        monitoring.saveState();
                        break;
                    }

                    Document doc = Jsoup.connect(urlSets.getUrl())
                            .timeout(1000 * 10).get();
                    Elements elements = doc.select("a");
                    for (Element element : elements) {

                        if (((element.absUrl("href").contains("#")) ||
                                (element.absUrl("href").contains("action=edit"))) ||
                                (monitoring.indexingUrls.contains(element.absUrl("href")))) {
                            continue;
                        }
                        UrlSets urlSetsL = new UrlSets(element.absUrl("href"), urlSets.getDeep() + 1);

//                        if (urlSetsL.getDeep() < 3) { //для оптимизации
                        String query = "INSERT INTO URLSETS \n" +
                                " VALUES ('" + urlSetsL.getUrl() + "', " + urlSetsL.getDeep() + ");";

                        BaseConnection bc = new BaseConnection();
                        bc.dbDataQuery(query);
                        bc.closeConnection();
//                        }
                    }

                    Scanner in = new Scanner(doc.body().text());
                    int numberOfWords = 0;

                    Map<String, Integer> listIndex = new LinkedHashMap<>();
                    for (Map.Entry<String, Integer> it : monitoring.dictionary.entrySet()) {
                        String[] line = null;
                        line = it.getKey().split(" ");
                        for (String s : line) {
                            listIndex.put(s, 0);
                        }
                    }

                    while (in.hasNextLine()) {
                        String line = in.nextLine().toLowerCase();
                        String[] lineWords = null;
                        lineWords = line.split("[^а-я]");
                        numberOfWords += lineWords.length;
                        for (String word : lineWords) {
                            // добавить тут обработку стоп слов
                            if (listIndex.containsKey(word)) {
                                listIndex.put(word, listIndex.get(word) + 1);
                            }
                        }
                    }

                    monitoring.calcFrequency((double) numberOfWords, listIndex, urlSets.getUrl(), doc.title());

                    listIndex = null;
                } catch (Exception e) {
                    System.out.println(urlSets.getUrl() + " " + e.getMessage());
                }

            }

            if (monitoring.states.size() == 0) {
                BaseConnection bc = new BaseConnection();
                bc.dbDataQuery("DELETE TOP " + blockSize + " FROM URLSETS"); //удаляем предыдущую пачку
                java.sql.ResultSet rSet = bc.dbData("SELECT TOP 10000 * FROM URLSETS"); // грузим новую

                while (rSet.next()) {
                    UrlSets urlSets = new UrlSets(rSet.getString(1), rSet.getInt(2));
                    monitoring.states.add(urlSets);
                }
                bc.closeConnection();
            }
        }
    }

    public void initUrls() throws Exception {
        Scanner txt = new Scanner(new File("dictionary.txt"), "Cp1251");
        while (txt.hasNextLine()) {
            dictionary.put(txt.nextLine(), 0);
        }

//        txt = new Scanner(new File("url_list.txt"));
//        String url;
//        while (txt.hasNextLine()) {
//            url = txt.nextLine();
//            UrlSets urlSets = new UrlSets(url, 1);
//            states.add(urlSets);
//        }

        BaseConnection bc = new BaseConnection();
        java.sql.ResultSet rSet = bc.dbData("SELECT TOP 10000 * FROM URLSETS");

        while (rSet.next()) {
            UrlSets urlSets = new UrlSets(rSet.getString(1), rSet.getInt(2));
            states.add(urlSets);
        }

        if (states.size() == 0) {
            StartingSet stS = new StartingSet();
            stS.makeStartingSet(dictionary.keySet());
            rSet = bc.dbData("SELECT TOP 10000 * FROM URLSETS");

            while (rSet.next()) {
                UrlSets urlSets = new UrlSets(rSet.getString(1), rSet.getInt(2));
                states.add(urlSets);
            }
        }

        bc.closeConnection();

        txt = new Scanner(new File("IdexingUrls.txt"));
        while (txt.hasNextLine()) {
            indexingUrls.add(txt.nextLine());
        }
        System.out.println("Инициализация завершена");
    }

    public void calcFrequency(Double numberOfWords, Map<String, Integer> frequencyList, String url, String title) throws Exception {
        Double termFrequency;

        Map<Double, String> termList = null;
        termList = new TreeMap<>(new TreeComparator());
        FileWriter writer = new FileWriter("MyFile.txt", true);
        BufferedWriter bw = new BufferedWriter(writer);

        // frequencyList - индекс документа по ключевым словам
        // termList - подсчитанная частота для каждого терма

        for (Object aSet : frequencyList.entrySet()) {
            Map.Entry entry = (Map.Entry) aSet;
            int term = (int) entry.getValue();
            termFrequency = ((double) term) / numberOfWords;
            //         if (termFrequency > 0.01) {
            termList.put(termFrequency, (String) entry.getKey());
            //         }
        }

        Map<Double, String> resultList = new TreeMap<>(new TreeComparator());

        Map<String, Double> inverseTermList = new LinkedHashMap<>();

        for (Map.Entry<Double, String> entry : termList.entrySet()) {
            inverseTermList.put(entry.getValue(), entry.getKey());
        }

        for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
            String[] line = line = entry.getKey().split(" ");
            double summ = 0;
            boolean acessTerm = true;
            for (String s : line) {
                double k;
                try {
                    k = inverseTermList.get(s);
                } catch (NullPointerException e) {
                    k = 0;
                }
                if (k == 0) {
                    acessTerm = false;
                    break;
                }
                summ += k;
            }
            line = null;
            if (acessTerm) {
                resultList.put(summ, entry.getKey());
            }
        }

        double chek;
        try {
            chek = resultList.entrySet().iterator().next().getKey();
        } catch (Exception e) {
            chek = 0;
        }

        if (chek > 0) {
            XMLWriter xmlWriter = new XMLWriter();
            resultList.entrySet().removeIf(e -> e.getKey() == 0);
            xmlWriter.write(url, title, resultList);

            bw.write(new Date().toString());
            bw.write("\r\n");
            bw.write(title);
            bw.write("\r\n");
            bw.write(url);
            bw.write("\r\n");
            bw.write(resultList.entrySet().stream().limit(30).collect(Collectors.toList()).toString());
            bw.write("\r\n");

            xmlWriter = null;
        }
        bw.flush();
        bw.close();

        termList = null;
        frequencyList = null;
        inverseTermList = null;
        resultList = null;
        writer.close();
        bw = null;
    }

    public void saveState() throws IOException {

        FileWriter file = new FileWriter("url_list.txt", false);
        BufferedWriter iu = new BufferedWriter(file);
        file = null;
        file = new FileWriter("IdexingUrls.txt", true);
        BufferedWriter niu = new BufferedWriter(file);
        Set<String> notIndexingUrl = new LinkedHashSet<>(states.stream().map(UrlSets::getUrl).limit(200)
                .filter(s -> !s.equals("")).collect(Collectors.toList()));

        for (String s : notIndexingUrl) {
            iu.write(s);
            iu.write("\r\n");
        }
        notIndexingUrl = null;
        for (String s : indexingUrls) {
            niu.write("\r\n");
            niu.write(s);

        }

        iu.flush();
        niu.flush();

        iu.close();
        iu = null;
        niu.close();
        niu = null;

    }
}
