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
 * ContextState context = Context.capture();
 * // ...
 * ContextState previousContext = context.install();
 * try{
 *     // your context-requiring code
 * }finally{
 *     previousContext.restore();
 * }
 *  </code></pre>
 * <p>
 * Or you can use one of the many {@link #wrap(Runnable)} methods for wrapping reactive types:
 * </p>
 * 
 *  <pre><code>
 *  Runnable runnableWithContext = Context.wrap(() -> ...your context-requiring code...);
 *  CompletionStage&lt;String&gt; completionStageWithContext = Context.wrap(originalCompletionStage);
 *  </code></pre>
 * <h2>Threads, class loaders</h2>
 * <p>
 *  If you are using a flat classpath, this is all you need to know. If you're using a modular class loader,
 *  or need to have several independent {@link Context} objects, each with their own list of providers and 
 *  propagators, then you need to stop using the global {@link Context} instance and create your own.
 * </p>
 * <p>
 *  You can create your own Context with {@link #Context()}, then set it as a thread-local with
 *  {@link #setThreadInstance(Context)}, and when you're done you can clear the thread-local with
 *  {@link #clearThreadInstance()}.
 * </p>
 * <p>
 *  Note that each captured context state will restore the proper Context thread-local when
 *  calling {@link #install(ContextState)} and {@link #restore(ContextState)}, so as to avoid
 *  interference.
 * </p>
 * @author Stéphane Épardaud
 */
public class Context {

	private static Context instance = new Context();
	private static ThreadLocal<Context> threadInstance = new ThreadLocal<Context>();
	
	/**
	 * Returns a {@link Context} type suitable for handling Reactive Contexts. If there is a
	 * thread-local {@link Context} as installed by {@link #setThreadInstance(Context)}, the
	 * thread-local instance is returned. If not, a global shared instance is returned.
	 * @return a thread-local or global instance of {@link Context}.
	 * @see #setThreadInstance(Context)
	 */
	public static Context getInstance() {
		Context ret = threadInstance.get();
		if(ret == null)
			ret = instance;
		return ret;
	}
	
	/**
	 * Installs a {@link Context} instance to use in the current thread. It will be returned
	 * by {@link #getInstance()} within the current thread.
	 * 
	 * @param instance the {@link Context} instance to install in the current thread
	 * @return the previous instance of {@link Context} that was associated to the current thread
	 * @see #clearThreadInstance()
	 */
	public static Context setThreadInstance(Context instance) {
		Context oldInstance = threadInstance.get();
		threadInstance.set(instance);
		return oldInstance;
	}

	/**
	 * Clears the currently-associated {@link Context} instance for the current thread.
	 * @see #setThreadInstance(Context)
	 */
	public static void clearThreadInstance() {
		threadInstance.remove();
	}

	private List<ContextProvider<?>> providers = new ArrayList<>();
	private List<ContextPropagator> propagators = new ArrayList<>();
	
	/**
	 * Creates a new Context instance with the associated {@link ContextProvider} and {@link ContextPropagator}
	 * as looked up by {@link ServiceLoader#load(Class)} for the current classloader.
	 */
	public Context() {
		for (ContextProvider<?> listener : ServiceLoader.load(ContextProvider.class)) {
			providers.add(listener);
		}
		for (ContextPropagator propagator : ServiceLoader.load(ContextPropagator.class)) {
			propagators.add(propagator);
			propagator.setup();
		}
	}

	/**
	 * Captures the current state as given by the current context.
	 * @return the current context state
	 * @see #getInstance()
	 */
	public static ContextState capture() {
		return getInstance().captureState();
	}
	
	/**
	 * Captures all contexts currently registered via this {@link Context}'s {@link ContextProvider} plugins.
	 * @return the storage required for all currently registered contexts.
	 * @see #install(ContextState)
	 */
	public ContextState captureState() {
		Object[] ret = new Object[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			ContextProvider<?> plugin = providers.get(i);
			ret[i] = plugin.capture();
		}
		return new ContextState(this, ret, threadInstance.get());
	}

	/**
	 * Installs a set of contexts previously captured with {@link #captureState()} to all
	 * currently registered {@link ContextProvider} plugins in this {@link Context}.
	 * @param state the context state previously captured with {@link #captureState()}
	 * @return the (current/before installation) storage required for all currently registered contexts.
	 * @see #captureState()
	 * @see #restore(ContextState)
	 * @throws IllegalArgumentException if the state to install has not been captured by this {@link Context} instance.
	 */
	public ContextState install(ContextState state) {
		if(this != state.getContext())
			throw new IllegalArgumentException("State was captured with different context");
		Object[] oldStates = new Object[providers.size()];
		Object[] states = state.getState();
		for (int i = 0; i < providers.size(); i++) {
			@SuppressWarnings("unchecked")
			ContextProvider<Object> plugin = (ContextProvider<Object>) providers.get(i);
			oldStates[i] = plugin.install(states[i]);
		}
		return new ContextState(this, oldStates, setThreadInstance(this));
	}

	/**
	 * Restores a set of contexts previously captured with {@link #install(Object[])} to all
	 * currently registered {@link ContextProvider} plugins of this {@link Context}.
	 * @param states a set of contexts previously captured with {@link #install(ContextState)}
	 * @see #install(ContextState)
	 * @throws IllegalArgumentException if the state to install has not been captured by this {@link Context} instance.
	 */
	public void restore(ContextState state) {
		if(this != state.getContext())
			throw new IllegalArgumentException("State was captured with different context");
		Object[] states = state.getState();
		for (int i = 0; i < providers.size(); i++) {
			@SuppressWarnings("unchecked")
			ContextProvider<Object> plugin = (ContextProvider<Object>) providers.get(i);
			plugin.restore(states[i]);
		}
		Context previousThreadContext = state.getPreviousThreadContext();
		if(previousThreadContext == null)
			clearThreadInstance();
		else
			setThreadInstance(previousThreadContext);
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
	
	static Runnable wrap(ContextState state, Runnable f) {
		return () -> {
			ContextState oldState = state.install();
			try {
				f.run();
			}finally {
				oldState.restore();
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
	
	static <T> Consumer<T> wrap(ContextState state, Consumer<T> f) {
		return v -> {
			ContextState oldState = state.install();
			try {
				f.accept(v);
			}finally {
				oldState.restore();
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
	
	static <T, U> BiConsumer<T, U> wrap(ContextState state, BiConsumer<T, U> f) {
		return (t, u) -> {
			ContextState oldState = state.install();
			try {
				f.accept(t, u);
			}finally {
				oldState.restore();
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

	static <T, U, V> BiFunction<T, U, V> wrap(ContextState state, BiFunction<T, U, V> fn){
		return (t, u) -> {
			ContextState oldState = state.install();
			try {
				return fn.apply(t, u);
			}finally {
				oldState.restore();
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

	static <T, U> Function<T, U> wrap(ContextState state, Function<T, U> fn){
		return v -> {
			ContextState oldState = state.install();
			try {
				return fn.apply(v);
			}finally {
				oldState.restore();
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

	static <T> CompletableFuture<T> wrap(ContextState state, CompletableFuture<T> f) {
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

	static <T> CompletionStage<T> wrap(ContextState state, CompletionStage<T> f) {
		return new CompletionStageWrapper<T>(state, f);
	}
}
