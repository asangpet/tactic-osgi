package ak.tactic.model.simulator.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.DeadEvent;

public class Bus {
	private final Logger logger = LoggerFactory.getLogger(Bus.class);
	private final ConcurrentHashMap<Class<?>, List<Invoker>> eventToInvokerMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<EventObjectKey, List<Invoker>> eventObjectToMethodMap = new ConcurrentHashMap<>();
	private final BlockingQueue<Poster> eventQueue = new LinkedBlockingQueue<>();
	private final ExecutorService executor;
	private final Thread dispatchThread;
	
	public Bus(ExecutorService executor) {
		this.executor = executor;
		dispatchThread = new Thread("DispatchBus") {
			@Override
			public void run() {
				try {
					while (true) {
						Poster postedEvent = eventQueue.take();
						emitEvent(postedEvent);
					}
				} catch (InterruptedException e) {
					logger.warn("Dispatcher thread interrupted, terminating");
				}
			};
		};
		dispatchThread.setDaemon(true);
		dispatchThread.start();
	}
	
	private class EventObjectKey {
		Class<?> eventClass;
		Object source;
		
		public EventObjectKey(Class<?> clazz, Object source) {
			eventClass = clazz;
			this.source = source;
		}
		
		@Override
		public boolean equals(Object obj) {
			EventObjectKey testKey = (EventObjectKey)obj;
			return this.source.equals(testKey.source) && this.eventClass.equals(testKey.eventClass); 
		}
		
		@Override
		public int hashCode() {
			return source.hashCode()*39 + eventClass.hashCode();
		}
	}
	
	private class Invoker {
		Method method;
		boolean hasCallback;
		Object sourceObject;
		
		public Invoker(Object source, Method method, boolean hasCallback) {
			this.sourceObject = source;
			this.method = method;
			this.hasCallback = hasCallback;
		}
	}
	
	private class Poster {
		Object callback;
		Object target;
		Object event;
		
		public Poster(Object event, Object target, Object callback) {
			this.callback = callback;
			this.event = event;
			this.target = target;
		}
	}
	
	public void register(Object object) {
		for (Method method : object.getClass().getMethods()) {
			Subscribe subscribe = method.getAnnotation(Subscribe.class);
			if (subscribe != null) {
				Class<?>[] params = method.getParameterTypes();
				Class<?> eventClass = params[0];
				List<Invoker> methodList = eventToInvokerMap.get(eventClass);
				if (methodList == null) {
					methodList = new ArrayList<Invoker>();
					eventToInvokerMap.put(eventClass, methodList);
				}
				logger.info("Event {} has handler {}", eventClass, method);
				boolean hasCallback = params.length == 2;
				Invoker invoker = new Invoker(object, method, hasCallback);
				methodList.add(invoker);
				
				EventObjectKey eventObjectKey = new EventObjectKey(eventClass, object);
				List<Invoker> objectMethods = eventObjectToMethodMap.get(eventClass);
				if (objectMethods == null) {
					objectMethods = new ArrayList<Invoker>();
					eventObjectToMethodMap.put(eventObjectKey, objectMethods);
				}
				objectMethods.add(invoker);
			}
		}
	}
	
	public void post(Object event, Object target, Object callback) {
		Poster poster = new Poster(event, target, callback);
		eventQueue.add(poster);
	}
	
	public void post(Object event) {
		post(event, null, null);
	}
	
	public void send(Object event, Object target) {
		post(event, target, this);
	}

	public void send(Object event, Object target, Object callback) {
		post(event, target, callback);
	}
	
	private void emitEvent(Poster poster) {
		List<Invoker> invokers = null;
		if (poster.target == null) {
			// Publish event
			invokers = eventToInvokerMap.get(poster.event.getClass());
		} else {
			// Direct event
			invokers = eventObjectToMethodMap.get(new EventObjectKey(poster.event.getClass(), poster.target));
		}
		if (invokers == null) {
			if (poster.event instanceof DeadEvent) {
				return;
			} else {
				if (poster.target == null) {
					logger.warn("No handler registered for event {}", poster.event);
				} else {
					logger.warn("No handler registered for event {} with object {}", poster.event, poster.target);
				}
				post(new DeadEvent(poster.callback, poster.event));
				return;
			}
		}
		for (Invoker invoker : invokers) {
			if (invoker.hasCallback) {
				asyncDispatch(invoker, poster.event, poster.callback);
			} else {
				asyncDispatch(invoker, poster.event);
			}
		}
	}
	
	private void asyncDispatch(final Invoker invoker, final Object... args) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				//logger.debug("Dispatching {} params to {} Method {}",new Object[] {args.length, invoker.sourceObject, invoker.method});
				dispatch(invoker, args);
			}
		});
	}
	
	private void dispatch(Invoker invoker, Object... args) {
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
