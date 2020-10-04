/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>This class implements the reverberator, as described in 
 * <a href=http://www.run.montefiore.ulg.ac.be/~grailet/docs/INFO0062/2019-2020/reverberator.pdf">
 * the course webpage</a></p>
 *
 * <p>François Straet</p>
 */

package be.uliege.straet.oop.filters;

import be.uliege.montefiore.oop.audio.FilterException;

public class ReverberatorFilter extends CompositeFilter {

    AdditionFilter afm = new AdditionFilter();  // middle adder
    AdditionFilter afta = new AdditionFilter(); // top adders
    AdditionFilter aftb = new AdditionFilter();

    CompositeFilter B1 = new CompositeFilter(1, 1);
    CompositeFilter B2 = new CompositeFilter(1, 1);
    CompositeFilter B3 = new CompositeFilter(1, 1);
    
    GainFilter gfva = new GainFilter(0.34); //vertical gains
    GainFilter gfvb = new GainFilter(0.14);
    GainFilter gfvc = new GainFilter(0.14);
    GainFilter gfh = new GainFilter(0.1);  // horizontal gain

    DelayFilter dfLoop = new DelayFilter(1);
    CompositeFilter lowPass = lowPass(0.7133, 2 * 44);

    public ReverberatorFilter() throws FilterException {
        super(1, 1);

        connectB1();
        connectB2();
        connectB3();

        // add...
        addBlock(B1);
        addBlock(B2);
        addBlock(B3);

        addBlock(afm);
        addBlock(afta);
        addBlock(aftb);

        addBlock(gfva);
        addBlock(gfvb);
        addBlock(gfvc);
        addBlock(gfh);
        
        addBlock(lowPass);
        addBlock(dfLoop);

        // connections

        connectInputToBlock(0, afm, 0);

        connectBlockToBlock(afm, 0, B1, 0);
        connectBlockToBlock(B1, 0, B2, 0);
        connectBlockToBlock(B2, 0, B3, 0);

        connectBlockToBlock(B1, 0, gfva, 0);
        connectBlockToBlock(B2, 0, gfvb, 0);
        connectBlockToBlock(B3, 0, gfvc, 0);

        connectBlockToBlock(gfva, 0, afta, 0);
        connectBlockToBlock(gfvb, 0, afta, 1);
        connectBlockToBlock(afta, 0, aftb, 0);
        connectBlockToBlock(gfvc, 0, aftb, 1);

        connectBlockToBlock(B3, 0, lowPass, 0);
        connectBlockToBlock(lowPass, 0, gfh, 0);
        connectBlockToBlock(gfh, 0, dfLoop, 0);
        connectBlockToBlock(dfLoop, 0, afm, 1);


        connectBlockToOutput(aftb, 0, 0);
    }

    /**
     * connectBi functions:
     * makes the connections needed to build the ith block.
     * @throws FilterException      If some connection could not be made.
     */
    private void connectB1() throws FilterException {
        AllPassFilter apfa = new AllPassFilter(0.3, 353);          // 8 ms
        AllPassFilter apfb = new AllPassFilter(0.3, 529);          // 12 ms

        DelayFilter df = new DelayFilter(176);  // 4 ms

        B1.addBlock(apfa);
        B1.addBlock(apfb);
        B1.addBlock(df);

        B1.connectInputToBlock(0, apfa, 0);
        B1.connectBlockToBlock(apfa, 0, apfb, 0);
        B1.connectBlockToBlock(apfb, 0, df, 0);

        B1.connectBlockToOutput(df, 0, 0);
    }

    private void connectB2() throws FilterException {
        AllPassFilter apfi = new AllPassFilter(0.25, 2734);       // 62 ms
        AllPassFilter apfo = new AllPassFilter(0.5, 3837, apfi); // 87 ms

        DelayFilter dfr = new DelayFilter(750);  // 17 ms
        DelayFilter dfl = new DelayFilter(1367); // 31 ms

        B2.addBlock(apfo);
        B2.addBlock(dfr);
        B2.addBlock(dfl);

        B2.connectInputToBlock(0, dfr, 0);
        B2.connectBlockToBlock(dfr, 0, apfo, 0);
        B2.connectBlockToBlock(apfo, 0, dfl, 0);
        B2.connectBlockToOutput(dfl, 0, 0);
    }

    private void connectB3() throws FilterException {
        CompositeFilter cf = new CompositeFilter(1, 1);

        AllPassFilter apfia = new AllPassFilter(0.25, 3352); // 76 ms
        AllPassFilter apfib = new AllPassFilter(0.25, 1323); // 30 ms

        cf.addBlock(apfia);
        cf.addBlock(apfib);
        cf.connectInputToBlock(0, apfia, 0);
        cf.connectBlockToBlock(apfia, 0, apfib, 0);
        cf.connectBlockToOutput(apfib, 0, 0);

        AllPassFilter apfo = new AllPassFilter(0.5, 5292, cf); // 120 ms
        DelayFilter df = new DelayFilter(132);  // 3 ms

        B3.addBlock(apfo);
        B3.addBlock(df);

        B3.connectInputToBlock(0, df, 0);
        B3.connectBlockToBlock(df, 0, apfo, 0);
        B3.connectBlockToOutput(apfo, 0, 0);
    }
    
    /**
     * Return a newly created low-pass filter.
     * This could alternatively be another class extending CompositeFilter.
     * @param gain                  The gain used for the filter
     * @param delay                 The delay used for the filter
     * @return                      The newly created filter
     * @throws FilterException      If some filter could not be instanciated
     */
    public static CompositeFilter lowPass(double gain, int delay) 
        throws FilterException {

        CompositeFilter f = new CompositeFilter(1, 1);
        
        // instanciate members filters...
        GainFilter gfa = new GainFilter(1-gain);
        GainFilter gfb = new GainFilter(gain);
        DelayFilter df = new DelayFilter(delay);
        AdditionFilter af = new AdditionFilter();

        // add them to the composite filter
        f.addBlock(gfa);
        f.addBlock(gfb);
        f.addBlock(df);
        f.addBlock(af);

        // connections...
        f.connectInputToBlock(0, gfa, 0);

        f.connectBlockToBlock(gfa, 0, af, 0);
        f.connectBlockToBlock(gfb, 0, af, 1);
        f.connectBlockToBlock(df, 0, gfb, 0);
        f.connectBlockToBlock(af, 0, df, 0);

        f.connectBlockToOutput(af, 0, 0);

        return f;
    }

}
