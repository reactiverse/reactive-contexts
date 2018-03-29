package fr.epardaud.reactivecontexts.propagators.rxjava2;

import fr.epardaud.reactivecontexts.core.Context;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Function;

public class ContextPropagatorOnObservableAssemblyAction implements Function<Observable, Observable> {

	@Override
	public Observable apply(Observable t) throws Exception {
		return new ContextPropagatorObservable(t);
	}

	public class ContextPropagatorObservable<T> extends Observable<T> {

		private Observable<T> source;
		private Object[] context;

		public ContextPropagatorObservable(Observable<T> t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(Observer<? super T> observer) {
			Object[] previousContext = Context.install(context);
			try {
				source.subscribe(observer);
			}finally {
				Context.restore(previousContext);
			}
		}

	}

}
