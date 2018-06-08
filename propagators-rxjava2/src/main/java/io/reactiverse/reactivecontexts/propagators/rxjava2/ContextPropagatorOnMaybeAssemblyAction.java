package io.reactiverse.reactivecontexts.propagators.rxjava2;

import io.reactiverse.reactivecontexts.core.Context;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.functions.Function;

public class ContextPropagatorOnMaybeAssemblyAction implements Function<Maybe, Maybe> {

	@Override
	public Maybe apply(Maybe t) throws Exception {
		return new ContextPropagatorMaybe(t);
	}

	public class ContextPropagatorMaybe<T> extends Maybe<T> {

		private Maybe<T> source;
		private Object[] context;

		public ContextPropagatorMaybe(Maybe<T> t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(MaybeObserver<? super T> observer) {
			Object[] previousContext = Context.install(context);
			try {
				source.subscribe(observer);
			}finally {
				Context.restore(previousContext);
			}
		}

	}

}
