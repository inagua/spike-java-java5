package ch.inagua.spike.java.java5.executor.future;

import org.junit.Test;

import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 */
public class FutureTest {

    @Test
    public void test_get() throws Exception {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> result = executor.submit(new Callable<Integer>() {
            public Integer call() throws Exception {
                Thread.sleep(1000);
                return Integer.valueOf(10);
            }
        });
        executor.shutdown();

        int loops = 0;
        long debut = System.currentTimeMillis();
        while (!result.isDone()) {
            // System.out.printf("attente (%d ms)%n", System.currentTimeMillis() - debut);
            loops++;
            try {
                Thread.sleep(205);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // System.out.printf("Resultat (%d ms)%n", System.currentTimeMillis() - debut);
        long duration = System.currentTimeMillis() - debut;
        assertThat(result.get(), equalTo(10));
        assertThat(loops, equalTo(5));
        assertThat(duration, is(both(greaterThan(900L)).and(lessThan(1300L))));
    }

    @Test(expected = TimeoutException.class)
    public void test_get_withTimeout() throws Exception {
        final long timeout = 500L;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> result = executor.submit(new Callable<Integer>() {
            public Integer call() throws Exception {
                Thread.sleep(2 * timeout);
                return Integer.valueOf(10);
            }
        });
        executor.shutdown();

        result.get(timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    public void test_get_withCancel() throws Exception {

        class Container {
            public long count = 0;
        }

        final Container container = new Container();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> result = executor.submit(new Callable<Long>() {
            public Long call() throws Exception {
                container.count = 0;
                while (container.count < Long.MAX_VALUE) {
                    container.count++;
                }
                return container.count;
            }
        });
        executor.shutdown();

        long debut = System.currentTimeMillis();
        try {
            result.get(1L, TimeUnit.MILLISECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - debut;

            assertThat(result.isDone(), is(false));
            assertThat(duration, is(lessThan(5L)));

            result.cancel(true);
            assertThat(result.isDone(), is(true));
            assertThat(container.count, is(lessThan(Long.MAX_VALUE / 10)));
            System.out.printf("container.count=" + container.count);
        }
    }

    @Test
    public void test_get_withCancel2() throws Exception {

        class Container {
            public long count = 0;
        }

        final Container container = new Container();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> result = executor.submit(new Callable<Long>() {
            public Long call() throws Exception {
                container.count = 0;
                while (container.count < Long.MAX_VALUE && !Thread.currentThread().isInterrupted()) {
                    container.count++;
                }
                return container.count;
            }
        });
        executor.shutdown();

        long debut = System.currentTimeMillis();
        try {
            result.get(1L, TimeUnit.MILLISECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - debut;

            assertThat(result.isDone(), is(false));
            assertThat(duration, is(lessThan(5L)));

            result.cancel(true);
            assertThat(result.isDone(), is(true));
            assertThat(container.count, is(lessThan(Long.MAX_VALUE / 10)));

            System.out.printf("container.count=" + container.count);

        }
    }
}