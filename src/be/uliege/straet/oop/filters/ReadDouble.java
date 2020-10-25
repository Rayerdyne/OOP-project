package be.uliege.straet.oop.filters;

/** 
 * <p>This class just holds reference to a WriteDouble, but only with a value()
 * method, so that we only can read it.</p>
 * 
 * <p>This mechanism, combined with the WriteDouble class, aims to mimic a 
 * owner - reader system, we can figure out that some objet holds the value and
 * can set it, and the other can only read it.</p>
 *
 * <p>Of course, dereferencing two times at each access to the variable may not
 * be the best solution in terms of speed, we could have used only WriteDouble
 * objects, but this way prevent from setting value where we should not.</p>
 * 
 * <p>Added: reference to the object holding the associated ReadDouble, and the
 * correponding output index, for being able to write in xml file.</p>
 */

public class ReadDouble {
    private boolean isConnectedToInput = false;
    private WriteDouble ref;
    private Block source = null;
    private int i;

    /**
     * Constructs a object referencing a value.
     * @param ref       A reference to the {@code WriteDouble} to 'follow'
     */
    public ReadDouble(WriteDouble ref) {
        this.ref = ref;
    }

    /**
     * Construct the referencing tool to a {@code WriteDouble}.
     * @param ref       A reference to the {@code WriteDouble} to 'follow'
     * @param source    The block that holds the {@code WriteDouble}
     * @param i         The index of this WriteDouble regarding the source
     */
    public ReadDouble(WriteDouble ref, Block source, int i) {
        this(ref);
        this.source = source;
        this.i = i;
    }

    /**
     * Constructs a referencing tool, assuming it is an internal filter 
     * connection to a composite filter's input.
     * @param ref       A reference to the WriteDouble to 'follow'
     * @param i         The index of the input, regarding the composite filter
     */
    public ReadDouble(WriteDouble ref, int i) {
        this(ref);
        this.i = i;
        isConnectedToInput = true;
    }

    /**
     * @return      The value currently stored in the referenced
     *              {@code WriteDouble}
     */
    public double value() {
        return ref.value;
    }

    /**
     * @return      The index of the output regarding the block that holds it.
     */
    public int outputIndex() {
        return i;
    }

    /**
     * @return      The {@code Block} that holds this {@code WriteDouble}.
     */
    public Block source() {
        return source;
    }

    /**
     * @return      true if this {@code WriteDouble} is connected to an input.
     */
    public boolean connectedToInput() {
        return isConnectedToInput;
    }
}
