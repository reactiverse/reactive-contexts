package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactiverse.reactivecontexts.core.ContextState;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.functions.Function;

public class ContextPropagatorOnCompletableAssemblyAction implements Function<Completable, Completable> {

	@Override
	public Completable apply(Completable t) throws Exception {
		return new ContextPropagatorCompletable(t);
	}

	public class ContextPropagatorCompletable extends Completable {

		private Completable source;
		private ContextState context;

		public ContextPropagatorCompletable(Completable t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(CompletableObserver observer) {
			ContextState previousContext = context.install();
			try {
				source.subscribe(observer);
			}finally {
				previousContext.restore();
			}
		}

	}

}
