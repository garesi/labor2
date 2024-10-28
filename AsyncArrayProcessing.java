import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

public class AsyncArrayProcessing {

    // Метод для генерації масиву випадкових чисел
    public static int[] generateArray(int size, int min, int max) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(max - min + 1) + min;
        }
        return array;
    }

    // Основний метод запуску програми
    public static void main(String[] args) {
        int arraySize = 50; // Розмір масиву, можна змінити за потребою
        int min = 0;        // Мінімальне значення діапазону
        int max = 1000;     // Максимальне значення діапазону

        int[] array = generateArray(arraySize, min, max);
        int chunkSize = 10; // Розмір частини масиву для обробки в окремому потоці

        // Асинхронна обробка масиву з використанням Future
        processArrayInChunks(array, chunkSize);

        // Обчислення середнього значення масиву
        calculateAverageAsync(array, chunkSize);
    }

    // Метод для асинхронної обробки масиву по частинах
    public static void processArrayInChunks(int[] array, int chunkSize) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Integer>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // Розбиваємо масив на частини та передаємо їх для обробки в окремих потоках
        for (int i = 0; i < array.length; i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, array.length);
            Future<Integer> future = executor.submit(() -> {
                int sum = 0;
                for (int j = start; j < end; j++) {
                    sum += array[j];
                }
                return sum;
            });
            futures.add(future);
        }

        // Очікуємо завершення задач та збираємо результати
        int totalSum = 0;
        for (Future<Integer> future : futures) {
            try {
                if (!future.isCancelled() && future.isDone()) {
                    totalSum += future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();
        System.out.println("Загальна сума масиву: " + totalSum);
        System.out.println("Час виконання обробки по частинах: " + (endTime - startTime) + " мс");
    }

    // Метод для асинхронного обчислення середнього значення масиву
    public static void calculateAverageAsync(int[] array, int chunkSize) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Set<Double> results = new CopyOnWriteArraySet<>();
        List<Future<Double>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // Розділення масиву на частини та асинхронна передача їх для обчислення
        for (int i = 0; i < array.length; i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, array.length);
            Future<Double> future = executor.submit(() -> {
                double sum = 0;
                for (int j = start; j < end; j++) {
                    sum += array[j];
                }
                double average = sum / (end - start);
                results.add(average);
                return average;
            });
            futures.add(future);
        }

        // Перевіряємо статус виконання та отримуємо середні значення
        double globalSum = 0;
        int count = 0;
        for (Future<Double> future : futures) {
            try {
                if (!future.isCancelled() && future.isDone()) {
                    double average = future.get();
                    globalSum += average * chunkSize;
                    count += chunkSize;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        double globalAverage = globalSum / count;
        long endTime = System.currentTimeMillis();
        System.out.println("Середнє значення масиву: " + globalAverage);
        System.out.println("Час виконання обчислення середнього: " + (endTime - startTime) + " мс");
    }
}
