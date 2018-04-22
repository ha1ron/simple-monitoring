import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Set;

/**
 * Created by User on 21.04.2018.
 */
public class StartingSet {

    public void makeStartingSet(Set<String> termList)  {
        Document doc = null;
        BaseConnection bc = new BaseConnection();

        for (String s : termList) {
            String qTerm = null;

            try {
                qTerm = URLEncoder.encode(s, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int iterator = 1;
            while (iterator < 801) {
                try {
//                if (iterator == 30) {
//                    TimeUnit.SECONDS.sleep(5);
//                }

                    doc = Jsoup.connect("https://www.bing.com/search?q=" + qTerm + "&first=" + iterator)
                            .timeout(1000 * 10).get();
//                doc = Jsoup.connect("https://yandex.ru/search/?clid=2224314&text="+qTerm+"&lr=63&p="+iterator)
//                        .timeout(1000 * 10).get();
//                doc = Jsoup.connect("https://www.google.ru/search?q=%D0%B2%D0%BE%D0%B7%D0%BE%D0%B1%D0%BD%D0%BE%D0%B2%D0%BB%D1%8F%D0%B5%D0%BC%D1%8B%D0%B5+%D0%B8%D1%81%D1%82%D0%BE%D1%87%D0%BD%D0%B8%D0%BA%D0%B8+%D1%8D%D0%BD%D0%B5%D1%80%D0%B3%D0%B8%D0%B8&start="+iterator)
//                        .timeout(1000 * 10).get();

                    Elements elements = doc.select("a");

                    for (Element element : elements) {

                        if (element.absUrl("href").contains("yandex") ||
                                element.absUrl("href").contains("google") ||
                                element.absUrl("href").contains("bing") ||
                                element.absUrl("href").contains("mail") ||
                                element.absUrl("href").contains("microsoft") ||
                                element.absUrl("href").length() == 0 ||
                                element.absUrl("href").contains("#") ||
                                element.absUrl("href").contains("action=edit")) {
                            continue;
                        }

                        String query = "INSERT INTO URLSETS \n" +
                                " VALUES ('" + element.absUrl("href") + "', " + 1 + ");";

                        bc.dbDataQuery(query);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                iterator += 10;
            }
        }

        try {
            bc.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
