// 
// Decompiled by Procyon v0.5.36
// 

package be.uliege.montefiore.oop.audio;

import javax.sound.sampled.AudioFileFormat;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import javax.sound.sampled.AudioFormat;

public class AudioSequence
{
    private short[] leftChannel;
    private short[] rightChannel;
    private AudioFormat format;
    
    public AudioSequence(final String pathname) throws AudioSequenceException {
        try {
            final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(pathname));
            this.format = audioInputStream.getFormat();
            final long n = audioInputStream.getFrameLength() * this.format.getFrameSize();
            if (n > 2147483647L) {
                throw new AudioSequenceException(invokedynamic(makeConcatWithConstants:(Ljava/lang/String;)Ljava/lang/String;, "Your audio file is too large and amounts more than "));
            }
            final byte[] b = new byte[(int)n];
            audioInputStream.read(b, 0, b.length);
            this.leftChannel = new short[b.length / 4];
            this.rightChannel = new short[b.length / 4];
            for (int i = 0; i < b.length; i += 4) {
                this.leftChannel[i / 4] = (short)((b[i + 1] & 0xFF) << 8 | (b[i] & 0xFF));
                this.rightChannel[i / 4] = (short)((b[i + 3] & 0xFF) << 8 | (b[i + 2] & 0xFF));
            }
        }
        catch (AudioSequenceException ex) {
            throw ex;
        }
        catch (UnsupportedAudioFileException ex3) {
            throw new AudioSequenceException("This type of audio file is not supported.");
        }
        catch (IOException ex2) {
            throw new AudioSequenceException(invokedynamic(makeConcatWithConstants:(Ljava/lang/String;)Ljava/lang/String;, ex2.getMessage()));
        }
    }
    
    private static void filter(final short[] array, final Filter filter) throws FilterException {
        for (int i = 0; i < array.length; ++i) {
            array[i] = (short)filter.computeOneStep(new double[] { array[i] })[0];
        }
        filter.reset();
    }
    
    public void filter(final Filter filter) throws FilterException {
        filter(this.leftChannel, filter);
        filter(this.rightChannel, filter);
    }
    
    public void output(final String pathname) throws AudioSequenceException {
        try {
            final byte[] buf = new byte[this.leftChannel.length * 4];
            for (int i = 0; i < this.leftChannel.length; ++i) {
                buf[i * 4] = (byte)(this.leftChannel[i] & 0xFF);
                buf[i * 4 + 1] = (byte)(this.leftChannel[i] >> 8 & 0xFF);
                buf[i * 4 + 2] = (byte)(this.rightChannel[i] & 0xFF);
                buf[i * 4 + 3] = (byte)(this.rightChannel[i] >> 8 & 0xFF);
            }
            AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(buf), this.format, this.leftChannel.length), AudioFileFormat.Type.WAVE, new File(pathname));
        }
        catch (IOException ex) {
            throw new AudioSequenceException(invokedynamic(makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;, "An I/O error occurred while writing the output file: ", ex.getMessage()));
        }
    }
}
