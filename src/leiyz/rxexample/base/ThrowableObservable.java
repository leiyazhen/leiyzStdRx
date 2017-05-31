package leiyz.rxexample.base;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import rx.Observable;
import rx.exceptions.OnCompletedFailedException;
import rx.exceptions.OnErrorFailedException;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Func1;

;

/**
 * Created by lyz on 17-5-24.
 */
public class ThrowableObservable<T, E extends Throwable> {
	private Observable<T> observable;
	private Function<Throwable, E> throwableFunction;

	protected ThrowableObservable(Observable<T> observable,
			Function<Throwable, E> throwableFunction) {
		this.observable = observable;
		this.throwableFunction = throwableFunction;
	}

	public Observable<T> asObservable() {
		return observable;
	}

	public ThrowableObservable<T, E> withException(
			Function<Throwable, E> throwableFunction) {
		if (this.throwableFunction == null) {
			this.throwableFunction = throwableFunction;
		}
		return this;
	}

	private static <R> Observable<R> asObservable(ThrowableSupplier<R> supplier) {
		return Observable.create(f -> {
			try {
				f.onNext(supplier.get());

			} catch (Throwable e) {
				f.onError(e);
			}
			f.onCompleted();
		});
	}

	public Stream<T> excuteStream() {
		return observable.toList().toBlocking().first().stream();
	}

	private <R> R throwableCall(Function<Observable<T>, R> mapper) throws E,
			IOException {
		try {
			return mapper.apply(observable);

		} catch (OnErrorFailedException | OnErrorNotImplementedException
				| OnCompletedFailedException e) {
			Throwable throwable = e.getCause();
			if (throwable instanceof IOException) {
				throw (IOException) throwable;
			}
			throw throwableFunction.apply((IOException) throwable);
		}
	}

	public T excute() throws E, IOException {
		return throwableCall(f -> f.toBlocking().first());
	}

	public Stream<T> toStream() throws E, IOException {
		return throwableCall(f -> f.toList().toBlocking().first().stream());
	}

	public ThrowableObservable<List<T>, E> toList() {
		return new ThrowableObservable<>(observable.toList(), throwableFunction);
	}

	public ThrowableObservable<T, E> filter(Function<? super T, Boolean> func) {
		return new ThrowableObservable<>(observable.filter(x -> func.apply(x)),
				throwableFunction);
	}

	public <R> ThrowableObservable<R, E> map(Function<T, R> mapper) {
		return new ThrowableObservable<>(observable.map(x -> mapper.apply(x)),
				throwableFunction);
	}

	public <R> ThrowableObservable<R, E> flatMap(
			Function<T, ThrowableObservable<R, E>> mapper) {
		return new ThrowableObservable<>(observable.flatMap(x -> mapper
				.apply(x).observable), throwableFunction);
	}

	public <R> ThrowableObservable<R, E> throwableMap(Function<T, R> mapper) {
		Func1<? super T, ? extends Observable<? extends R>> func = t -> {
			return Observable.create(f -> {
				try {
					f.onNext(mapper.apply(t));
				} catch (Exception e) {
					f.onError(e);
				}
				f.onCompleted();
			});
		};
		return new ThrowableObservable<>(observable.flatMap(func),
				throwableFunction);
	}

	@SuppressWarnings("unchecked")
	public static <R, E extends Exception> ThrowableObservable<R, E> from(
			R[] array) {
		return new ThrowableObservable<>(Observable.from(array), x -> (E) x);
	}

	public static <R, E extends Exception> ThrowableObservable<R, E> from(
			ThrowableSupplier<R> supplier) {
		return new ThrowableObservable<>(asObservable(supplier), x -> (E) x);
	}

	public static <R, E extends Exception> ThrowableObservable<R, E> from(
			ThrowableSupplier<R> supplier, Function<Throwable, E> throwablefunc) {
		return new ThrowableObservable<>(asObservable(supplier), throwablefunc);
	}

	public void subscribe(ThrowableAction<T> action) throws IOException, E {
		try {
			action.call(observable.toBlocking().first());
		} catch (Throwable e) {
			if (e instanceof RuntimeException) {
				Throwable throwable = e.getCause();
				if (throwable instanceof IOException) {
					throw (IOException) throwable;
				}
				throw throwableFunction.apply(throwable);
			}
			throw throwableFunction.apply(e);
		}
	}

	public void foreach(Consumer<T> consumer) throws IOException, E {
		try {
			observable.toBlocking().forEach(action -> consumer.accept(action));
		} catch (OnErrorFailedException | OnErrorNotImplementedException
				| OnCompletedFailedException e) {
			Throwable throwable = e.getCause();
			if (throwable instanceof IOException) {
				throw (IOException) throwable;
			}
			throw throwableFunction.apply((IOException) throwable);
		} catch (Exception e) {
			Throwable throwable = e.getCause();
			if (throwable instanceof IOException) {
				throw (IOException) throwable;
			}
			throw throwableFunction.apply(throwable);
		}
	}
}
