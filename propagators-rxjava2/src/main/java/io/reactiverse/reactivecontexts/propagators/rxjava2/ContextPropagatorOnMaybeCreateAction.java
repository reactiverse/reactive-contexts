package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

public class ContextPropagatorOnMaybeCreateAction
		implements BiFunction<Maybe, MaybeObserver, MaybeObserver> {

	@Override
	public MaybeObserver apply(Maybe maybe, MaybeObserver observer) throws Exception {
		return new ContextCapturerMaybe<>(maybe, observer);
	}

	public class ContextCapturerMaybe<T> implements MaybeObserver<T> {

	    private final MaybeObserver<T> source;
		private final ContextState states;

		public ContextCapturerMaybe(Maybe<T> observable, MaybeObserver<T> observer) {
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
