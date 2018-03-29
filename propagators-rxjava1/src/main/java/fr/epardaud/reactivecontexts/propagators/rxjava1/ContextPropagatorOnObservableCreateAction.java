package fr.epardaud.reactivecontexts.propagators.rxjava1;

import fr.epardaud.reactivecontexts.core.Context;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

public class ContextPropagatorOnObservableCreateAction implements Func1<OnSubscribe, OnSubscribe> {

	@Override
	public OnSubscribe call(OnSubscribe t) {
		return new ContextCapturerObservable(t);
	}
	
	final static class ContextCapturerObservable<T> implements Observable.OnSubscribe<T> {

	    final Observable.OnSubscribe<T> source;

		private Object[] states;

	    public ContextCapturerObservable(Observable.OnSubscribe<T> source) {
	        this.source = source;
	        states = Context.capture();
	    }

		@Override
		public void call(Subscriber<? super T> t) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.call(new OnAssemblyObservableSubscriber<T>(t, states));
			}finally {
				Context.restore(previousStates);
			}
			
		}

	    static final class OnAssemblyObservableSubscriber<T> extends Subscriber<T> {

	        final Subscriber<? super T> actual;
			private final Object[] states;


	        public OnAssemblyObservableSubscriber(Subscriber<? super T> actual, Object[] states) {
	            this.actual = actual;
	            this.states = states;
	            actual.add(this);
	        }

	        @Override
	        public void onError(Throwable e) {
	        	Object[] previousStates = Context.install(states);
				try {
					actual.onError(e);
				}finally {
					Context.restore(previousStates);
				}
	        }

	        @Override
	        public void onNext(T t) {
	        	Object[] previousStates = Context.install(states);
				try {
					actual.onNext(t);
				}finally {
					Context.restore(previousStates);
				}
	        }

	        @Override
	        public void onCompleted() {
	        	Object[] previousStates = Context.install(states);
				try {
					actual.onCompleted();
				}finally {
					Context.restore(previousStates);
				}
	        }
}
	}

}
