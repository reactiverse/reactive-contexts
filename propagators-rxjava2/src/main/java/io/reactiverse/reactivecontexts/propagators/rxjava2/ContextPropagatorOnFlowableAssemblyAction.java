package io.reactiverse.reactivecontexts.propagators.rxjava2;

import org.reactivestreams.Subscriber;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class ContextPropagatorOnFlowableAssemblyAction implements Function<Flowable, Flowable> {

	@Override
	public Flowable apply(Flowable t) throws Exception {
		return new ContextPropagatorFlowable(t);
	}

	public class ContextPropagatorFlowable<T> extends Flowable<T> {

		private Flowable<T> source;
		private ContextState context;

		public ContextPropagatorFlowable(Flowable<T> t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(Subscriber<? super T> observer) {
			ContextState previousContext = context.install();
			try {
				source.subscribe(observer);
			}finally {
				previousContext.restore();
			}
		}

	}

}
