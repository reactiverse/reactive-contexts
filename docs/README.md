# Reactive Contexts

[![Build Status](https://travis-ci.com/reactiverse/reactive-contexts.svg?branch=master)](https://travis-ci.com/reactiverse/reactive-contexts)

Reactive Contexts is a library that allows you to capture contexts from various providers ([RESTEasy](https://resteasy.github.io), 
[Redpipe](http://redpipe.net), [Weld](http://weld.cdi-spec.org))
and propagate them along the reactive flow of various propagators ([RxJava1, RxJava2](https://github.com/ReactiveX/RxJava)).

# The problem with contexts and reactive libraries

Many libraries (for example: RESTEasy, [CDI](http://cdi-spec.org)) use a thread-local context that holds information required by user-executing
code (in the case of RESTEasy: the current request, response and many other useful information so that the resource 
method can look them up when executed).

This works well in settings where you have one thread per operation, but stops working if the operation spawns other 
threads, or gets executed in other threads, or simply later, as is the case in many Reactive libraries, such as RxJava,
where users code is spread between the subscriber, operations such as filter/map, and producers, all of which can be
executed in different thread schedulers.

For example, the following RESTEasy/RxJava code works out of the box with the latest version of RESTEasy:

    @GET
    @Path("reactive-nodelay")
    public Single<String> reactiveNoDelay(@Context UriInfo uriInfo){
        return Single.just("hello")
                .map(str -> str + " from: "+uriInfo.getAbsolutePath());
    }

And will display something like `hello from: http://localhost:8081/reactive-nodelay`.

But if you introduce a delay, which is only one of the many ways to introduce a thread-switch from RxJava:

    @GET
    @Path("reactive-delay")
    public Single<String> reactiveDelay(@Context UriInfo uriInfo){
        return Single.just("hello")
                .delay(1, TimeUnit.SECONDS)
                .map(str -> str + " from: "+uriInfo.getAbsolutePath());
    }

Then it breaks down with `RESTEASY003880: Unable to find contextual data of type: javax.ws.rs.core.UriInfo`.

This is due to the fact that RESTEasy doesn't know that RxJava is going to schedule the delayed `map` operation
into a different scheduler, on a thread which doesn't have the RESTEasy context set-up in a thread-local.

This is not RESTEasy's fault: the exact same error will happen if you try to use CDI at that point, for the exact
same reason. Many existing libraries rely on thread-locals for context propagation, and it does not work in the
async/Reactive world.

RxJava supports a system of hooks/plugins that we can use to capture and restore context, but there can only be
one such hook/plugin, so if RESTEasy uses it, CDI cannot use it. Also, there would be a lot of code duplication
as propagating contexts with RxJava hooks/plugins is not trivial.

# The solution

In order to enable automatic context propagation of any number of contexts, Reactive Context uses a system of plugins
for saving/restoring contexts: `ContextProvider`, and plugins that hook into Reactive libraries/schedulers in
order to use the `Context` API for saving/restoring all contexts: `ContextPropagator`.

If your context-using framework is supported, and your Reactive library/scheduler is supported too, then all your
contexts will be automatically propagated and your code will look great. If not, add support for your 
`ContextProvider` or `ContextPropagator`. If it's not possible, you can still use manual context
propagation by accessing directly the `Context` API.

To get back to our original problematic code:

    @GET
    @Path("reactive-delay")
    public Single<String> reactiveDelay(@Context UriInfo uriInfo){
        return Single.just("hello")
                .delay(1, TimeUnit.SECONDS)
                .map(str -> str + " from: "+uriInfo.getAbsolutePath());
    }

Will work fine if you use the RESTEasy `ContextProvider` with the RxJava2 `ContextPropagator`, without any change
in your code. Automatic context propagation FTW!

# Usage

Note: at the moment, you have to clone and install Reactive Contexts locally, because it hasn't yet been pushed to
Maven Central.

Import the following Maven module:

```xml
<dependency>
    <groupId>io.reactiverse</groupId>
    <artifactId>reactive-contexts-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Then call `io.reactiverse.reactivecontexts.core.Context.load();` and your contexts will be propagated, depending on the
presence of the following optional plugins in your classpath:

artifactId | Description
--- | ---
`reactive-contexts-propagators-rxjava1` | Propagates contexts for RxJava1
`reactive-contexts-propagators-rxjava2` | Propagates contexts for RxJava2
`reactive-contexts-providers-redpipe` | Propagates Redpipe contexts
`reactive-contexts-providers-resteasy` | Propagates RESTEasy contexts
`reactive-contexts-providers-weld` | Propagates Weld contexts

If you are using RxJava 1 or 2, you don't need anything to propagate your contexts: every RxJava type (`Single`,
`Flowable`…) will have the right contexts automatically propagated. If you are using reactive types that don't
have a `reactive-contexts-propagator` plugin, such as `CompletionStage` in the JDK, see [below for how to manually
propagate contexts](#manual-context-propagation) 

# Building

Clone this repository, and run:

```shell
$ mvn clean install
```

# For context providers

If you have a context that your library provides and requires, which is often stored in thread-local
variables, it's very likely it won't work with reactive applications that register callbacks and
invoke them later in various threads.

In order for your library to have its context propagated to all supported reactive libraries, you
can implement the `io.reactiverse.reactivecontexts.core.ContextProvider` interface and specify how
you can save and restore your context:

```java
package my.library;

public class MyContextProvider implements ContextProvider<MyState> {

    @Override
    public MyState install(MyState state) {
        MyState previousState = MyContext.getState();
        MyContext.setState(state);
        return previousState;
    }

    @Override
    public void restore(MyState previousState) {
        MyContext.setState(previousState);
    }

    @Override
    public MyState capture() {
        return MyContext.getState();
    }
}
```

Then you declare a `META-INF/services/io.reactiverse.reactivecontexts.core.ContextProvider` file which
lists your fully-qualified class name implementing the `ContextProvider` interface (in this case
`my.library.MyContextProvider`) and include it in your classpath.

Upon initialisation, your context provider implementation will automatically be loaded and your
context will be propagated to all supported reactive libraries. 

# For context propagators

If you have a reactive library that supports scheduling of callbacks on various threads, you will need
to register a `ContextPropagator` implementation that will be called by the `reactive-contexts` library,
where you will register any required plumbing on the reactive library, to make sure it will properly
propagate all contexts during scheduling.

For example, here is how the RxJava1 propagator is implemented:

```java
public class RxJava1ContextPropagator implements ContextPropagator {

    public void setup() {
        RxJavaHooks.setOnSingleCreate(new ContextPropagatorOnSingleCreateAction());
        // ...
    }
}
```

Don't forget to list your context propagator's fully-qualified class names in the
`META-INF/services/io.reactiverse.reactivecontexts.core.ContextPropagator` file, and to include it in
your classpath.

Your plugin can capture all current contexts with `Context.capture()`, then install captured contexts with
`Context.install(contexts)` and restore them with `Context.restore(contexts)`.

For example, here is how contexts are propagated for RxJava1 `Single`:

```java
public class ContextPropagatorOnSingleCreateAction implements Func1<OnSubscribe, OnSubscribe> {

    @Override
    public OnSubscribe call(OnSubscribe t) {
        return new ContextCapturerSingle(t);
    }
    
    final static class ContextCapturerSingle<T> implements Single.OnSubscribe<T> {

        final Single.OnSubscribe<T> source;

        private Object[] states;

        public ContextCapturerSingle(Single.OnSubscribe<T> source) {
            this.source = source;
            // capture the context
            states = Context.capture();
        }

        @Override
        public void call(SingleSubscriber<? super T> t) {
            // restore the context for subscription
            Object[] previousStates = Context.install(states);
            try {
                source.call(new OnAssemblySingleSubscriber<T>(t, states));
            }finally {
                Context.restore(previousStates);
            }
        }

        static final class OnAssemblySingleSubscriber<T> extends SingleSubscriber<T> {

            final SingleSubscriber<? super T> actual;
            private final Object[] states;


            public OnAssemblySingleSubscriber(SingleSubscriber<? super T> actual, Object[] states) {
                this.actual = actual;
                this.states = states;
                actual.add(this);
            }

            @Override
            public void onError(Throwable e) {
                // propagate the context for listeners
                Object[] previousStates = Context.install(states);
                try {
                    actual.onError(e);
                }finally {
                    Context.restore(previousStates);
                }
            }

            @Override
            public void onSuccess(T t) {
                // propagate the context for listeners
                Object[] previousStates = Context.install(states);
                try {
                    actual.onSuccess(t);
                }finally {
                    Context.restore(previousStates);
                }
            }
        }
    }

}
```

## Manual context propagation

If you have a library that uses reactive types that don't support hooks, such as the JDK's `CompletionStage`,
you will have to manually capture and restore contexts, for example:

```java
CompletionStage<Response> userResponse = invokeUserAction();
Object[] states = Context.capture();
userResponse.thenAccept(response -> {
    Object[] previousStates = Context.install(states);
    try {
        writeResponse(response);
    }finally {
        Context.restore(previousStates);
    }
});
```
