/**
 * <p>
 * Reactive Contexts is a library which supports automatic context propagation in Reactive libraries.
 * </p>
 * <p>
 * Many libraries (for example: RESTEasy) use a thread-local context that holds information required by user-executing
 * code (in the case of RESTEasy: the current request, response and many other useful information so that the resource 
 * method can look them up when executed).
 * </p>
 * <p>
 * This works well in settings where you have one thread per operation, but stops working if the operation spawns other 
 * threads, or gets executed in other threads, or simply later, as is the case in many Reactive libraries, such as RxJava,
 * where users code is spread between the subscriber, operations such as filter/map, and producers, all of which can be
 * executed in different thread schedulers.
 * </p>
 * <p>
 * In order to enable automatic context propagation of any number of contexts, Reactive Context uses a system of plugins
 * for saving/restoring contexts: {@link ContextProvider}, and plugins that hook into Reactive libraries/schedulers in
 * order to use the {@link Context} API for saving/restoring all contexts: {@link ContextPropagator}.
 * </p>
 * <p>
 * If your context-using framework is supported, and your Reactive library/scheduler is supported too, then all your
 * contexts will be automatically propagated and your code will look great. If not, add support for your 
 * {@link ContextProvider} or {@link ContextPropagator}. If it's not possible, you can still use manual context
 * propagation by accessing directly the {@link Context} API.
 * </p>
 *
 * @author Stéphane Épardaud
 */
package io.reactiverse.reactivecontexts.core;

