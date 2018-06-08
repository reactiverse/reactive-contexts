package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

public class ContextPropagatorOnSingleCreateAction implements BiFunction<Single, SingleObserver, SingleObserver> {

	@Override
	public SingleObserver apply(Single s, SingleObserver o) throws Exception {
		return new ContextCapturerSingle(s, o);
	}

	final static class ContextCapturerSingle<T> implements SingleObserver<T> {

	    private final SingleObserver<T> source;
		private final Object[] states;

	    public ContextCapturerSingle(Single<T> s, SingleObserver<T> o) {
	    	this.source = o;
	        this.states = Context.capture();
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
		public void onSubscribe(Disposable d) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onSubscribe(d);
			}finally {
				Context.restore(previousStates);
			}
		}

		@Override
		public void onSuccess(T v) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onSuccess(v);
			}finally {
				Context.restore(previousStates);
			}
		}
	}
}
