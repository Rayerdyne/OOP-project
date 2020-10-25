import be.uliege.straet.oop.filters.*;
import be.uliege.straet.oop.loader.Writer;
import be.uliege.montefiore.oop.audio.TestAudioFilter;


/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>Class holding a main method for that builds the echo filter.</p>
 * 
 * <p>François Straet</p>
 */
class Demo {
    public static void main(String[] args) {
        if (args.length >= 3 && args[0].equals("Reverb")) {
            appyReverb(args[1], args[2]);
        }

        else if (args.length >= 2) {
            applyEcho(args[0], args[1]);
        }

        else {
            System.out.println( "Wrong usage !\n" +
                                "Demo <source> <destination> or \n" +
                                "Demo Reverb <source> <destination>.");
        }

    }

    static void appyReverb(String in, String out) {
        try {
            ReverberatorFilter f = new ReverberatorFilter();

            Writer.writeFilter(f, "Dabou2.xml");
    
            // Apply the filter to the input
            // TestAudioFilter.applyFilter(f, in, out);
            }
        catch (Exception e) {
            // System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    static void applyEcho(String in, String out) {
        try {
            CompositeFilter f = new CompositeFilter(1, 1);
    
            // Create filters needed to the EchoFilter
            DelayFilter delay = new DelayFilter(10000);
            GainFilter gain = new GainFilter(0.6);
            AdditionFilter adder = new AdditionFilter();
    
            // Add them...
            f.addBlock(delay);
            f.addBlock(gain);
            f.addBlock(adder);
            
            
            // Connections
            f.connectInputToBlock(0, adder, 0);
    
            f.connectBlockToBlock(gain, 0, adder, 1);
            f.connectBlockToBlock(adder, 0, delay, 0);
            f.connectBlockToBlock(delay, 0, gain, 0);
    
            f.connectBlockToOutput(adder, 0, 0);
    
            Writer.writeFilter(f, "Dabou.xml");
            // Apply the filter to the input
            TestAudioFilter.applyFilter(f, in, out);

            }
        catch (Exception e) {
            // System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
