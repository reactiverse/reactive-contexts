package io.reactiverse.reactivecontexts.propagators.rxjava2;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
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
		private final ContextState states;

		public ContextCapturerFlowable(Flowable<T> observable, Subscriber<T> observer) {
	    	this.source = observer;
	        this.states = Context.capture();
		}

		@Override
		public void onComplete() {
        	ContextState previousStates = states.install();
			try {
	    		source.onComplete();
			}finally {
				previousStates.restore();
			}
		}

		@Override
		public void onError(Throwable t) {
        	ContextState previousStates = states.install();
			try {
	    		source.onError(t);
			}finally {
				previousStates.restore();
			}
		}

		@Override
		public void onNext(T v) {
        	ContextState previousStates = states.install();
			try {
	    		source.onNext(v);
			}finally {
				previousStates.restore();
			}
		}

		@Override
		public void onSubscribe(Subscription s) {
        	ContextState previousStates = states.install();
			try {
	    		source.onSubscribe(s);
			}finally {
				previousStates.restore();
			}
		}
	}

}
