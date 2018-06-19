package io.reactiverse.reactivecontexts.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

final class CompletableFutureWrapper<T> extends CompletableFuture<T> {
	private final Object[] state;
	private final CompletableFuture<T> f;

	CompletableFutureWrapper(Object[] state, CompletableFuture<T> f) {
		this.state = state;
		this.f = f;
	}

	@Override
	public boolean complete(T value) {
		return f.complete(value);
	}

	@Override
	public boolean completeExceptionally(Throwable ex) {
		return f.completeExceptionally(ex);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return f.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return f.isCancelled();
	}

	@Override
	public boolean isCompletedExceptionally() {
		return f.isCompletedExceptionally();
	}

	@Override
	public void obtrudeValue(T value) {
		f.obtrudeValue(value);
	}

	@Override
	public void obtrudeException(Throwable ex) {
		f.obtrudeException(ex);
	}

	@Override
	public int getNumberOfDependents() {
		return f.getNumberOfDependents();
	}

	@Override
	public boolean isDone() {
		return f.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return f.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return f.get(timeout, unit);
	}

	@Override
	public T join() {
		return f.join();
	}

	@Override
	public T getNow(T valueIfAbsent) {
		return f.getNow(valueIfAbsent);
	}

	@Override
	public CompletableFuture<T> toCompletableFuture() {
		return this;
	}

	@Override
	public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
		return Context.wrap(state, f.exceptionally(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
		return Context.wrap(state, f.handle(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
		return Context.wrap(state, f.handleAsync(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn,
			Executor executor) {
		return Context.wrap(state, f.handleAsync(Context.wrap(state, fn), executor));
	}

	@Override
	public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
		return Context.wrap(state, f.thenApply(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
		return Context.wrap(state, f.thenApplyAsync(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
		return Context.wrap(state, f.thenApplyAsync(Context.wrap(state, fn), executor));
	}

	@Override
	public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
		return Context.wrap(state, f.thenAccept(Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
		return Context.wrap(state, f.thenAcceptAsync(Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
		return Context.wrap(state, f.thenAcceptAsync(Context.wrap(state, action), executor));
	}

	@Override
	public CompletableFuture<Void> thenRun(Runnable action) {
		return Context.wrap(state, f.thenRun(Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> thenRunAsync(Runnable action) {
		return Context.wrap(state, f.thenRunAsync(Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
		return Context.wrap(state, f.thenRunAsync(Context.wrap(state, action), executor));
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return Context.wrap(state, f.thenCombine(other, Context.wrap(state, fn)));
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return Context.wrap(state, f.thenCombineAsync(other, Context.wrap(state, fn)));
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
		return Context.wrap(state, f.thenCombineAsync(other, Context.wrap(state, fn), executor));
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return Context.wrap(state, f.thenAcceptBoth(other, Context.wrap(state, action)));
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return Context.wrap(state, f.thenAcceptBothAsync(other, Context.wrap(state, action)));
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action, Executor executor) {
		return Context.wrap(state, f.thenAcceptBothAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterBoth(other, Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterBothAsync(other, Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action,
			Executor executor) {
		return Context.wrap(state, f.runAfterBothAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other,
			Function<? super T, U> fn) {
		return Context.wrap(state, f.applyToEither(other, Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other,
			Function<? super T, U> fn) {
		return Context.wrap(state, f.applyToEitherAsync(other, Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other,
			Function<? super T, U> fn, Executor executor) {
		return Context.wrap(state, f.applyToEitherAsync(other, Context.wrap(state, fn), executor));
	}

	@Override
	public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other,
			Consumer<? super T> action) {
		return Context.wrap(state, f.acceptEither(other, Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other,
			Consumer<? super T> action) {
		return Context.wrap(state, f.acceptEitherAsync(other, Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other,
			Consumer<? super T> action, Executor executor) {
		return Context.wrap(state, f.acceptEitherAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterEither(other, Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
		return Context.wrap(state, f.runAfterEitherAsync(other, Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action,
			Executor executor) {
		return Context.wrap(state, f.runAfterEitherAsync(other, Context.wrap(state, action), executor));
	}

	@Override
	public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
		return Context.wrap(state, f.thenCompose(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
		return Context.wrap(state, f.thenComposeAsync(Context.wrap(state, fn)));
	}

	@Override
	public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
			Executor executor) {
		return Context.wrap(state, f.thenComposeAsync(Context.wrap(state, fn), executor));
	}

	@Override
	public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
		return Context.wrap(state, f.whenComplete(Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
		return Context.wrap(state, f.whenCompleteAsync(Context.wrap(state, action)));
	}

	@Override
	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action,
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