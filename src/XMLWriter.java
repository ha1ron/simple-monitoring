import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by User on 22.04.2018.
 */
public class XMLWriter {

    public void write(String url, String title, Map<Double, String> termList) {
        String filePath = "out.xml";
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Element employee = doc.getDocumentElement();
            employee.appendChild(addUrlSet(doc, new Date(), url, title, termList));

            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.setOutputProperty(OutputKeys.ENCODING, "cp1251");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private Node addUrlSet(Document doc, Date date, String url, String title, Map<Double, String> termList) {
        Element urlSet = doc.createElement("url_set");

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        urlSet.appendChild(getEmployeeElements(doc, "data", dateFormat.format(date)));

        urlSet.appendChild(getEmployeeElements(doc, "title", title));

        urlSet.appendChild(getEmployeeElements(doc, "url", url));

        Element termL = doc.createElement("term_list");

        for (Map.Entry<Double, String> entry : termList.entrySet()) {
            Element dependence = doc.createElement("dependence");
            dependence.appendChild(getEmployeeElements(doc, "term", entry.getValue()));
            termL.appendChild(dependence);
            dependence.appendChild(getEmployeeElements(doc, "frequency", entry.getKey().toString()));
            termL.appendChild(dependence);
        }

        urlSet.appendChild(termL);

        return urlSet;
    }

    private static Node getEmployeeElements(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

}
