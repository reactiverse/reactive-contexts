package io.reactiverse.reactivecontexts.core;

/**
 * <p>
 * A Context Propagator is a plugin for Reactive Contexts which can automatically propagate
 * contexts by plugging into a Reactive library or scheduler via hooks and plugins. If you get notified
 * by the target Reactive library or scheduler of when user code gets executed, you can choose to capture
 * and restore all registered contexts using the {@link Context} API.
 * </p>
 * <p>
 * For example, in RxJava, the {@link ContextPropagator} will use the existing RxJava mechanism for plugins
 * so that whenever a Reactive type is created, the contexts are associated to the Reactive type, and then
 * restored every time the Reactive type subscribers get called, thus propagating all contexts to the
 * chain of Reactive type operations.
 * </p>
 * <p>
 * In order to register your {@link ContextPropagator} you need to create a file in 
 * <tt>META-INF/services/io.reactiverse.reactivecontexts.core.ContextPropagator</tt> which contains a list
 * of fully-qualified type names of each of your implementations of {@link ContextPropagator} (one per line).
 * </p>
 *
 * @author Stéphane Épardaud
 * @see Context
 */
public interface ContextPropagator {

	/**
	 * Called by {@link Context} to initialise every propagator. Implement this method
	 * to register your propagator in whatever Reactive library/scheduler you want to propagate
	 * contexts for.
	 */
	public void setup();
}
