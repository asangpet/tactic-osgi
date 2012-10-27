package ak.tactic.model.simulator.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bus {
	private final Logger logger = LoggerFactory.getLogger(Bus.class);
	private final ConcurrentHashMap<Class<?>, List<Invoker>> map = new ConcurrentHashMap<>();
	private final ExecutorService executor = Executors.newFixedThreadPool(4); 
	
	private class Invoker {
		Method method;
		Object sourceObject;
		
		public Invoker(Object source, Method method) {
			this.sourceObject = source;
			this.method = method;
		}
	}
	
	public void register(Object object) {
		for (Method method : object.getClass().getMethods()) {
			Subscribe subscribe = method.getAnnotation(Subscribe.class);
			if (subscribe != null) {
				Class<?> param = method.getParameterTypes()[0];
				List<Invoker> methodList = map.get(param);
				if (methodList == null) {
					methodList = new ArrayList<Invoker>();
					map.put(param, methodList);
				}
				logger.info("Event {} has handler {}",param,method);
				methodList.add(new Invoker(object, method));
			}
		}
	}
	
	public void post(Object object) {
		List<Invoker> invokers = map.get(object.getClass());
		if (invokers == null) {
			logger.warn("No handler registered for event {}", object);
			return;
		}
		for (Invoker invoker : invokers) {
			asyncDispatch(invoker, object);
		}
	}
	
	private void asyncDispatch(final Invoker invoker, final Object args) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				dispatch(invoker, args);
			}
		});
	}
	
	private void dispatch(Invoker invoker, Object args) {
		try {
			invoker.method.invoke(invoker.sourceObject, args);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}
}
