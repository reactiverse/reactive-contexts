package io.reactiverse.reactivecontexts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
 * * <p>
 * Or you can use one of the many {@link #wrap(Runnable)} methods for wrapping reactive types:
 * </p>
 * 
 *  <pre><code>
 *  Runnable runnableWithContext = Context.wrap(() -> ...your context-requiring code...);
 *  CompletionStage&lt;String&gt; completionStageWithContext = Context.wrap(originalCompletionStage);
 *  </code></pre>

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

	/**
	 * Initialises the list of registered {@link ContextProvider} and {@link ContextPropagator}
	 * if they are not already initialised. Otherwise, has no effect.
	 */
	public static void load() {
		// does not do anything, but triggers the static block load
	}
	
	/**
	 * Wraps a {@link Runnable} so that its {@link Runnable#run()} method will
	 * be called with the current reactive context.
	 * @param f the {@link Runnable} to wrap
	 * @return a {@link Runnable} which will have its reactive context set to the current context.
	 */
	public static Runnable wrap(Runnable f) {
		return wrap(capture(), f);
	}
	
	static Runnable wrap(Object[] state, Runnable f) {
		return () -> {
			Object[] oldState = install(state);
			try {
				f.run();
			}finally {
				restore(oldState);
			}
		};
	}

	/**
	 * Wraps a {@link Consumer} so that its {@link Consumer#accept(Object)} method will
	 * be called with the current reactive context.
	 * @param f the {@link Consumer} to wrap
	 * @return a {@link Consumer} which will have its reactive context set to the current context.
	 */
	public static <T> Consumer<T> wrap(Consumer<T> f) {
		return wrap(capture(), f);
	}
	
	static <T> Consumer<T> wrap(Object[] state, Consumer<T> f) {
		return v -> {
			Object[] oldState = install(state);
			try {
				f.accept(v);
			}finally {
				restore(oldState);
			}
		};
	}

	/**
	 * Wraps a {@link BiConsumer} so that its {@link BiConsumer#accept(Object, Object)} method will
	 * be called with the current reactive context.
	 * @param f the {@link BiConsumer} to wrap
	 * @return a {@link BiConsumer} which will have its reactive context set to the current context.
	 */
	public static <T,U> BiConsumer<T,U> wrap(BiConsumer<T, U> f) {
		return wrap(capture(), f);
	}
	
	static <T, U> BiConsumer<T, U> wrap(Object[] state, BiConsumer<T, U> f) {
		return (t, u) -> {
			Object[] oldState = install(state);
			try {
				f.accept(t, u);
			}finally {
				restore(oldState);
			}
		};
	}

	/**
	 * Wraps a {@link BiFunction} so that its {@link BiFunction#apply(Object, Object)} method will
	 * be called with the current reactive context.
	 * @param f the {@link BiFunction} to wrap
	 * @return a {@link BiFunction} which will have its reactive context set to the current context.
	 */
	public static <T, U, V> BiFunction<T, U, V> wrap(BiFunction<T, U, V> fn){
		return wrap(capture(), fn);
	}

	static <T, U, V> BiFunction<T, U, V> wrap(Object[] state, BiFunction<T, U, V> fn){
		return (t, u) -> {
			Object[] oldState = install(state);
			try {
				return fn.apply(t, u);
			}finally {
				restore(oldState);
			}
		};
	}
	
	/**
	 * Wraps a {@link Function} so that its {@link Function#apply(Object)} method will
	 * be called with the current reactive context.
	 * @param f the {@link Function} to wrap
	 * @return a {@link Function} which will have its reactive context set to the current context.
	 */
	public static <T, U> Function<T, U> wrap(Function<T, U> fn){
		return wrap(capture(), fn);
	}

	static <T, U> Function<T, U> wrap(Object[] state, Function<T, U> fn){
		return v -> {
			Object[] oldState = install(state);
			try {
				return fn.apply(v);
			}finally {
				restore(oldState);
			}
		};
	}

	/**
	 * Wraps a {@link CompletableFuture} so that all its handlers
	 * are called with the current reactive context.
	 * @param f the {@link CompletableFuture} to wrap
	 * @return a {@link CompletableFuture} which will have its reactive context set to the current context.
	 */
	public static <T> CompletableFuture<T> wrap(CompletableFuture<T> f) {
		return wrap(capture(), f);
	}

	static <T> CompletableFuture<T> wrap(Object[] state, CompletableFuture<T> f) {
		return new CompletableFutureWrapper<T>(state, f);
	}

	/**
	 * Wraps a {@link CompletionStage} so that all its handlers
	 * are called with the current reactive context.
	 * @param f the {@link CompletionStage} to wrap
	 * @return a {@link CompletionStage} which will have its reactive context set to the current context.
	 */
	public static <T> CompletionStage<T> wrap(CompletionStage<T> f) {
		return wrap(capture(), f);
	}

	static <T> CompletionStage<T> wrap(Object[] state, CompletionStage<T> f) {
		return new CompletionStageWrapper<T>(state, f);
	}
}
