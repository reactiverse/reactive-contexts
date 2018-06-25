package io.reactiverse.reactivecontexts.core;

/**
 * Opaque context state class which contains context state that has been captured, and can be restored.
 *
 * @see Context
 * @author Stéphane Épardaud
 */
public class ContextState {
	private Context context;
	private Context previousThreadContext;
	private Object[] state;
	
	ContextState(Context context, Object[] state, Context previousThreadContext) {
		this.context = context;
		this.state = state;
		this.previousThreadContext = previousThreadContext;
	}
	
	Context getContext() {
		return context;
	}
	
	Object[] getState() {
		return state;
	}
	
	Context getPreviousThreadContext() {
		return previousThreadContext;
	}
	
	public ContextState install() {
		return context.install(this);
	}
	
	public void restore() {
		context.restore(this);
	}
}
