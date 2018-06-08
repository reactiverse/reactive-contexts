package io.reactiverse.reactivecontexts.propagators.rxjava1;

import io.reactiverse.reactivecontexts.core.ContextPropagator;
import rx.plugins.RxJavaHooks;

public class RxJava1ContextPropagator implements ContextPropagator {

	public void setup() {
		System.err.println("Installing context propagator for RxJava1");
		RxJavaHooks.setOnSingleCreate(new ContextPropagatorOnSingleCreateAction());
		RxJavaHooks.setOnObservableCreate(new ContextPropagatorOnObservableCreateAction());
		RxJavaHooks.setOnCompletableCreate(new ContextPropagatorOnCompleteCreateAction());
	}

}
