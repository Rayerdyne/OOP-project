package be.uliege.straet.oop.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ValueMapper {
    private HashMap<String, Node> hm;

    private ValueMapper() {
        hm = new HashMap<String, Node>();
    }

    public ValueMapper(Node n) {
        this();
        addAllNNM(n.getAttributes());
        NodeList children = n.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(Writer.VALUE_NODE_TAG)) {
                addAllNNM(child.getAttributes());
            }
        }
    }

    public void addAllNNM(NamedNodeMap nnm) {
        if (nnm == null)
            return;
        for (int i = 0; i < nnm.getLength(); i++) {
            hm.put(nnm.item(i).getNodeName(), nnm.item(i));
        }
    }

    /**
     * <p>Adds a list of Node</p>
     * <p>Note: if any attributes matches, null will be mapped to the
     * corresponding name.</p>
     * @param nl            The NodeList
     */
    public void addAllNL(NodeList nl) {
        if (nl == null)
            return;
        for (int i = 0; i < nl.getLength(); i++) {
            NamedNodeMap nnm = nl.item(i).getAttributes();
            addAllNNM(nnm);
        }
    }

    public Node getNamedItem(String name) {
        return hm.get(name);
    }

    public Set<Entry<String, Node>> entries() {
        return hm.entrySet();
    }

    public Collection<Node> values() {
        return hm.values();
    }

    public int getLength() {
        return hm.size();
    }
}