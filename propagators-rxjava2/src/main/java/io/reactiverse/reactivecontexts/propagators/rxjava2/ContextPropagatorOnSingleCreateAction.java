package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
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
		private final ContextState states;

	    public ContextCapturerSingle(Single<T> s, SingleObserver<T> o) {
	    	this.source = o;
	        this.states = Context.capture();
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
		public void onSubscribe(Disposable d) {
        	ContextState previousStates = states.install();
			try {
	    		source.onSubscribe(d);
			}finally {
				previousStates.restore();
			}
		}

		@Override
		public void onSuccess(T v) {
        	ContextState previousStates = states.install();
			try {
	    		source.onSuccess(v);
			}finally {
				previousStates.restore();
			}
		}
	}
}
