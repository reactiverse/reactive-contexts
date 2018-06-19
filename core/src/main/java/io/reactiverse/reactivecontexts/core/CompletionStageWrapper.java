package io.reactiverse.reactivecontexts.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

final class CompletionStageWrapper<T> implements CompletionStage<T> {
	private final Object[] state;
	private final CompletionStage<T> f;

	CompletionStageWrapper(Object[] state, CompletionStage<T> f) {
		this.state = state;
		this.f = f;
	}

	@Override
	public CompletableFuture<T> toCompletableFuture() {
		return Context.wrap(f.toCompletableFuture());
	}

	@Override
	public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) {
		return Context.wrap(state, f.exceptionally(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
		return Context.wrap(state, f.handle(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
		return Context.wrap(state, f.handleAsync(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn,
			Executor executor) {
		return Context.wrap(state, f.handleAsync(Context.wrap(state, fn), executor));
	}

	@Override
	public <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> fn) {
		return Context.wrap(state, f.thenApply(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
		return Context.wrap(state, f.thenApplyAsync(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
		return Context.wrap(state, f.thenApplyAsync(Context.wrap(state, fn), executor));
	}

	@Override
	public CompletionStage<Void> thenAccept(Consumer<? super T> action) {
		return Context.wrap(state, f.thenAccept(Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action) {
		return Context.wrap(state, f.thenAcceptAsync(Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
		return Context.wrap(state, f.thenAcceptAsync(Context.wrap(state, action), executor));
	}

	@Override
	public CompletionStage<Void> thenRun(Runnable action) {
		return Context.wrap(state, f.thenRun(Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> thenRunAsync(Runnable action) {
		return Context.wrap(state, f.thenRunAsync(Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
		return Context.wrap(state, f.thenRunAsync(Context.wrap(state, action), executor));
	}

	@Override
	public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return Context.wrap(state, f.thenCombine(other, Context.wrap(state, fn)));
	}

	@Override
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return Context.wrap(state, f.thenCombineAsync(other, Context.wrap(state, fn)));
	}

	@Override
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
		return Context.wrap(state, f.thenCombineAsync(other, Context.wrap(state, fn), executor));
	}

	@Override
	public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return Context.wrap(state, f.thenAcceptBoth(other, Context.wrap(state, action)));
	}

	@Override
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return Context.wrap(state, f.thenAcceptBothAsync(other, Context.wrap(state, action)));
	}

	@Override
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action, Executor executor) {
		return Context.wrap(state, f.thenAcceptBothAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterBoth(other, Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterBothAsync(other, Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action,
			Executor executor) {
		return Context.wrap(state, f.runAfterBothAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,
			Function<? super T, U> fn) {
		return Context.wrap(state, f.applyToEither(other, Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,
			Function<? super T, U> fn) {
		return Context.wrap(state, f.applyToEitherAsync(other, Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,
			Function<? super T, U> fn, Executor executor) {
		return Context.wrap(state, f.applyToEitherAsync(other, Context.wrap(state, fn), executor));
	}

	@Override
	public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other,
			Consumer<? super T> action) {
		return Context.wrap(state, f.acceptEither(other, Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,
			Consumer<? super T> action) {
		return Context.wrap(state, f.acceptEitherAsync(other, Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,
			Consumer<? super T> action, Executor executor) {
		return Context.wrap(state, f.acceptEitherAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterEither(other, Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterEitherAsync(other, Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action,
			Executor executor) {
		return Context.wrap(state, f.runAfterEitherAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
		return Context.wrap(state, f.thenCompose(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
		return Context.wrap(state, f.thenComposeAsync(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
			Executor executor) {
		return Context.wrap(state, f.thenComposeAsync(Context.wrap(state, fn), executor));
	}

	@Override
	public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
		return Context.wrap(state, f.whenComplete(Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
		return Context.wrap(state, f.whenCompleteAsync(Context.wrap(state, action)));
	}

	@Override
	public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action,
			Executor executor) {
		return Context.wrap(state, f.whenCompleteAsync(Context.wrap(state, action), executor));
	}

	@Override
	public String toString() {
		return f.toString();
	}

	@Override
	public int hashCode() {
		return f.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return f.equals(obj);
	}
}