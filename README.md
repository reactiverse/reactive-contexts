# Reactive Contexts

[![Build Status](https://travis-ci.com/reactiverse/reactive-contexts.svg?branch=master)](https://travis-ci.com/reactiverse/reactive-contexts)

ReactiveContexts is a library that allows you to capture contexts from various providers (Resteasy, Redpipe, Weld)
and propagate them along the reactive flow of various propagators (RxJava1, RxJava2).

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
`reactive-contexts-providers-resteasy` | Propagates Resteasy contexts
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
