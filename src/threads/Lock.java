package threads;

import java.util.List;

public class Lock {

    private int threads = 0;

    public synchronized void work(List<Integer> queue, int limit) {
        while (queue.size() == limit && threads != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threads++;
    }

    public synchronized void finishWork() {
        threads = 0;
        notifyAll();
    }

}
