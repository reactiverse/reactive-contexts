# ReactiveContexts

ReactiveContexts is a library that allows you to capture contexts from various providers (Resteasy, Redpipe, Weld)
and propagate them along the reactive flow of various propagators (RxJava1, RxJava2).

# Usage

Import the following Maven module:

```xml
        <dependency>
            <groupId>fr.epardaud.reactivecontexts</groupId>
            <artifactId>reactivecontexts-core</artifactId>
            <version>0.0.1</version>
        </dependency>
```

Then call `fr.epardaud.reactivecontexts.core.Context.load();` and your contexts will be propagated, depending on the
following optional plugins:

artifactId | Description
--- | ---
`reactivecontexts-propagators-rxjava1 | Propagates contexts for RxJava1
`reactivecontexts-propagators-rxjava2 | Propagates contexts for RxJava2
`reactivecontexts-providers-redpipe | Propagates Redpipe contexts
`reactivecontexts-providers-resteasy | Propagates Resteasy contexts
`reactivecontexts-providers-weld | Propagates Weld contexts
