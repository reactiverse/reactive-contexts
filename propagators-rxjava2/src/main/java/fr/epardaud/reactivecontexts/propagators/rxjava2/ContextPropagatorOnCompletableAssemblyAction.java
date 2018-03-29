package fr.epardaud.reactivecontexts.propagators.rxjava2;

import fr.epardaud.reactivecontexts.core.Context;
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
		private Object[] context;

		public ContextPropagatorCompletable(Completable t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(CompletableObserver observer) {
			Object[] previousContext = Context.install(context);
			try {
				source.subscribe(observer);
			}finally {
				Context.restore(previousContext);
			}
		}

	}

}
