package intellijcoder.arena;

import intellijcoder.main.IntelliJCoderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;


public class ArenaFileParser {

    public ArenaAppletInfo parse(InputStream file) throws IntelliJCoderException {
        try {
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            ArenaAppletInfo appletInfo = new ArenaAppletInfo();
            parseJars(d, appletInfo);
            parseMainClass(d, appletInfo);
            parseArguments(d, appletInfo);
            return appletInfo;
        } catch (Exception e) {
            throw new IntelliJCoderException("Failed to parse TopCoder jnlp file ", e);
        }
    }

    public void parseJars(Document d, ArenaAppletInfo appletInfo) {
        NodeList jarNodes = d.getElementsByTagName("jar");
        for (int i = 0; i < jarNodes.getLength(); i++) {
            Node item = jarNodes.item(i);
            appletInfo.addClassPathItem(item.getAttributes().getNamedItem("href").getTextContent());
        }
    }

    public void parseMainClass(Document d, ArenaAppletInfo appletInfo) {
        Node appNode = d.getElementsByTagName("application-desc").item(0);
        appletInfo.setMainClass(appNode.getAttributes().getNamedItem("main-class").getTextContent());
    }

    public void parseArguments(Document d, ArenaAppletInfo appletInfo) {
        NodeList argumentNodes = d.getElementsByTagName("argument");
        for (int i = 0; i < argumentNodes.getLength(); i++) {
            Node item = argumentNodes.item(i);
            appletInfo.addArgument(item.getTextContent());
        }
    }
}
