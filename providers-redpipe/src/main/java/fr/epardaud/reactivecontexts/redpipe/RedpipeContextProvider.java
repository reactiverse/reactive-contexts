package fr.epardaud.reactivecontexts.redpipe;

import fr.epardaud.reactivecontexts.core.ContextProvider;
import net.redpipe.engine.core.AppGlobals;

public class RedpipeContextProvider implements ContextProvider<AppGlobals> {

	@Override
	public AppGlobals install(AppGlobals state) {
		return AppGlobals.set(state);
	}

	@Override
	public void restore(AppGlobals previousState) {
		AppGlobals.set(previousState);		
	}

	@Override
	public AppGlobals capture() {
		return AppGlobals.get();
	}
}
