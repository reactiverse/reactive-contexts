package fr.epardaud.reactivecontexts.propagators.rxjava2;

import fr.epardaud.reactivecontexts.core.ContextPropagator;
import io.reactivex.plugins.RxJavaPlugins;

public class RxJava2ContextPropagator implements ContextPropagator {

	public void setup() {
		System.err.println("Installing context propagator for RxJava2");
		RxJavaPlugins.setOnSingleSubscribe(new ContextPropagatorOnSingleCreateAction());
		RxJavaPlugins.setOnCompletableSubscribe(new ContextPropagatorOnCompletableCreateAction());
		RxJavaPlugins.setOnFlowableSubscribe(new ContextPropagatorOnFlowableCreateAction());
		RxJavaPlugins.setOnMaybeSubscribe(new ContextPropagatorOnMaybeCreateAction());
		RxJavaPlugins.setOnObservableSubscribe(new ContextPropagatorOnObservableCreateAction());
		
		RxJavaPlugins.setOnSingleAssembly(new ContextPropagatorOnSingleAssemblyAction());
		RxJavaPlugins.setOnCompletableAssembly(new ContextPropagatorOnCompletableAssemblyAction());
		RxJavaPlugins.setOnFlowableAssembly(new ContextPropagatorOnFlowableAssemblyAction());
		RxJavaPlugins.setOnMaybeAssembly(new ContextPropagatorOnMaybeAssemblyAction());
		RxJavaPlugins.setOnObservableAssembly(new ContextPropagatorOnObservableAssemblyAction());
	}

}
