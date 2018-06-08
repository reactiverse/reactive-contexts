package io.reactiverse.reactivecontexts.core;

public interface ContextProvider<State> {

	public State install(State state);
	
	public void restore(State previousState);
	
	public State capture();
}
