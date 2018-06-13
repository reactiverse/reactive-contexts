package io.reactiverse.reactivecontexts.propagators.rxjava1;

import io.reactiverse.reactivecontexts.core.ContextPropagator;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.plugins.RxJavaHooks;

/**
 * Reactive Context propagator for RxJava 1. Supports propagating context to all {@link Single},
 * {@link Observable} and {@link Completable} types.
 *
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class RxJava1ContextPropagator implements ContextPropagator {

	public void setup() {
		RxJavaHooks.setOnSingleCreate(new ContextPropagatorOnSingleCreateAction());
		RxJavaHooks.setOnObservableCreate(new ContextPropagatorOnObservableCreateAction());
		RxJavaHooks.setOnCompletableCreate(new ContextPropagatorOnCompleteCreateAction());
	}

}
