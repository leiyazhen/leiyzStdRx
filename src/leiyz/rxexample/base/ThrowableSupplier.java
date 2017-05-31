package leiyz.rxexample.base;

@FunctionalInterface
public interface ThrowableSupplier<R> {
	public R get() throws Throwable;
}
