package com.github.axet.wget;

import java.util.ArrayList;

public class SpeedInfo {

    public static final int SAMPLE_LENGTH = 1000;
    public static final int SAMPLE_MAX = 20;

    public class Sample {
        // bytes downloaded
        public long current;
        // current time
        public long now;
        // start block? used to mark block after download has been altered /
        // restarted
        public boolean start;

        public Sample() {
            current = 0;
            now = System.currentTimeMillis();
            start = false;
        }

        public Sample(long current) {
            this.current = current;
            now = System.currentTimeMillis();
            start = false;
        }

        public Sample(long current, long now) {
            this.current = current;
            this.now = now;
            start = false;
        }
    }

    protected ArrayList<Sample> samples = new ArrayList<SpeedInfo.Sample>();
    protected long peak;

    // start sample use to calculate average speed
    protected Sample start = null;

    public SpeedInfo() {
    }

    /**
     * Start calculate speed from 'current' bytes downloaded
     * 
     * @param current
     */
    synchronized public void start(long current) {
        Sample s = new Sample(current);
        s.start = true;
        start = s;
        add(s);
    }

    /**
     * step download process with 'current' bytes downloaded
     * 
     * @param current
     */
    synchronized public void step(long current) {
        long now = System.currentTimeMillis();

        long lastUpdate = getLastUpdate();
        if (lastUpdate + SAMPLE_LENGTH < now) {
            add(new Sample(current, now));
        }
    }

    /**
     * Current download speed
     * 
     * @return bytes per second
     */
    synchronized public int getCurrentSpeed() {
        if (getRowSamples() < 2)
            return 0;

        // [s1] [s2] [EOF]
        Sample s1 = samples.get(samples.size() - 2);
        Sample s2 = samples.get(samples.size() - 1);

        long current = s2.current - s1.current;
        long time = s2.now - s1.now;

        if (time == 0)
            return 0;

        return (int) (current * 1000 / time);
    }

    /**
     * Average speed from start download
     * 
     * @return bytes per second
     */
    synchronized public int getAverageSpeed() {
        if (start == null || getRowSamples() < 2)
            return 0;

        Sample s2 = samples.get(samples.size() - 1);

        long current = s2.current - start.current;
        long time = s2.now - start.now;

        return (int) (current * 1000 / time);
    }

    /**
     * Average speed for maximum stepsBack steps
     * 
     * @param stepsBack
     * @return bytes per second
     */
    synchronized public int getAverageSpeed(int stepsBack) {
        if (start == null || getRowSamples() < 2)
            return 0;

        int is2 = samples.size() - 1;
        int is1 = is2 - stepsBack;
        if (is1 < 0)
            is1 = 0;

        Sample s1 = samples.get(is1);

        // if steps back below start download, then use start mark
        if (s1.now < start.now)
            s1 = start;

        Sample s2 = samples.get(is2);

        long current = s2.current - s1.current;
        long time = s2.now - s1.now;

        return (int) (current * 1000 / time);
    }

    synchronized public int getSamples() {
        return samples.size();
    }

    synchronized public Sample getSample(int index) {
        return samples.get(index);
    }

    synchronized public long getPeak() {
        return peak;
    }

    //
    // protected
    //

    protected Sample getStart() {
        for (int i = samples.size() - 1; i >= 0; i--) {
            Sample s = samples.get(i);
            if (s.start)
                return s;
        }

        return null;
    }

    protected void add(Sample s) {
        // check if we have broken / restarted download. check if here some
        // samples
        if (samples.size() > 0) {
            Sample s1 = samples.get(samples.size() - 1);
            // check if last download 'current' stands before current 'current'
            // download
            if (s1.current > s.current) {
                s.start = true;
                start = s;
            }
        }

        samples.add(s);

        while (samples.size() > SAMPLE_MAX)
            samples.remove(0);

        peakUpdate();
    }

    /**
     * return number of samples in the row (before download restart)
     * 
     * @return
     */
    protected int getRowSamples() {
        for (int i = samples.size() - 1; i >= 0; i--) {
            Sample s = samples.get(i);
            if (s.start)
                return samples.size() - i;
        }

        return samples.size();
    }

    protected long getLastUpdate() {
        if (samples.size() == 0)
            return 0;

        Sample s = samples.get(samples.size() - 1);
        return s.now;
    }

    protected void peakUpdate() {
        peak = 0;
        for (Sample s : samples) {
            if (peak < s.current)
                peak = s.current;
        }
    }
}
