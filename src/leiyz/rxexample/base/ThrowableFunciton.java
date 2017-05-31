package leiyz.rxexample.base;

@FunctionalInterface
public interface ThrowableFunciton<T, R> {
	public R apply(T t) throws Throwable;

	static <T> ThrowableFunciton<T, T> identiy() {
		return t -> t;
	}
}
