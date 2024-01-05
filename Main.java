import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Main {

    private static final int THREADS = 16;
    private static final CountDownLatch latch = new CountDownLatch(THREADS);
    private static final CyclicBarrier cyclicBarrier = new CyclicBarrier(THREADS);
    private static final Set<Integer> objectHashcodes = new CopyOnWriteArraySet<>();

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        Stream.generate(() -> (Runnable) () -> {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            objectHashcodes.add(ThreadUnsafeSingleton.getInstance().hashCode());
            latch.countDown();
        }).limit(THREADS).forEach(executorService::submit);
        latch.await();
        if (objectHashcodes.size() > 1) {
            System.out.printf("This implementation is not thread-safe and created [%s] instances with [%d] concurrent threads!%n", objectHashcodes.size(), THREADS);
        } else {
            System.out.println("This implementation is thread-safe!");
        }
        executorService.shutdown();
    }

    static class ThreadUnsafeSingleton {
        private static volatile ThreadUnsafeSingleton instance;

        private ThreadUnsafeSingleton() {
        }

        static ThreadUnsafeSingleton getInstance() {
            if (instance == null) {
                instance = new ThreadUnsafeSingleton();
            }
            return instance;
        }
    }
}
