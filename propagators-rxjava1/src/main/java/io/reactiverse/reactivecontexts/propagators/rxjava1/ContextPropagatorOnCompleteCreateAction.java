package io.reactiverse.reactivecontexts.propagators.rxjava1;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.propagators.rxjava1.ContextPropagatorOnSingleCreateAction.ContextCapturerSingle.OnAssemblySingleSubscriber;
import rx.Completable.OnSubscribe;
import rx.CompletableSubscriber;
import rx.Subscription;
import rx.Completable;
import rx.functions.Func1;

public class ContextPropagatorOnCompleteCreateAction implements Func1<OnSubscribe, OnSubscribe> {

	@Override
	public OnSubscribe call(OnSubscribe t) {
		return new ContextCapturerCompletable(t);
	}

	final static class ContextCapturerCompletable implements OnSubscribe {

	    final OnSubscribe source;

		private Object[] states;

	    public ContextCapturerCompletable(OnSubscribe source) {
	        this.source = source;
	        states = Context.capture();
	    }

	    @Override
	    public void call(CompletableSubscriber t) {
        	Object[] previousStates = Context.install(states);
			try {
	    		source.call(new OnAssemblyCompletableSubscriber(t, states));
			}finally {
				Context.restore(previousStates);
			}
	    }

	    static final class OnAssemblyCompletableSubscriber implements CompletableSubscriber {

	        final CompletableSubscriber actual;
			private final Object[] states;


	        public OnAssemblyCompletableSubscriber(CompletableSubscriber actual, Object[] states) {
	            this.actual = actual;
	            this.states = states;
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
	        public void onCompleted() {
	        	Object[] previousStates = Context.install(states);
				try {
					actual.onCompleted();
				}finally {
					Context.restore(previousStates);
				}
	        }

			@Override
			public void onSubscribe(Subscription d) {
	        	Object[] previousStates = Context.install(states);
				try {
					actual.onSubscribe(d);
				}finally {
					Context.restore(previousStates);
				}
			}
	    }
	}

}
