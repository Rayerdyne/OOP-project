package be.uliege.straet.oop.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to handle properly the fact that we can specify attributes either in
 * the tag or in the {@code Writer.VALUE_NODE_TAG} children attributes.
 */
public class ValueMapper {
    private HashMap<String, Node> hm;

    private ValueMapper() {
        hm = new HashMap<String, Node>();
    }

    /**
     * Constructor.
     * @param n     The {@code Node} we fetch its attributes
     */
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

    /**
     * Adds all the entries of a {@code NamedNodeMap}.
     * @param nnm   The{@code NamedNodeMap}
     */
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

    /**
     * @return      The value associated to the key {@code name}.
     */
    public Node getNamedItem(String name) {
        return hm.get(name);
    }

    /**
     * @return      A {@code Set<Entry<String, Node>>} containing all the
     *              entries in this {@code ValueMapper}
     */
    public Set<Entry<String, Node>> entries() {
        return hm.entrySet();
    }

    /**
     * @return      A {@code Collection<Node>} containing all the values stored
     *              in this {@code ValueMapper}
     */
    public Collection<Node> values() {
        return hm.values();
    }

    /**
     * @return      The number of entries in this{@code ValueMapper}
     */
    public int getLength() {
        return hm.size();
    }
}