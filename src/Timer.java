public class Timer {
    private long start;
    private long time;

    public Timer(long time) {
        this.time = time;
    }

    public void reset() {
        start = System.nanoTime();
    }

    public void check() {
        if(System.nanoTime() - start >= time) {
            throw new IllegalStateException("Timeout");
        }
    }

}
