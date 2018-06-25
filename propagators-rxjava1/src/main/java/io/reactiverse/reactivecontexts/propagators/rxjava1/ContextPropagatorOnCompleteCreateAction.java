package io.reactiverse.reactivecontexts.propagators.rxjava1;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import rx.Completable.OnSubscribe;
import rx.CompletableSubscriber;
import rx.Subscription;
import rx.functions.Func1;

public class ContextPropagatorOnCompleteCreateAction implements Func1<OnSubscribe, OnSubscribe> {

	@Override
	public OnSubscribe call(OnSubscribe t) {
		return new ContextCapturerCompletable(t);
	}

	final static class ContextCapturerCompletable implements OnSubscribe {

	    final OnSubscribe source;

		private ContextState states;

	    public ContextCapturerCompletable(OnSubscribe source) {
	        this.source = source;
	        states = Context.capture();
	    }

	    @Override
	    public void call(CompletableSubscriber t) {
        	ContextState previousStates = states.install();
			try {
	    		source.call(new OnAssemblyCompletableSubscriber(t, states));
			}finally {
				previousStates.restore();
			}
	    }

	    static final class OnAssemblyCompletableSubscriber implements CompletableSubscriber {

	        final CompletableSubscriber actual;
			private final ContextState states;


	        public OnAssemblyCompletableSubscriber(CompletableSubscriber actual, ContextState states) {
	            this.actual = actual;
	            this.states = states;
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
	        public void onCompleted() {
	        	ContextState previousStates = states.install();
				try {
					actual.onCompleted();
				}finally {
					previousStates.restore();
				}
	        }

			@Override
			public void onSubscribe(Subscription d) {
				ContextState previousStates = states.install();
				try {
					actual.onSubscribe(d);
				}finally {
					previousStates.restore();
				}
			}
	    }
	}

}
