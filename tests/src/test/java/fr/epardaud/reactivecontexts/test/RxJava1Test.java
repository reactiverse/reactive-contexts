package fr.epardaud.reactivecontexts.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.environment.se.Weld;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.epardaud.reactivecontexts.core.Context;
import net.redpipe.engine.core.AppGlobals;
import rx.Completable;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

public class RxJava1Test {
	
	@RequestScoped
	public static class MyBean {
		public MyBean() {
			System.err.println("Creating MyBean");
		}
		
		public long identity() {
			return System.identityHashCode(this);
		}
	}

	private static Weld weld;

	@BeforeClass
	public static void init() {
		// seed
		ResteasyProviderFactory.pushContext(String.class, "test");
		AppGlobals.set(new AppGlobals());
		AppGlobals.get().setGlobal(String.class, "test");
		weld = new Weld();
		weld.addBeanClass(MyBean.class);
		weld.initialize();
		// initialise
		Context.load();
	}
	
	@AfterClass
	public static void shutdown() {
		weld.shutdown();
	}

	private BoundRequestContext cdiContext;
	private Map<String, Object> contextMap;
	private MyBean myBean;
	
	@Before
	public void before() {
        cdiContext = CDI.current().select(BoundRequestContext.class).get();
        contextMap = new HashMap<String,Object>();
        cdiContext.associate(contextMap);
        cdiContext.activate();
        
        myBean = CDI.current().select(MyBean.class).get();
        Assert.assertNotNull(myBean);
        
        Assert.assertEquals(myBean.identity(), CDI.current().select(MyBean.class).get().identity());
	}
	
	@After
	public void after() {
		cdiContext.invalidate();
		cdiContext.deactivate();
		cdiContext.dissociate(contextMap);
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
        Assert.assertEquals(myBean.identity(), CDI.current().select(MyBean.class).get().identity());
	}
}
