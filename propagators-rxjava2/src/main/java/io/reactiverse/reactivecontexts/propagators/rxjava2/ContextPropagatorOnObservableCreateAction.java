package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

public class ContextPropagatorOnObservableCreateAction
		implements BiFunction<Observable, Observer, Observer> {

	@Override
	public Observer apply(Observable observable, Observer observer) throws Exception {
		return new ContextCapturerObservable(observable, observer);
	}

	public class ContextCapturerObservable<T> implements Observer<T> {

	    private final Observer<T> source;
		private final ContextState states;

		public ContextCapturerObservable(Observable<T> observable, Observer<T> observer) {
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
		public void onSubscribe(Disposable d) {
        	ContextState previousStates = states.install();
			try {
	    		source.onSubscribe(d);
			}finally {
				previousStates.restore();
			}
		}
	}
}
