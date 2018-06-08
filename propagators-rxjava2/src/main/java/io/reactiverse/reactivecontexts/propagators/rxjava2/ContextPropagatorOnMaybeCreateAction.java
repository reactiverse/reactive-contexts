package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
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
		private final Object[] states;

		public ContextCapturerMaybe(Maybe<T> observable, MaybeObserver<T> observer) {
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
