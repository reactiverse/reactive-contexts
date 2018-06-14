package io.reactiverse.reactivecontexts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * <p>
 * Main class for initialising the list of {@link ContextPropagator} and {@link ContextProvider}.
 * </p>
 * <p>
 * Upon startup, you should initialise Reactive Contexts by calling {@link #load()}, which
 * will initialise all {@link ContextPropagator} and {@link ContextProvider} via the {@link ServiceLoader}
 * mechanism.
 * </p>
 * <p>
 * If you don't have an automatic {@link ContextPropagator}, you can then manually capture contexts with 
 * {@link #capture()}, and then surround your context-requiring code with:
 * </p>
 * 
 *  <pre><code>
 * Object[] contexts = Context.capture();
 * try{
 *     // your context-requiring code
 * }finally{
 *     Context.restore(contexts);
 * }
 *  </code></pre>
 *
 * @author Stéphane Épardaud
 */
public class Context {
	
	//
	// Helpers
	
	private static List<ContextProvider<?>> providers = new ArrayList<>();
	private static List<ContextPropagator> propagators = new ArrayList<>();
	
	static {
		for (ContextProvider<?> listener : ServiceLoader.load(ContextProvider.class)) {
			providers.add(listener);
		}
		for (ContextPropagator propagator : ServiceLoader.load(ContextPropagator.class)) {
			propagators.add(propagator);
			propagator.setup();
		}
	}
	
	/**
	 * Captures all contexts currently registered via {@link ContextProvider} plugins.
	 * @return the storage required for all currently registered contexts.
	 * @see #install(Object[])
	 */
	public static Object[] capture() {
		Object[] ret = new Object[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			ContextProvider<?> plugin = providers.get(i);
			ret[i] = plugin.capture();
		}
		return ret;
	}

	/**
	 * Installs a set of contexts previously captured with {@link #capture()} to all
	 * currently registered {@link ContextProvider} plugins.
	 * @param states the set of contexts previously captured with {@link #capture()}
	 * @return the (current/before installation) storage required for all currently registered contexts.
	 * @see #capture()
	 * @see #restore(Object[])
	 */
	public static Object[] install(Object[] states) {
		Object[] ret = new Object[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			@SuppressWarnings("unchecked")
			ContextProvider<Object> plugin = (ContextProvider<Object>) providers.get(i);
			ret[i] = plugin.install(states[i]);
		}
		return ret;
	}

	/**
	 * Restores a set of contexts previously captured with {@link #install(Object[])} to all
	 * currently registered {@link ContextProvider} plugins.
	 * @param states a set of contexts previously captured with {@link #install(Object[])}
	 * @see #install(Object[])
	 */
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
