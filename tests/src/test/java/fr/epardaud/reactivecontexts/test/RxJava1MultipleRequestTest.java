package fr.epardaud.reactivecontexts.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.environment.se.Weld;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.epardaud.reactivecontexts.core.Context;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class RxJava1MultipleRequestTest {
	
	@RequestScoped
	public static class MyBean {
		private String reqId;
		
		public MyBean() {
		}

		public String getReqId() {
			return reqId;
		}

		public void setReqId(String reqId) {
			this.reqId = reqId;
		}
		
		@Override
		public String toString() {
			return "MyBean for "+reqId;
		}
	}

	private static Weld weld;

	@BeforeClass
	public static void init() {
		// seed
		weld = new Weld();
		weld.addBeanClass(MyBean.class);
		weld.initialize();

		// initialise
		Context.load();
	}

	@AfterClass
	public static void teardown() {
		weld.shutdown();
	}

	public Map<String, Object> newRequest(String reqId) {
		ResteasyProviderFactory.clearContextData();
		ResteasyProviderFactory.pushContext(String.class, reqId);

        BoundRequestContext cdiContext = CDI.current().select(BoundRequestContext.class).get();
        if(cdiContext.isActive()) {
        	cdiContext.dissociate(null);
        }
        HashMap<String, Object> contextMap = new HashMap<String,Object>();
        Assert.assertTrue(cdiContext.associate(contextMap));
        cdiContext.activate();
        
        MyBean myBean = CDI.current().select(MyBean.class).get();
        Assert.assertNotNull(myBean);
        Assert.assertNull(myBean.getReqId());
        myBean.setReqId(reqId);
        
        Assert.assertEquals(myBean.getReqId(), CDI.current().select(MyBean.class).get().getReqId());
        
        return contextMap;
	}
	
	public void endOfRequest(Map<String,Object> contextMap) {
		ResteasyProviderFactory.removeContextDataLevel();

        BoundRequestContext cdiContext = CDI.current().select(BoundRequestContext.class).get();
		cdiContext.invalidate();
		cdiContext.deactivate();
		cdiContext.dissociate(contextMap);
	}

	@Test
	public void testObservableOnSingleWorkerThread() throws Throwable {
		Executor myExecutor = Executors.newSingleThreadExecutor();
		testObservable(Schedulers.from(myExecutor));
	}
	
	@Test
	public void testObservableOnNewWorkerThreads() throws Throwable {
		testObservable(Schedulers.newThread());
	}
	
	private void testObservable(Scheduler scheduler) throws Throwable {
		CountDownLatch latch = new CountDownLatch(4);

		Throwable[] ret = new Throwable[1];
		
		Map<String, Object> req1Map = newRequest("req 1");
		Observable.create(emitter -> {
			checkContextCaptured("req 1");
			new Thread(() -> {
				emitter.onNext("a");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				emitter.onNext("b");
				emitter.onCompleted();
			}).start();
		}, BackpressureMode.BUFFER)
		.subscribeOn(scheduler)
		.observeOn(scheduler)
		.doOnCompleted(() -> {
			checkContextCaptured("req 1");
			endOfRequest(req1Map);
		})
		.subscribe(success -> {
			checkContextCaptured("req 1");
			latch.countDown();
		}, error -> {
			ret[0] = error;
			latch.countDown();
			latch.countDown();
		});

		Map<String, Object> req2Map = newRequest("req 2");
		Observable.create(emitter -> {
			checkContextCaptured("req 2");
			new Thread(() -> {
				emitter.onNext("a");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				emitter.onNext("b");
				emitter.onCompleted();
			}).start();
		}, BackpressureMode.BUFFER)
		.subscribeOn(scheduler)
		.observeOn(scheduler)
		.doOnCompleted(() -> {
			checkContextCaptured("req 2");
			endOfRequest(req2Map);
		})
		.subscribe(success -> {
			checkContextCaptured("req 2");
			latch.countDown();
		}, error -> {
			ret[0] = error;
			latch.countDown();
			latch.countDown();
		});

		latch.await();
		if (ret[0] != null)
			throw ret[0];
	}

	private void checkContextCaptured(String reqId) {
		Assert.assertEquals(reqId, ResteasyProviderFactory.getContextData(String.class));
        Assert.assertEquals(reqId, CDI.current().select(MyBean.class).get().getReqId());
	}
}
