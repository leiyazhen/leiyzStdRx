package leiyz.rxexample.base;

@FunctionalInterface
public interface ThrowableAction<T> {
	public void call(T t) throws Throwable;
}
