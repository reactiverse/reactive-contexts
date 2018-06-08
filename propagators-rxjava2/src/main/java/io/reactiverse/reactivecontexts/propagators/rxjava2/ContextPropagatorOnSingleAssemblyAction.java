package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.functions.Function;

public class ContextPropagatorOnSingleAssemblyAction implements Function<Single, Single> {

	@Override
	public Single apply(Single t) throws Exception {
		return new ContextPropagatorSingle(t);
	}

	public class ContextPropagatorSingle<T> extends Single<T> {

		private Single<T> source;
		private Object[] context;

		public ContextPropagatorSingle(Single<T> t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(SingleObserver<? super T> observer) {
			Object[] previousContext = Context.install(context);
			try {
				source.subscribe(observer);
			}finally {
				Context.restore(previousContext);
			}
		}

	}

}
