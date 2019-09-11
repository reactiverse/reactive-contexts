package io.reactiverse.reactivecontexts.test;

import io.reactiverse.reactivecontexts.core.Context;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.observers.AssertableSubscriber;

import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static rx.Observable.from;
import static rx.Observable.just;

public class BackPressureExceptionTest {

    @BeforeClass
    public static void before() {
        Context.getInstance();
    }


    @Test
    public void testBackPressure() {
        AssertableSubscriber<Integer> test = from(asList(1,2,3,4,5,6))
            .concatMap(integer -> just(integer).delay(100, TimeUnit.MILLISECONDS)).test();

        test.awaitTerminalEvent();

        System.out.println("Get on next events: " + test.getOnNextEvents().size());
        System.out.println("Get on error events: " + test.getOnErrorEvents().size());
        System.out.println("Error: " + test.getOnErrorEvents().get(0));
    }
}
