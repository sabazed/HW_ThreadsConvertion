package threads;

import java.util.*;

public class Task2 {
    private static final int NUM_THREADS = 10;
    private static final int CHANNEL_CAPACITY = 100;
    private static final int POISON_PILL = -1;

    public static List<String> generate(final int from, final int to, final int count) throws InterruptedException {
        if (from < 0 || to < 0 || !isInRange(count, 0, to - from + 1)) throw new IllegalArgumentException();

        List<String> generated = new ArrayList<>(count);

        // Create a queue for communication channel and an arraylist to save all used numbers
        List<Integer> used = new ArrayList(count);
        List<Integer> comms = new LinkedList<>();

        // Create a lock for accessing comms
        Lock lock = new Lock();

        // Create a producer thread which randomizes numbers and adds them to a queue
        Thread producer = new Thread() {
            @Override
            public void run() {
                // Create Random class object to generate ints within bounds
                Random randomizer = new Random();
                // Save remaining numbers
                int remaining = count;
                while (used.size() != count) {
                    // Determine how many numbers should be added to fill container / finish generating
                    int loopnum;
                    if (remaining > CHANNEL_CAPACITY) {
                        remaining -= CHANNEL_CAPACITY;
                        loopnum = CHANNEL_CAPACITY;
                    }
                    else loopnum = remaining;
                    // Lock the comms queue
                    lock.work(comms, CHANNEL_CAPACITY);
                    for (int i = 0; i < loopnum; i++) {
                        // Generate a random number
                        int randomNum = randomizer.nextInt(from, to + 1);
                        // Check if the same number has already been generated
                        if (!used.contains(randomNum)) {
                            // If it is a unique number then it should be put both to the used list and comms queue
                            enqueue(comms, randomNum);
                            enqueue(used, randomNum);
                        }
                        else {
                            // Retry generating once more
                            i--;
                        }
                    }
                    // Unlock the queue
                    lock.finishWork();
                }
                lock.work(comms, CHANNEL_CAPACITY);
                // When the work is finished 10 poison pills are sent to the consumers
                for (int i = 0; i < 10; i++) {
                    enqueue(comms, POISON_PILL);
                }
                lock.finishWork();
            }
        };

        // Create an array of consumers that convert numbers
        Thread[] consumers = new Thread[NUM_THREADS];
        // Loop over the array and set the elements to consumer threads (overridden run())
        for (int i = 0; i < NUM_THREADS; i++) {
            consumers[i] = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        // Create a variable for the next number from the queue
                        int nextNumber = 0;
                        // Start the consumer
                        lock.work(comms, 0);
                        // Get the next number from the comms
                        nextNumber = dequeue(comms);
                        // Unlock the queue
                        lock.finishWork();
                        // If the poison pill is received the thread stops working
                        if (nextNumber == -1) break;
                        // Otherwise, the string is generated and added to the arraylist
                        String generatedString = nextNumber + ", " + KanjiLib.convert(nextNumber);
                        addString(generated, generatedString);
                    }
                }
            };
        }

        // Start all threads
        producer.start();
        for (Thread consumer : consumers) consumer.start();

        // Join all threads
        producer.join();
        for (Thread consumer : consumers) consumer.join();


        // Lastly return the generated array
        return generated;
    }

    // Auxiliary function for dequeuing
    private static synchronized int dequeue(List<Integer> queue) {
        return queue.remove(0);
    }

    // Auxiliary function for enqueuing
    private static synchronized void enqueue(List<Integer> col, int num) {
        col.add(num);
    }

    // Auxiliary function to add generated strings into the array in a synchronized way
    private static synchronized void addString(List array, String number) {
        // Add the string to the array, no need for verification as the producer will send the correct amount and data
        array.add(number);
    }

    private static boolean isInRange(int count, int from, int to) {
        return from <= count && count <= to;
    }

}