package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
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
		private final ContextState states;

	    public ContextCapturerCompletable(Completable s, CompletableObserver o) {
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
		public void onComplete() {
        	ContextState previousStates = states.install();
			try {
	    		source.onComplete();
			}finally {
				previousStates.restore();
			}
		}
	}

}
