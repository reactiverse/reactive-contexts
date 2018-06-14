package io.reactiverse.reactivecontexts.core;

/**
 * <p>
 * A Context Provider is a plugin for Reactive Contexts which can automatically capture, install and restore
 * contexts for a library which requires a context object to be propagated.
 * </p>
 * <p>
 * For example, RESTEasy uses a thread-local context that holds the current request, response and many other
 * useful information so that the resource method can look them up when executed. This works well in settings
 * where you have one thread per operation, but stops working if the operation spawns other threads, or gets
 * executed in other threads, or simply later, as is the case in many Reactive libraries.
 * </p>
 * <p>
 * In your implementation of {@link ContextProvider} you will be responsible for telling Reactive Contexts
 * how to capture and reinstall the context for the library you want to support. In practice it will mean
 * giving access to the thread-local storage that the target library uses.
 * </p>
 * <p>
 * In order to register your {@link ContextProvider} you need to create a file in 
 * <tt>META-INF/services/io.reactiverse.reactivecontexts.core.ContextProvider</tt> which contains a list
 * of fully-qualified type names of each of your implementations of {@link ContextProvider} (one per line).
 * </p>
 *
 * @author Stéphane Épardaud
 * @see Context
 */
public interface ContextProvider<State> {

	/**
	 * Called by {@link Context#install(Object[])} to install your previously-captured state.
	 * @param state your context state, as previously captured by {@link #capture()} or {@link #install(Object)}.
	 * @return the current/previous context state, before reinstalling the new state.
	 */
	public State install(State state);
	
	/**
	 * Called by {@link Context#restore(Object[])} to install your previously-captured state.
	 * @param previousState your context state, as previously captured by {@link #capture()} or {@link #install(Object)}.
	 */
	public void restore(State previousState);

	/**
	 * Captures your current context state.
	 * @return your current context state.
	 */
	public State capture();
}
