package be.uliege.straet.oop.gui;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

import be.uliege.montefiore.oop.audio.AudioSequenceException;
import be.uliege.montefiore.oop.audio.FilterException;

import be.uliege.straet.oop.filters.CompositeFilter;

public class Computer implements Runnable {

    public static final int SAMPLING_FREQUENCY = 44100;
    public static final int SAMPLE_SIZE_BITS = 16;
    public static final int CHANNELS = 2;
    public static final boolean SIGNED = true; 
    public static final boolean BIG_ENDIAN = true;

    public static final int CONTINUE = 0;
    public static final int PAUSE = 1;
    public static final int ABORT = 2;

    public static final int COMPUTE_FILE = 0;
    public static final int PLAY_AUDIO = 1;
    public static final int APPLY_TO_VOICE = 2;



    private AudioSequence2[] inputs;
    /** The size of the longest input in samples (some audio inputs may be
     *  shorter) */
    private long audioSize = 0;

    private CompositeFilter cfR, cfL;
    private int type = COMPUTE_FILE;
    private int nbInputs;

    // runFile() needs:
    private String filename = null;
    // runAudio() needs:
    private boolean paused = false;
    private boolean abort = false;
    // runVoice() needs:
    private int[] remote;
    private TargetDataLine line;


    private short[][] rightChannels;
    private short[][] leftChannels;

    /**
     * Constructs a Computer.
     * 
     * @param inputs An array of AudioSequence2 that will be inputted to the
     *               composite filter
     * @param cf     The CompositeFilter we want to apply to the inputs
     * @param type   Sets the type of computation to do: PLAY_AUDIO, 
     *               COMPUTE_FILE or APPLY_TO_VOICE
     * @throws ComputationException If the number of inputs mismatch the number
     *                              AudioSequences.
     */
    public Computer(AudioSequence2[] inputs, CompositeFilter cfR, 
        CompositeFilter cfL, int type) 
        throws ComputationException {

        if (type != APPLY_TO_VOICE && inputs.length != cfR.nbInputs())
            throw new ComputationException("The numbers of AudioSequences "
                + "and of composite filter inputs mismatch.");

        this.inputs = inputs;
        this.cfR = cfR;
        this.cfL = cfL;
        this.type = type;

        nbInputs = cfR.nbInputs();

        if (type !=  APPLY_TO_VOICE)
            for (AudioSequence2 input : inputs)
                audioSize = audioSize < input.getSize() ? input.getSize() : 
                    audioSize;

        if (type == PLAY_AUDIO && cfR.nbOutputs() != 1) 
            throw new ComputationException("Could not play the output of a " +
                "filter that has more than one output.");
    }

    /**
     * Constructs a Computer.
     * 
     * @param inputs   An array of AudioSequence2 that will be inputted to the
     *                 composite filter
     * @param cf       The CompositeFilter we want to apply to the inputs
     * @param filename The name of the output wav file
     * @throws ComputationException If the number of inputs mismatch the number
     *                              AudioSequences.
     */
    public Computer(AudioSequence2[] inputs, CompositeFilter cfR, 
        CompositeFilter cfL, String filename) throws ComputationException {
        this(inputs, cfR, cfL, COMPUTE_FILE);
        this.filename = filename;

        if (cfR.nbOutputs() != 1)
            throw new ComputationException("Could not output to file more " + 
                "than one sound.");
    }

    public Computer(AudioSequence2[] inputs, CompositeFilter cfR, 
        CompositeFilter cfL, int[] remote) throws ComputationException {

        this(inputs, cfR, cfL, APPLY_TO_VOICE);
        this.remote = remote;
        
        AudioFormat format = new AudioFormat(SAMPLING_FREQUENCY, 
            SAMPLE_SIZE_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); 
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            throw new ComputationException("Line is unavailible. " + 
                "(LineUnavailibleException exception raised)");
        }

        if (!AudioSystem.isLineSupported(info)) 
            throw new ComputationException("Line is not supported. " + 
                "(isLineSupported(info) returned false)");

        if (cfR.nbInputs() != 1) 
            throw new ComputationException("There should be exactly 1 input " +
                "in order to apply the filter to the microphone input.");
    }

    public void run() {
        switch(type) {
            case COMPUTE_FILE:
                runFile();          break;
            case PLAY_AUDIO:
                runAudio(false);    break;
            case APPLY_TO_VOICE:
                runVoice();         break;
        }
    }

    public void runFile() {
        setChannels();

        try {
            for (long i = 0; i < audioSize; i++)
                inputs[0].setValue(i, true, computeOneStep(i, true)); // right
            cfR.reset();

            for (long i = 0; i < audioSize; i++)
                inputs[0].setValue(i, false, computeOneStep(i, false)); // left
            cfL.reset();

            inputs[0].output(filename);
        } catch (FilterException e) {
            WorkSpace.showError("Could not compute step.", e);
        } catch (AudioSequenceException e) {
            WorkSpace.showError("Some I/O error occured when writing the " + 
                "output to a file.", e);
        }

        JOptionPane.showMessageDialog(null, 
            "The computation of the output is finished", "Info",
            JOptionPane.INFORMATION_MESSAGE);

        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Plays the audio after the filter has been applied.
     * @param areChannelsSet    If true, does not loads the short arrays from 
     *                          the `AudioSequence`s, an duse them as they are.
     */
    public void runAudio(boolean areChannelsSet) {
        if (!areChannelsSet)
            setChannels();
        AudioFormat format = new AudioFormat(SAMPLING_FREQUENCY, SAMPLE_SIZE_BITS, CHANNELS, 
                                             SIGNED, BIG_ENDIAN);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, 
            format, 2200);

        try {
            SourceDataLine soundLine = (SourceDataLine) 
                AudioSystem.getLine(info);
            soundLine.open(format);

            soundLine.start();
            int bufferSize = 8800;
            byte[] buffer = new byte[bufferSize];

            long i = 0;
            while (i < audioSize) {
                if (!paused) {
                    for (int j = 0; j < bufferSize && i < audioSize; j += 4) {
                        short sR = computeOneStep(i, true);
                        short sL = computeOneStep(i, false);
                        buffer[j ] = (byte) (sL >> 8 & 0xFF);
                        buffer[j + 1] = (byte) (sL & 0xFF);
                        buffer[j + 2] = (byte) (sR >> 8 & 0xFF);
                        buffer[j + 3] = (byte) (sR & 0xFF);
                        i++;
                    }
                }
                switch (manageRequests()) {
                    case CONTINUE: 
                        // the next call is blocking until the entire buffer is
                        // sent to the SourceDataLine
                        soundLine.write(buffer, 0, bufferSize);
                        break;
                    case PAUSE:    
                        try {
                            TimeUnit.MILLISECONDS.sleep(200);
                        } catch (InterruptedException e) { }
                        break;
                    case ABORT:     return;
                }
            }

        } catch (LineUnavailableException e) {
            WorkSpace.showError("Recieved `LineUnavailibleException`.", e);
        } catch (FilterException e) {
            WorkSpace.showError("Could not compute step.", e);
        }
        cfR.reset();
        cfL.reset();
    }

    public void runVoice() {
        // Assume that the TargetDataLine, line, has already
        // been obtained and opened.
        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        int numBytesRead;
        byte[] data = new byte[line.getBufferSize() / 5];

        // Begin audio capture.
        line.start();
        while(remote[0] == -1) {
            // Read the next chunk of data from the TargetDataLine and write it
            numBytesRead =  line.read(data, 0, data.length);
            out.write(data, 0, numBytesRead);
        }
        line.close();

        byte[] b = out.toByteArray();
        //  4 = #bytes per sample = CHANNELS * (SAMPLE_SIZE_BITS / 8)
        audioSize = b.length / 4;

        rightChannels = new short[nbInputs][];
        leftChannels = new short[nbInputs][];
        rightChannels[0] = new short[(int) audioSize];
        leftChannels[0] = new short[(int) audioSize];

        for (int i = 0; i < b.length; i += 4) {
            leftChannels[0][i / 4] = 
                (short)((b[i] & 0xFF) << 8 | (b[i + 1] & 0xFF));
            rightChannels[0][i / 4] = 
                (short)((b[i + 2] & 0xFF) << 8 | (b[i + 3] & 0xFF));
        }
        runAudio(true);
    }

    private short computeOneStep(long i, boolean isRight) 
        throws FilterException {

        double[] in = new double[nbInputs];
        for (int j = 0; j < nbInputs; j++)
            in[j] = isRight ? rightChannels[j][(int) i] : 
                              leftChannels[j][(int) i];

        if (isRight)
            return (short) cfR.computeOneStep(in)[0];
        else
            return (short) cfL.computeOneStep(in)[0];
    }

    /**
     * Sets the channels inputs, i.e. fetches short[] from the audio sequences.
     */
    private void setChannels() {
        rightChannels = new short[nbInputs][];
        leftChannels = new short[nbInputs][];
        for (int i = 0; i < nbInputs; i++) {
            rightChannels[i] = inputs[i].getRightChannel();
            leftChannels[i] = inputs[i].getLeftChannel();
        }
    }

    public synchronized int manageRequests() {
        if (abort)
            return ABORT;

        if (paused) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) { }
            return PAUSE;
        }

        return CONTINUE;
    }

    public synchronized void pause()  {  paused = true;    }
    public synchronized void play()   {  paused = false;   }
    public synchronized void toggle() {  paused = !paused; }
    public synchronized void end()    {  abort = true;     }
    public boolean isAudioPaused()    {  return paused;    }

    public long getAudioSize() {  return audioSize;  }
}