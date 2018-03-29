package fr.epardaud.reactivecontexts.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.epardaud.reactivecontexts.core.Context;
import net.redpipe.engine.core.AppGlobals;
import rx.Completable;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.schedulers.Schedulers;

public class RxJava1Test {

	@BeforeClass
	public static void init() {
		// seed
		ResteasyProviderFactory.pushContext(String.class, "test");
		AppGlobals.set(new AppGlobals());
		AppGlobals.get().setGlobal(String.class, "test");

		// initialise
		Context.load();
	}
	
	@Test
	public void testCompletable() throws Throwable {
		// check initial state
		checkContextCaptured();
		CountDownLatch latch = new CountDownLatch(1);

		Throwable[] ret = new Throwable[1];
		Completable.create(subscriber -> {
			// check deferred state
			checkContextCaptured();
			
			subscriber.onCompleted();
		})
		.subscribeOn(Schedulers.newThread())
		.subscribe(() -> {
			latch.countDown();
		}, error -> {
			ret[0] = error;
			latch.countDown();
		});

		latch.await();
		if (ret[0] != null)
			throw ret[0];
	}

	@Test
	public void testSingle() throws Throwable {
		// check initial state
		checkContextCaptured();
		CountDownLatch latch = new CountDownLatch(1);

		Throwable[] ret = new Throwable[1];
		Single.create(subscriber -> {
			// check deferred state
			checkContextCaptured();
			
			subscriber.onSuccess("YES");
		})
		.subscribeOn(Schedulers.newThread())
		.subscribe(success -> {
			latch.countDown();
		}, error -> {
			ret[0] = error;
			latch.countDown();
		});

		latch.await();
		if (ret[0] != null)
			throw ret[0];
	}

	@Test
	public void testObservable() throws Throwable {
		// check initial state
		checkContextCaptured();
		CountDownLatch latch = new CountDownLatch(1);

		Throwable[] ret = new Throwable[1];
		Observable.create(emitter -> {
			// check deferred state
			checkContextCaptured();
			
			emitter.onNext("a");
			emitter.onCompleted();
		}, BackpressureMode.BUFFER)
		.subscribeOn(Schedulers.newThread())
		.subscribe(success -> {
			latch.countDown();
		}, error -> {
			ret[0] = error;
			latch.countDown();
		});

		latch.await();
		if (ret[0] != null)
			throw ret[0];
	}

	private void checkContextCaptured() {
		Assert.assertEquals("test", ResteasyProviderFactory.getContextData(String.class));
		Assert.assertEquals("test", AppGlobals.get().getGlobal(String.class));
	}
}
