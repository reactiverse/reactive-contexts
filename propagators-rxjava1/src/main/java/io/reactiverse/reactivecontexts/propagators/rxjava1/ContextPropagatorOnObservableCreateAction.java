package io.reactiverse.reactivecontexts.propagators.rxjava1;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Producer;
import rx.Subscriber;
import rx.functions.Func1;

public class ContextPropagatorOnObservableCreateAction implements Func1<OnSubscribe, OnSubscribe> {

	@Override
	public OnSubscribe call(OnSubscribe t) {
		return new ContextCapturerObservable(t);
	}

	final static class ContextCapturerObservable<T> implements Observable.OnSubscribe<T> {

	    final Observable.OnSubscribe<T> source;

		private ContextState states;

	    public ContextCapturerObservable(Observable.OnSubscribe<T> source) {
	        this.source = source;
	        states = Context.capture();
	    }

		@Override
		public void call(Subscriber<? super T> t) {
			ContextState previousStates = states.install();
			try {
	    		source.call(new OnAssemblyObservableSubscriber<T>(t, states));
			}finally {
				previousStates.restore();
			}

		}

	    static final class OnAssemblyObservableSubscriber<T> extends Subscriber<T> {

	        final Subscriber<? super T> actual;
			private final ContextState states;

	        public OnAssemblyObservableSubscriber(Subscriber<? super T> actual, ContextState states) {
	        	super(actual);
	            this.actual = actual;
	            this.states = states;
	            actual.add(this);
	        }

	        @Override
	        public void onError(Throwable e) {
	        	ContextState previousStates = states.install();
				try {
					actual.onError(e);
				}finally {
					previousStates.restore();
				}
	        }

	        @Override
	        public void onNext(T t) {
	        	ContextState previousStates = states.install();
				try {
					actual.onNext(t);
				}finally {
					previousStates.restore();
				}
	        }

	        @Override
	        public void onCompleted() {
	        	ContextState previousStates = states.install();
				try {
					actual.onCompleted();
				}finally {
					previousStates.restore();
				}
	        }

            @Override
            public void setProducer(Producer p) {
	        	if (p.getClass().getSimpleName().startsWith("OnSubscribeConcatMap")) {
					p.request(2);
				} else {
	        		super.setProducer(p);
				}
            }
        }
	}
}
