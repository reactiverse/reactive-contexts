package fr.epardaud.reactivecontexts.providers.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.BoundBeanStore;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.bound.BoundRequestContext;

import fr.epardaud.reactivecontexts.core.ContextProvider;

public class CDIContextProvider implements ContextProvider<Map<String,Object>> {

	private static Method getBeanStore;
	private static Field mapBeanStoreDelegate;

	static {
		try {
			getBeanStore = AbstractBoundContext.class.getDeclaredMethod("getBeanStore");
			getBeanStore.setAccessible(true);
			mapBeanStoreDelegate = MapBeanStore.class.getDeclaredField("delegate");
			mapBeanStoreDelegate.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Map<String,Object> install(Map<String,Object> state) {
		BoundRequestContext context;
		try {
			context = CDI.current().select(BoundRequestContext.class).get();
		}catch(IllegalStateException x) {
			// no CDI
			return null;
		}
		Map<String,Object> oldStore = null;
		if(context.isActive()) {
			try {
				BoundBeanStore beanStore = (BoundBeanStore) getBeanStore.invoke(context);
				oldStore = (Map<String,Object>) mapBeanStoreDelegate.get(beanStore);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			context.deactivate();
			context.dissociate(null);
		}
		if(state != null) {
			context.associate(state);
			context.activate();
		}
		return oldStore;
	}

	@Override
	public void restore(Map<String,Object> previousState) {
		install(previousState);
	}

	@Override
	public Map<String,Object> capture() {
		BoundRequestContext context;
		try {
			context = CDI.current().select(BoundRequestContext.class).get();
		}catch(IllegalStateException x) {
			// no CDI
			return null;
		}
		try {
			BoundBeanStore beanStore = (BoundBeanStore) getBeanStore.invoke(context);
			if(beanStore == null)
				return null;
			Map<String,Object> ret = (Map<String,Object>) mapBeanStoreDelegate.get(beanStore);
			return ret;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
