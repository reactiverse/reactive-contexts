package fr.epardaud.reactivecontexts.propagators.rxjava1;

import fr.epardaud.reactivecontexts.core.Context;
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

		private Object[] states;

	    public ContextCapturerSingle(Single.OnSubscribe<T> source) {
	        this.source = source;
	        states = Context.capture();
	    }

	    @Override
	    public void call(SingleSubscriber<? super T> t) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.call(new OnAssemblySingleSubscriber<T>(t, states));
			}finally {
				Context.restore(previousStates);
			}
	    }

	    static final class OnAssemblySingleSubscriber<T> extends SingleSubscriber<T> {

	        final SingleSubscriber<? super T> actual;
			private final Object[] states;


	        public OnAssemblySingleSubscriber(SingleSubscriber<? super T> actual, Object[] states) {
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
	        public void onSuccess(T t) {
	        	Object[] previousStates = Context.install(states);
				try {
					actual.onSuccess(t);
				}finally {
					Context.restore(previousStates);
				}
	        }
	    }
	}

}
