package threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Task1 {

    private static final int NUM_THREADS = 10;

    public static List<String> generate(final int from, final int to, final int count) throws InterruptedException {
        if (from < 0 || to < 0 || !isInRange(count, 0, to - from + 1)) throw new IllegalArgumentException();

        List<String> generated = new ArrayList<>(count);

        // Create an array of threads
        Thread[] workers = new Thread[NUM_THREADS];
        // Loop over the array and put our threads inside with overridden run()
        for (int i = 0; i < NUM_THREADS; i++) {
            // Set each element
            workers[i] = new Thread() {
                @Override
                public void run() {
                    // Create Random class object to generate ints within bounds
                    Random randomizer = new Random();
                    while (true) {
                        // Generate a random number
                        int randomNum = randomizer.nextInt(from, to + 1);
                        // Convert the number into Japanese interpretation
                        String generatedString = randomNum + ", " + KanjiLib.convert(randomNum);
                        // If the string can't be added then the loop breaks
                        if (!addString(generated, generatedString, count)) break;
                    }
                }
            };
        }

        // Start all threads
        for (Thread thread : workers) thread.start();
        // Join all threads
        for (Thread thread : workers) thread.join();

        // When all threads finish their job then the list is returned
        return generated;
    }

    // Auxiliary function to add generated strings into the array in a synchronized way
    private static synchronized boolean addString(List array, String number, int maxSize) {
        // Check if the list contains current string or if it is not full then a new element is added and true is returned
        if (array.size() < maxSize) {
            // If
            if (!array.contains(number)) {
                array.add(number);
            }
            return true;
        }
        // If the array is full then false is returned
        return false;
    }

    private static boolean isInRange(int count, int from, int to) {
        return from <= count && count <= to;
    }

}