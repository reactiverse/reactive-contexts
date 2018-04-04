package fr.epardaud.reactivecontexts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class Context {
	
	//
	// Helpers
	
	private static List<ContextProvider<?>> providers = new ArrayList<>();
	private static List<ContextPropagator> propagators = new ArrayList<>();
	
	static {
		for (ContextProvider<?> listener : ServiceLoader.load(ContextProvider.class)) {
			providers.add(listener);
			System.err.println("Context provider: "+listener);
		}
		for (ContextPropagator propagator : ServiceLoader.load(ContextPropagator.class)) {
			propagators.add(propagator);
			propagator.setup();
		}
	}
	
	public static Object[] capture() {
		Object[] ret = new Object[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			ContextProvider<?> plugin = providers.get(i);
			ret[i] = plugin.capture();
		}
		return ret;
	}

	public static Object[] install(Object[] states) {
		Object[] ret = new Object[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			@SuppressWarnings("unchecked")
			ContextProvider<Object> plugin = (ContextProvider<Object>) providers.get(i);
			ret[i] = plugin.install(states[i]);
		}
		return ret;
	}

	public static void restore(Object[] states) {
		for (int i = 0; i < providers.size(); i++) {
			@SuppressWarnings("unchecked")
			ContextProvider<Object> plugin = (ContextProvider<Object>) providers.get(i);
			plugin.restore(states[i]);
		}
	}

	public static void load() {
		// does not do anything, but triggers the static block load
	}
}
