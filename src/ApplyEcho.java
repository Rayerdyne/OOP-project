/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>Class holding a main method for that builds the echo filter.</p>
 * 
 * <p>François Straet</p>
 */

import be.uliege.straet.oop.filters.*;

import be.uliege.montefiore.oop.audio.TestAudioFilter;

class ApplyEcho {
    public static void main(String[] args) {
        String fileNameIn = "Source.wav";
        String fileNameOut = "Filtered.wav";

        if (args.length >= 1) {
            fileNameIn = args[0];
            if (args.length >= 2)
                fileNameOut = args[1];
        }

        try {
        CompositeFilter f = new CompositeFilter(1, 1);

        DelayFilter delay = new DelayFilter(10000);
        GainFilter gain = new GainFilter(0.6);
        AdditionFilter adder = new AdditionFilter();

        f.addBlock(delay);
        f.addBlock(gain);
        f.addBlock(adder);

        f.connectInputToBlock(0, adder, 0);

        f.connectBlockToBlock(gain, 0, adder, 1);
        f.connectBlockToBlock(adder, 0, delay, 0);
        f.connectBlockToBlock(delay, 0, gain, 0);

        f.connectBlockToOutput(adder, 0, 0);

        TestAudioFilter.applyFilter(f, fileNameIn, fileNameOut);
        }
        catch (Exception e) {
            // System.err.println(e.getMessage());
            e.printStackTrace();
        }


    }
}