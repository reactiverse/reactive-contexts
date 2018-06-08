package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

public class ContextPropagatorOnCompletableCreateAction
		implements BiFunction<Completable, CompletableObserver, CompletableObserver> {

	@Override
	public CompletableObserver apply(Completable completable, CompletableObserver observer) throws Exception {
		return new ContextCapturerCompletable(completable, observer);
	}

	final static class ContextCapturerCompletable implements CompletableObserver {

	    private final CompletableObserver source;
		private final Object[] states;

	    public ContextCapturerCompletable(Completable s, CompletableObserver o) {
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
		public void onComplete() {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onComplete();
			}finally {
				Context.restore(previousStates);
			}
		}
	}

}
