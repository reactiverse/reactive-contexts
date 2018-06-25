package io.reactiverse.reactivecontexts.propagators.rxjava1;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import rx.Single;
import rx.Single.OnSubscribe;
import rx.SingleSubscriber;
import rx.functions.Func1;

public class ContextPropagatorOnSingleCreateAction implements Func1<OnSubscribe, OnSubscribe> {

	@Override
	public OnSubscribe call(OnSubscribe t) {
		return new ContextCapturerSingle(t);
	}
	
	final static class ContextCapturerSingle<T> implements Single.OnSubscribe<T> {

	    final Single.OnSubscribe<T> source;

		private ContextState states;

	    public ContextCapturerSingle(Single.OnSubscribe<T> source) {
	        this.source = source;
	        states = Context.capture();
	    }

	    @Override
	    public void call(SingleSubscriber<? super T> t) {
        	ContextState previousStates = states.install();
			try {
	    		source.call(new OnAssemblySingleSubscriber<T>(t, states));
			}finally {
				previousStates.restore();
			}
	    }

	    static final class OnAssemblySingleSubscriber<T> extends SingleSubscriber<T> {

	        final SingleSubscriber<? super T> actual;
			private final ContextState states;


	        public OnAssemblySingleSubscriber(SingleSubscriber<? super T> actual, ContextState states) {
	            this.actual = actual;
	            this.states = states;
	            actual.add(this);
	        }

	        @Override
	        public void onError(Throwable e) {
	        	ContextState previousStates = states.install();
				try {
					actual.onError(e);
				}finally {
					previousStates.restore();
				}
	        }

	        @Override
	        public void onSuccess(T t) {
	        	ContextState previousStates = states.install();
				try {
					actual.onSuccess(t);
				}finally {
					previousStates.restore();
				}
	        }
	    }
	}

}
