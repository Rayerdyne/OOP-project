import be.uliege.straet.oop.filters.*;
import be.uliege.straet.oop.filters.IntegratorFilter;

// import be.uliege.montefiore.oop.audio.*;


/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>Class holding a main method for custom testing.</p>
 * 
 * <p>François Straet</p>
 */
public class Test {

    public static void main(String[] args) {
        double[] tab = new double[] { 1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0, 4.0,
            1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0, 4.0};
        

        try {
            CompositeFilter f = new CompositeFilter(1, 1);

            DelayFilter df = new DelayFilter(5);
            IntegratorFilter gf = new IntegratorFilter(0.5);
            AdditionFilter af = new AdditionFilter();

            f.addBlock(df);
            f.addBlock(gf);
            f.addBlock(af);
            
            f.connectInputToBlock(0, af, 0);
            f.connectBlockToBlock(af, 0, df, 0);
            f.connectBlockToBlock(df, 0, gf, 0);
            f.connectBlockToBlock(gf, 0, af, 1);
            f.connectBlockToOutput(af, 0, 0);

            for (int i = 0; i < tab.length; i++) {
                double[] res = f.computeOneStep(new double[] { tab[i] });
                System.out.println(res[0]);
            }
        }
        catch (Exception e) {
            System.out.println("ERROR : ");
            e.printStackTrace();
        }
    }
 }
