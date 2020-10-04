/*
 * INFO0062 - Object-Oriented Programming
 * Project basis
 *
 * Example code to filter a WAV file using audio.jar. The filter has to be implemented first.
 * 
 * @author: J.-F. Grailet (ULiege)
 */

import be.uliege.straet.oop.filters.*;
import be.uliege.montefiore.oop.audio.*; // Will import: TestAudioFilter, Filter, FilterException

public class Example
{
    public static void main(String[] args)
    {
        String in = new String("Source.wav");
        String out = new String("Filtered.wav");
        if (args.length >= 1)
            in = args[0] + ".wav";
        if (args.length >= 2)
            out = args[1] + ".wav";

        try
        {
            CompositeFilter f = allPass(0.5, -0.5, 4410);

            TestAudioFilter.applyFilter(f, in, out);
        }
        catch(Exception e)
        {
            System.err.println(e.getMessage());
        }
    }

    private static CompositeFilter allPass(double gain1, double gain2, int 
        delay) throws FilterException {

        CompositeFilter f = new CompositeFilter(1, 1);

        AdditionFilter afa = new AdditionFilter();
        AdditionFilter afb = new AdditionFilter();
        GainFilter gfa = new GainFilter(gain1);
        GainFilter gfb = new GainFilter(gain2);
        DelayFilter df = new DelayFilter(delay);

        f.addBlock(afa);
        f.addBlock(afb);
        f.addBlock(gfa);
        f.addBlock(gfb);
        f.addBlock(df);

        f.connectInputToBlock(0, afa, 0);
        f.connectInputToBlock(0, gfb, 0);

        f.connectBlockToBlock(gfa, 0, afa, 1);
        f.connectBlockToBlock(gfb, 0, afb, 0);
        f.connectBlockToBlock(df, 0, afb, 1);
        f.connectBlockToBlock(afa, 0, df, 0);
        f.connectBlockToBlock(afb, 0, gfa, 0);

        f.connectBlockToOutput(afb, 0, 0);

        return f;
    }

    /*
    private static CompositeFilter echo() throws FilterException {
        CompositeFilter f = new CompositeFilter(1, 1);

        DelayFilter df = new DelayFilter(44100);
        GainFilter gf = new GainFilter(0.5);
        AdditionFilter af = new AdditionFilter();

        f.addBlock(df);
        f.addBlock(gf);
        f.addBlock(af);
        
        f.connectInputToBlock(0, af, 0);
        f.connectBlockToBlock(af, 0, df, 0);
        f.connectBlockToBlock(df, 0, gf, 0);
        f.connectBlockToBlock(gf, 0, af, 1);
        f.connectBlockToOutput(af, 0, 0);

        return f;
    }

    private static CompositeFilter noise(double p) throws FilterException {
            CompositeFilter f = new CompositeFilter(1, 1);
            DifferentiatorFilter df = new DifferentiatorFilter();
            AdditionFilter af = new AdditionFilter();
            GainFilter gf = new GainFilter(p);

            f.addBlock(df);
            f.addBlock(af);
            f.addBlock(gf);

            f.connectInputToBlock(0, af, 0);
            f.connectInputToBlock(0, df, 0);
            f.connectBlockToBlock(df, 0, gf, 0);
            f.connectBlockToBlock(gf, 0, af, 1);
            f.connectBlockToOutput(af, 0, 0);

            return f;
    }
    */
}
