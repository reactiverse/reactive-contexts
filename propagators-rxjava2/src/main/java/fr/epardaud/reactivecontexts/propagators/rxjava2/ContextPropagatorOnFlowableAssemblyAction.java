package fr.epardaud.reactivecontexts.propagators.rxjava2;

import org.reactivestreams.Subscriber;

import fr.epardaud.reactivecontexts.core.Context;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class ContextPropagatorOnFlowableAssemblyAction implements Function<Flowable, Flowable> {

	@Override
	public Flowable apply(Flowable t) throws Exception {
		return new ContextPropagatorFlowable(t);
	}

	public class ContextPropagatorFlowable<T> extends Flowable<T> {

		private Flowable<T> source;
		private Object[] context;

		public ContextPropagatorFlowable(Flowable<T> t) {
			this.source = t;
			this.context = Context.capture();
		}

		@Override
		protected void subscribeActual(Subscriber<? super T> observer) {
			Object[] previousContext = Context.install(context);
			try {
				source.subscribe(observer);
			}finally {
				Context.restore(previousContext);
			}
		}

	}

}
