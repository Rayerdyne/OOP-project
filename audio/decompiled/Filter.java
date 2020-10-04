// 
// Decompiled by Procyon v0.5.36
// 

package be.uliege.montefiore.oop.audio;

public interface Filter
{
    int nbInputs();
    
    int nbOutputs();
    
    double[] computeOneStep(final double[] p0) throws FilterException;
    
    void reset();
}
