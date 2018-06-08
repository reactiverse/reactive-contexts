package io.reactiverse.reactivecontexts.propagators.rxjava2;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;

public class ContextPropagatorOnFlowableCreateAction
		implements BiFunction<Flowable, Subscriber, Subscriber> {

	@Override
	public Subscriber apply(Flowable flowable, Subscriber observer) throws Exception {
		return new ContextCapturerFlowable<>(flowable, observer);
	}

	public class ContextCapturerFlowable<T> implements Subscriber<T> {

	    private final Subscriber<T> source;
		private final Object[] states;

		public ContextCapturerFlowable(Flowable<T> observable, Subscriber<T> observer) {
	    	this.source = observer;
	        this.states = Context.capture();
		}

		@Override
		public void onComplete() {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onComplete();
			}finally {
				Context.restore(previousStates);
			}
		}

		@Override
		public void onError(Throwable t) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onError(t);
			}finally {
				Context.restore(previousStates);
			}
		}

		@Override
		public void onNext(T v) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onNext(v);
			}finally {
				Context.restore(previousStates);
			}
		}

		@Override
		public void onSubscribe(Subscription s) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onSubscribe(s);
			}finally {
				Context.restore(previousStates);
			}
		}
	}

}
