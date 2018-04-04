package fr.epardaud.reactivecontexts.providers.resteasy;

import java.util.Map;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import fr.epardaud.reactivecontexts.core.ContextProvider;

public class ResteasyContextProvider implements ContextProvider<Map<Class<?>, Object>> {

	@Override
	public Map<Class<?>, Object> install(Map<Class<?>, Object> state) {
		ResteasyProviderFactory.pushContextDataMap(state);
		return null;
	}

	@Override
	public void restore(Map<Class<?>, Object> previousState) {
		ResteasyProviderFactory.removeContextDataLevel();
	}

	@Override
	public Map<Class<?>, Object> capture() {
		return ResteasyProviderFactory.getContextDataMap();
	}
}
