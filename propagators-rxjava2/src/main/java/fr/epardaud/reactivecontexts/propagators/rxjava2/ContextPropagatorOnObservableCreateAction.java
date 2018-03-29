package fr.epardaud.reactivecontexts.propagators.rxjava2;

import fr.epardaud.reactivecontexts.core.Context;
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
		private final Object[] states;

		public ContextCapturerObservable(Observable<T> observable, Observer<T> observer) {
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
		public void onSubscribe(Disposable d) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.onSubscribe(d);
			}finally {
				Context.restore(previousStates);
			}
		}
	}
}
