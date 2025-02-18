package kz.alfabank;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Common {

    private static final Common instance = new Common();

    public static Common getInstance() {
        return instance;
    }

    private Common() {
    }

    public String getNodeValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }
}
