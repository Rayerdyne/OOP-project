package be.uliege.straet.oop.gui;

// 
// Decompiled by Procyon v0.5.36
// 
// Then I added what I needed to be able to have multiple input files.
// Note that when one input file exceeds the other in length, we'll put zeros
// instead.

import be.uliege.montefiore.oop.audio.AudioSequenceException;
import be.uliege.montefiore.oop.audio.Filter;
import be.uliege.montefiore.oop.audio.FilterException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * <p>Decompiled (by Procyon v0.5.36) version of {@code AudioSequence} to add 
 * the ability to have multiple input files.</p>
 * <p>If one input is "too short" comparing to the others, it'll put zeros.</p>
 */
public class AudioSequence2 {

    private short[] leftChannel;
    private short[] rightChannel;
    private AudioFormat format;

    /**
     * Constructor.
     * @param pathname      The path to the file to open and load in this 
     *                     {@code AudioSequence}
     * @throws AudioSequenceException   If the input file could not be opened 
     *                                  or loaded
     */
    public AudioSequence2(final String pathname) throws AudioSequenceException
        {
        try {
            if (!pathname.endsWith(".csv")) {
                final AudioInputStream audioInputStream = 
                    AudioSystem.getAudioInputStream(new File(pathname));
                this.format = audioInputStream.getFormat();
                final long n = audioInputStream.getFrameLength() * 
                                this.format.getFrameSize();
                if (n > 2147483647L) {
                    throw new AudioSequenceException("Your audio file is too" +
                        " large and amounts more than " + n);
                }
                final byte[] b = new byte[(int)n];
                audioInputStream.read(b, 0, b.length);
                this.leftChannel = new short[b.length / 4];
                this.rightChannel = new short[b.length / 4];
                for (int i = 0; i < b.length; i += 4) {
                    this.leftChannel[i / 4] = 
                        (short)((b[i + 1] & 0xFF) << 8 | (b[i] & 0xFF));
                    this.rightChannel[i / 4] = 
                        (short)((b[i + 3] & 0xFF) << 8 | (b[i + 2] & 0xFF));
                }
            }
            else {
                byte[] encoded = Files.readAllBytes(Paths.get(pathname));
                String contents = new String(encoded, 
                                             StandardCharsets.US_ASCII);
                String[] numbers = contents.split("[,]");
                int nb = numbers[numbers.length-1] == null ? 
                         numbers.length - 1 : numbers.length;
                leftChannel = new short[nb / 2];
                rightChannel = new short[nb / 2];
                for (int i = 0; i < nb; i += 2) {
                    leftChannel[i / 2] = Short.valueOf(numbers[i].trim());
                    rightChannel[i / 2] = Short.valueOf(numbers[i+1].trim());
                }
            }
        }
        catch (AudioSequenceException ex) {
            throw ex;
        }
        catch (UnsupportedAudioFileException ex3) {
            throw new AudioSequenceException("This type of audio file is not" +
                " supported.");
        }
        catch (IOException ex2) {
            throw new AudioSequenceException("An I/O error occured when " + 
                "reading the input file: " + ex2.getMessage());
        }
        catch (ArrayIndexOutOfBoundsException ex3) {
            throw new AudioSequenceException("Could not load csv file due " +
                "to odd number of values.");
        }
    }
    
    /**
     * Filters the array with given {@code Filter}.
     * @param array     The sequence of number to filter
     * @param filter    The {@code Filter} to apply
     * @throws FilterException      If the {@code Filter} could not process the
     *                              input.
     */
    private static void filter(final short[] array, final Filter filter) 
        throws FilterException {
        for (int i = 0; i < array.length; ++i) {
            array[i] = (short)filter.computeOneStep( 
                new double[] { array[i] })[0];
        }
        filter.reset();
    }
    
    /**
     * Filters the left and the right channels of the {@code AudioSequence} 
     * with given {@code Filter}.
     * @param filter                The {@code Filter} to apply
     * @throws FilterException      If the {@code Filter} could not process the
     *                              input.
     */
    public void filter(final Filter filter) throws FilterException {
        filter(this.leftChannel, filter);
        filter(this.rightChannel, filter);
    }

    /**
     * (I added this method)
     * @return      The right channel of the {@code AudioSequence}
     */
    public short[] getRightChannel() {
        /*for (int i = 0; i < 500; i++) {
            System.out.print("  " + rightChannel[444444 + i]);
        }*/
        return rightChannel;}
    /**
     * (I added this method)
     * @return      The right channel of the {@code AudioSequence}
     */
    public short[] getLeftChannel() {   return leftChannel;   }
    /**
     * @return      The number of the samples in that {@code AudioSequence}
     */
    public long getSize() {  return leftChannel.length;  }

    /**
     * <p>Get the incoming right or left channels values.</p>
     * 
     * <p>If we exceed the audiosequence's lenght, then we return 0.</p>
     * @param i         The index of the value we want
     * @param isRight   If true, we return the value for the right channel, 
     *                  otherwise the value of the left channel
     * @return      An array containing the next values of the audio sequence
     */
    public short getValue(long i, boolean isRight) {
        if (i >= getSize())
            return 0;
        
        return isRight ? rightChannel[(int) i] : leftChannel[(int) i];  
    }

    /**
     * Sets a value of the audio sequence (in order to save some memory).
     * @param i         The index of the value we want to set
     * @param isRight   If true, we modify the right channel, the left 
     *                  otherwise
     * @param v         The new value
     */
    public void setValue(long i,  boolean isRight, short v) {
        if (isRight)
            rightChannel[(int) i] = v;
        else    
            leftChannel[(int) i] = v;
    }
    /* </My added stuff> */
    
    /**
     * Outputs the computed output to a file.
     * @param pathname      The path to the file to output to
     * @throws AudioSequenceException   If we have a problem with the files...
     */
    public void output(final String pathname) throws AudioSequenceException {
        try {
            if (!pathname.endsWith(".csv")) {
                final byte[] buf = new byte[this.leftChannel.length * 4];
                for (int i = 0; i < this.leftChannel.length; ++i) {
                    buf[i * 4] = (byte)(this.leftChannel[i] & 0xFF);
                    buf[i * 4 + 1] = (byte)(this.leftChannel[i] >> 8 & 0xFF);
                    buf[i * 4 + 2] = (byte)(this.rightChannel[i] & 0xFF);
                    buf[i * 4 + 3] = (byte)(this.rightChannel[i] >> 8 & 0xFF);
                }
                AudioSystem.write(new AudioInputStream(
                    new ByteArrayInputStream(buf), this.format, 
                    this.leftChannel.length), AudioFileFormat.Type.WAVE, 
                    new File(pathname));
            }
            else {
                FileWriter fw = new FileWriter(pathname);
                PrintWriter pw = new PrintWriter(fw);
                for (int i = 0; i < this.leftChannel.length; ++i) {
                    pw.write(Short.toString(this.leftChannel[i]));
                    pw.write(",");
                    pw.write(Short.toString(this.rightChannel[i]));
                    pw.write(",");
                }
                pw.close();
            }
        }
        catch (IOException ex) {
            throw new AudioSequenceException("An I/O error occurred while " +
                "writing the output file: " + ex.getMessage());
        }
    }
}