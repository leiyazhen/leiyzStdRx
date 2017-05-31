package leiyz.rxexample.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class ServiceProvider {

	static class MemorizingSupplier<T> implements ThrowableSupplier<T>,
			Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final ThrowableSupplier<T> delegate;
		transient volatile boolean initialized;
		transient T value;

		MemorizingSupplier(ThrowableSupplier<T> supplier) {
			this.delegate = supplier;
		}

		@Override
		public T get() throws Throwable {
			if (!initialized) {
				synchronized (this) {
					if (!initialized) {
						T t = delegate.get();
						value = t;
						initialized = true;
						return value;
					}
				}
			}
			return value;
		}
	}

	protected Consumer<Field> consumer() {
		return x -> {
			final boolean old = x.isAccessible();
			x.setAccessible(true);
			try {
				Class<?> entityType = Class.forName(Stream
						.of(x.getGenericType().getTypeName().split("<|>"))
						.skip(1).findFirst().get());
				SupplierInject supplierInject = x
						.getAnnotation(SupplierInject.class);
				ThrowableSupplier<?> object = () -> getObjectMapper().apply(
						entityType);
				x.set(this, supplierInject.cached() ? memorize(object) : object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				x.setAccessible(old);
			}
		};
	}

	private <T> ThrowableSupplier<T> memorize(ThrowableSupplier<T> delegate) {
		return (delegate instanceof MemorizingSupplier) ? delegate
				: new MemorizingSupplier<T>(delegate);
	}

	protected abstract ThrowableFunciton<Class<?>, Object> getObjectMapper();
}
