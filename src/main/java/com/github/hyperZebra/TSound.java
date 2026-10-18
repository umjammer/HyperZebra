package com.github.hyperZebra;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;


public class TSound implements LineListener {

    static private final double[] note_freq = new double[96];
    private final ArrayList<Clip> clipList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private volatile boolean nowPlaying = false;
    private Clip nowClip = null;
    public volatile String name = "";
    static boolean use = true;

    private static final float OUT_SAMPLE_RATE = 44100.0f;
    private static final AudioFormat OUT_FORMAT = new AudioFormat(OUT_SAMPLE_RATE, 16, 1, true, false);

    private static class AudioItem {
        final String soundName;
        final byte[] pcmData;
        final int gen;

        AudioItem(String soundName, byte[] pcmData, int gen) {
            this.soundName = soundName;
            this.pcmData = pcmData;
            this.gen = gen;
        }
    }

    private final LinkedBlockingQueue<AudioItem> playQueue = new LinkedBlockingQueue<>();
    private SourceDataLine dataLine = null;
    private Thread playerThread = null;
    private volatile int generation = 0;

    private synchronized void ensurePlayerStarted() {
        if (playerThread != null && playerThread.isAlive()) {
            return;
        }
        playerThread = new Thread(this::runPlayerLoop, "TSound-Player");
        playerThread.setDaemon(true);
        playerThread.start();
    }

    private synchronized SourceDataLine getOrOpenLine() throws LineUnavailableException {
        if (dataLine == null || !dataLine.isOpen()) {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, OUT_FORMAT);
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(OUT_FORMAT, 8820);
            dataLine.start();
        }
        return dataLine;
    }

    private void runPlayerLoop() {
        while (true) {
            try {
                AudioItem item = playQueue.take();
                if (item.gen != generation) {
                    continue;
                }
                SourceDataLine line = getOrOpenLine();
                nowPlaying = true;
                name = item.soundName;
                int offset = 0;
                int chunk = 2048;
                while (offset < item.pcmData.length && item.gen == generation) {
                    int len = Math.min(chunk, item.pcmData.length - offset);
                    line.write(item.pcmData, offset, len);
                    offset += len;
                }
                if (playQueue.isEmpty() && item.gen == generation) {
                    nowPlaying = false;
                    name = "";
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (LineUnavailableException e) {
                nowPlaying = false;
                name = "";
            }
        }
    }

    private static void setNoteFreq() {
        if (note_freq[0] != 1.0) {
            // Calculate semitone (12th root of 2)
            double r = Math.pow(2.0, 1.0 / 12.0);
            note_freq[0] = 1.0;
            for (int i = 1; i < 12; i++) {
                note_freq[i] = note_freq[i - 1] * r;
            }
            for (int i = 12; i < 96; i++) {
                note_freq[i] = note_freq[i - 12] * 2.0;
            }
        }
    }

    public boolean Play(OStack stack, String soundRsrc, String[] neiro, int tempo, int vol)
            throws xTalkException {
        if (!use) return true;

        if (note_freq[0] != 1.0) setNoteFreq();

        // Triplets
        if (neiro[5].contains("3")) {
            if (neiro[5].contains("....")) neiro[5] = "....";
            else if (neiro[5].contains("...")) neiro[5] = "...";
            else if (neiro[5].contains("..")) neiro[5] = "..";
            else if (neiro[5].contains(".")) neiro[5] = ".";
            else neiro[5] = "";
            Play(stack, soundRsrc, neiro, tempo, vol);
            Play(stack, soundRsrc, neiro, tempo, vol);
        }

        // Calculate length (seconds)
        double length = 0.0;
        if (neiro[4].equalsIgnoreCase("w")) length = 240.0 / tempo;
        else if (neiro[4].equalsIgnoreCase("h")) length = 120.0 / tempo;
        else if (neiro[4].equalsIgnoreCase("q")) length = 60.0 / tempo;
        else if (neiro[4].equalsIgnoreCase("e")) length = 30.0 / tempo;
        else if (neiro[4].equalsIgnoreCase("s")) length = 15.0 / tempo;
        else if (neiro[4].equalsIgnoreCase("t")) length = 7.5 / tempo;
        else if (neiro[4].equalsIgnoreCase("x")) length = 3.75 / tempo;
        else if (!neiro[1].isEmpty()) length = 60.0 / tempo;
        if (neiro[5].equals("....")) length *= 1.5 * 1.5 * 1.5 * 1.5;
        if (neiro[5].equals("...")) length *= 1.5 * 1.5 * 1.5;
        if (neiro[5].equals("..")) length *= 1.5 * 1.5; // Doesn't seem to be 1.75x.
        if (neiro[5].equals(".")) length *= 1.5;

        AudioInputStream sound = null;
        try {
            int rsrcid = stack.rsrc.getRsrcIdAll(soundRsrc, "sound");
            String path = stack.rsrc.getFilePathAll(rsrcid, "sound");
            sound = AudioSystem.getAudioInputStream(new File(path/*stack.file.getParent()+File.separatorChar+fileName*/));
        } catch (UnsupportedAudioFileException e) {
            // throw new xTalkException("Does not support audio file for sound \""+soundRsrc+"\"");
            return false;
        } catch (Exception e) {
            /*String path = (PCARDFrame.pc.stack.file.getParent()+File.separatorChar+"resource"+File.separatorChar+soundRsrc);
            try {
                sound = AudioSystem.getAudioInputStream(new File(path+".wav"));
            } catch (Exception e2) {
                try {
                    sound = AudioSystem.getAudioInputStream(new File(path+".aiff"));
                } catch (Exception e3) {*/
            String cpath = ("resource" + File.separatorChar + soundRsrc);
            try {
                sound = AudioSystem.getAudioInputStream(new File(cpath + ".wav"));
            } catch (FileNotFoundException e4) {
                sound = null;
                // throw new xTalkException("Sound \"" + soundRsrc + "\" not found");
            } catch (IOException e4) {
                throw new xTalkException("Cannot open sound \"" + soundRsrc + "\"");
            } catch (UnsupportedAudioFileException e4) {
                throw new xTalkException("Does not support audio file for sound \"" + soundRsrc + "\"");
            }
            // }
            // }
        }

        try {
            int code = 48;
            if (!neiro[1].isEmpty()) {
                code = 0;
                if (neiro[1].equalsIgnoreCase("c")) code = 0;
                else if (neiro[1].equalsIgnoreCase("d")) code = 2;
                else if (neiro[1].equalsIgnoreCase("e")) code = 4;
                else if (neiro[1].equalsIgnoreCase("f")) code = 5;
                else if (neiro[1].equalsIgnoreCase("g")) code = 7;
                else if (neiro[1].equalsIgnoreCase("a")) code = 9;
                else if (neiro[1].equalsIgnoreCase("b")) code = 11;
                if (neiro[2].equals("#")) code += 1;
                if (neiro[2].equals("b")) code -= 1;
                if (neiro[3].isEmpty()) code += 48;
                else code += 12 * Integer.parseInt(neiro[3]);
                if (code < 0) code = 0;
                if (code >= note_freq.length) code = note_freq.length - 1;
            }

            boolean isRest = neiro[1].equalsIgnoreCase("r");
            double pitchRatio = (neiro[1].isEmpty() && length == 0.0) ? 1.0 : (note_freq[code] / note_freq[48]);
            double volScale = Math.max(0.0, Math.min(1.0, vol / 100.0));

            byte[] pcmData;
            if (sound != null) {
                AudioFormat fileFormat = sound.getFormat();
                byte[] voiceData = readAllBytes(sound);
                sound.close();
                sound = null;

                double[] srcSamples = decodeToMonoNormalized(voiceData, fileFormat);
                double step = (fileFormat.getSampleRate() * pitchRatio) / OUT_SAMPLE_RATE;
                if (step <= 0.0) step = 1.0;

                // Length
                int outSamples;
                if (length != 0.0 || isRest) {
                    outSamples = Math.max(1, (int) Math.round(length * OUT_SAMPLE_RATE));
                } else {
                    outSamples = Math.max(1, (int) Math.round(srcSamples.length / step));
                }

                pcmData = new byte[outSamples * 2];
                if (!isRest && srcSamples.length > 0) {
                    int activeSamples = Math.min(outSamples, (int) Math.floor((srcSamples.length - 1) / step));
                    int attackSamples = Math.min((int) (0.003 * OUT_SAMPLE_RATE), activeSamples / 4);
                    int releaseSamples = Math.min((int) (0.008 * OUT_SAMPLE_RATE), activeSamples / 3);
                    for (int i = 0; i < activeSamples; i++) {
                        double srcPos = i * step;
                        int idx0 = (int) srcPos;
                        int idx1 = Math.min(idx0 + 1, srcSamples.length - 1);
                        double frac = srcPos - idx0;
                        double sample = srcSamples[idx0] * (1.0 - frac) + srcSamples[idx1] * frac;

                        double env = 1.0;
                        if (attackSamples > 0 && i < attackSamples) {
                            env = 0.5 * (1.0 - Math.cos(Math.PI * i / attackSamples));
                        } else if (releaseSamples > 0 && i >= activeSamples - releaseSamples) {
                            int rem = activeSamples - 1 - i;
                            env = 0.5 * (1.0 - Math.cos(Math.PI * rem / releaseSamples));
                        }

                        int val = (int) Math.round(sample * env * volScale * 32767.0);
                        if (val > 32767) val = 32767;
                        if (val < -32768) val = -32768;
                        pcmData[i * 2] = (byte) (val & 0xFF);
                        pcmData[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
                    }
                }
            } else {
                pcmData = synthesizePcm16(soundRsrc, length, pitchRatio, isRest, volScale);
            }

            if (vol != 100) {
                // no msg
            }

            int gen = this.generation;
            nowPlaying = true;
            name = soundRsrc;
            playQueue.offer(new AudioItem(soundRsrc, pcmData, gen));
            ensurePlayerStarted();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private static byte[] readAllBytes(AudioInputStream sound) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = sound.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    private static double[] decodeToMonoNormalized(byte[] raw, AudioFormat fmt) {
        int frameSize = Math.max(1, fmt.getFrameSize());
        int channels = Math.max(1, fmt.getChannels());
        int bits = fmt.getSampleSizeInBits();
        boolean bigEndian = fmt.isBigEndian();
        AudioFormat.Encoding enc = fmt.getEncoding();
        int numFrames = raw.length / frameSize;
        double[] out = new double[numFrames];

        for (int f = 0; f < numFrames; f++) {
            int frameOffset = f * frameSize;
            double sum = 0.0;
            for (int c = 0; c < channels; c++) {
                if (bits == 8) {
                    int b = raw[frameOffset + c];
                    if (enc == AudioFormat.Encoding.PCM_UNSIGNED) {
                        sum += ((b & 0xFF) - 128) / 128.0;
                    } else {
                        sum += b / 128.0;
                    }
                } else if (bits == 16) {
                    int off = frameOffset + c * 2;
                    if (off + 1 < raw.length) {
                        short s;
                        if (bigEndian) {
                            s = (short) (((raw[off] & 0xFF) << 8) | (raw[off + 1] & 0xFF));
                        } else {
                            s = (short) (((raw[off + 1] & 0xFF) << 8) | (raw[off] & 0xFF));
                        }
                        sum += s / 32768.0;
                    }
                }
            }
            out[f] = sum / channels;
        }
        return out;
    }

    private static byte[] synthesizePcm16(String soundRsrc, double length, double pitchRatio, boolean isRest, double volScale) {
        double dur = (length > 0.0) ? length : 0.35;
        int numSamples = Math.max(1, (int) Math.round(OUT_SAMPLE_RATE * dur));
        byte[] pcm = new byte[numSamples * 2];
        if (isRest) {
            return pcm;
        }

        double freq = 261.625565 * pitchRatio;
        String sname = soundRsrc != null ? soundRsrc.toLowerCase() : "";
        int attackSamples = Math.min((int) (0.005 * OUT_SAMPLE_RATE), numSamples / 4);
        int releaseSamples = Math.min((int) (0.015 * OUT_SAMPLE_RATE), numSamples / 3);
        double phase = 0.0;

        for (int i = 0; i < numSamples; i++) {
            double t = i / (double) OUT_SAMPLE_RATE;
            double sample;
            if (sname.equals("harpsichord")) {
                double env = Math.exp(-t * 6.0);
                sample = (Math.sin(2.0 * Math.PI * freq * t)
                        + 0.5 * Math.sin(2.0 * Math.PI * freq * 2.0 * t)
                        + 0.25 * Math.sin(2.0 * Math.PI * freq * 3.0 * t)) / 1.75 * env;
            } else if (sname.equals("boing")) {
                double env = Math.exp(-t * 4.0);
                double instFreq = freq * (1.0 + 0.15 * Math.sin(2.0 * Math.PI * 12.0 * t) * Math.exp(-t * 3.0));
                phase += 2.0 * Math.PI * instFreq / OUT_SAMPLE_RATE;
                sample = Math.sin(phase) * env;
            } else {
                double decay = Math.exp(-t * 2.0);
                sample = (Math.sin(2.0 * Math.PI * freq * t)
                        + 0.2 * Math.sin(2.0 * Math.PI * freq * 2.0 * t)) / 1.2 * decay;
            }

            double gateEnv = 1.0;
            if (attackSamples > 0 && i < attackSamples) {
                gateEnv = 0.5 * (1.0 - Math.cos(Math.PI * i / attackSamples));
            } else if (releaseSamples > 0 && i >= numSamples - releaseSamples) {
                int rem = numSamples - 1 - i;
                gateEnv = 0.5 * (1.0 - Math.cos(Math.PI * rem / releaseSamples));
            }

            int val = (int) Math.round(sample * gateEnv * volScale * 18000.0);
            if (val > 32767) val = 32767;
            if (val < -32768) val = -32768;
            pcm[i * 2] = (byte) (val & 0xFF);
            pcm[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
        return pcm;
    }

    public void PlayStop() {
        generation++;
        playQueue.clear();
        synchronized (this) {
            if (dataLine != null && dataLine.isOpen()) {
                dataLine.flush();
            }
        }
        for (Clip c : clipList) {
            c.close();
        }
        clipList.clear();
        nameList.clear();
        nowPlaying = false;
        name = "";
        if (nowClip != null) {
            Clip c = nowClip;
            nowClip = null;
            name = "";
            c.removeLineListener(this);
            c.stop();
            c.close();
        }
    }
    // todo: volume adjustment

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            Clip clip = (Clip) event.getSource();
            clip.removeLineListener(this);
            clip.stop();
            clip.close();
            if (clipList.isEmpty()) {
                nowPlaying = false;
                nowClip = null;
                name = "";
            } else {
                nowClip = clipList.get(0);
                name = nameList.get(0);
                clipList.remove(0);
                nameList.remove(0);
                nowPlaying = true;
                nowClip.addLineListener(this);
                nowClip.start();
            }
        }
        if (event.getType() == LineEvent.Type.CLOSE) {
            nowPlaying = false;
            clipList.clear();
            nameList.clear();
            nowClip = null;
            name = "";
        }
    }
}