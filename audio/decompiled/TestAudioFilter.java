// 
// Decompiled by Procyon v0.5.36
// 

package be.uliege.montefiore.oop.audio;

public class TestAudioFilter
{
    public static void applyFilter(final Filter filter, final String s, final String s2) throws FilterException, AudioSequenceException {
        final AudioSequence audioSequence = new AudioSequence(s);
        audioSequence.filter(filter);
        String s3 = s2;
        if (!s2.endsWith(".wav") && !s2.endsWith(".WAV")) {
            s3 = invokedynamic(makeConcatWithConstants:(Ljava/lang/String;)Ljava/lang/String;, s3);
        }
        audioSequence.output(s3);
    }
}
