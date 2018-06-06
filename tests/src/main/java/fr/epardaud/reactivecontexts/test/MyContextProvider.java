package fr.epardaud.reactivecontexts.test;

import fr.epardaud.reactivecontexts.core.ContextProvider;

public class MyContextProvider implements ContextProvider<MyContext> {

	@Override
	public MyContext install(MyContext state) {
		MyContext old = MyContext.get();
		MyContext.set(state);
		return old;
	}

	@Override
	public void restore(MyContext previousState) {
		MyContext.set(previousState);
	}

	@Override
	public MyContext capture() {
		return MyContext.get();
	}

}
