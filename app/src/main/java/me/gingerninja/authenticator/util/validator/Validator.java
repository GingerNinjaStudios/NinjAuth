package me.gingerninja.authenticator.util.validator;


import androidx.annotation.Nullable;

public class Validator<T> {
    public static final int RESULT_OK = 0;

    @Nullable
    private Consumer<T> onSuccess;

    @Nullable
    private Consumer<Integer> onFailure;

    private int failReason = RESULT_OK;

    @Nullable
    private T data;

    private Validator(int failReason) {
        this.failReason = failReason;
    }

    private Validator(@Nullable T data, @Nullable Consumer<T> onSuccess, @Nullable Consumer<Integer> onFailure) {
        this.data = data;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public static <U> Validator<U> from(@Nullable U data) {
        return from(data, null, null);
    }

    public static <U> Validator<U> from(@Nullable U data, @Nullable Consumer<U> onSuccess, @Nullable Consumer<Integer> onFailure) {
        return new Validator<>(data, onSuccess, onFailure);
    }

    public static <U> Validator<U> fail(int failReason) {
        return new Validator<>(failReason);
    }

    /**
     * Checks all validators and calls either onSuccess or onFailure on all of them. If one
     * validator fails, all of them fail, and their onFailure will be called even if they didn't
     * specifically fail. In this case the failReason will be {@link #RESULT_OK}.
     *
     * @param validators the validators to be checked
     * @return true if all of the validators succeed; otherwise false
     */
    public static boolean all(Validator... validators) {
        boolean success = true;

        for (Validator validator : validators) {
            success = !validator.hasFailed();
            if (!success) {
                break;
            }
        }

        if (success) {
            for (Validator validator : validators) {
                validator.callSuccess();
            }
        } else {
            for (Validator validator : validators) {
                validator.callFailure();
            }
        }

        return success;
    }

    public Validator<T> notNull(int failReason) {
        if (hasFailed()) {
            return this;
        }

        if (data == null) {
            this.failReason = failReason;
        }

        return this;
    }

    public Validator<T> test(Predicate<T> predicate, int failReason) {
        if (hasFailed()) {
            return this;
        }

        if (!predicate.test(data)) {
            this.failReason = failReason;
        }

        return this;
    }

    public Validator<T> test(Function<T, Integer> predicate) {
        if (hasFailed()) {
            return this;
        }

        failReason = predicate.apply(data);
        return this;
    }

    public Validator<T> process(Function<T, T> processor) {
        if (hasFailed()) {
            return this;
        }

        data = processor.apply(data);
        return this;
    }

    public <U> Validator<U> transform(Function<T, U> transformer) {
        return transform(transformer, null);
    }

    public <U> Validator<U> transform(Function<T, U> transformer, @Nullable Consumer<U> onSuccess) {
        if (hasFailed()) {
            return Validator.<U>fail(failReason).onFailure(onFailure);
        }

        return new Validator<>(transformer.apply(data), onSuccess, onFailure);
    }

    public Validator<T> onSuccess(Consumer<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Validator<T> onFailure(Consumer<Integer> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

    public boolean done() {
        if (hasFailed()) {
            if (onFailure != null) {
                onFailure.accept(failReason);
            }

            return false;
        } else {
            if (onSuccess != null) {
                onSuccess.accept(data);
            }

            return true;
        }
    }

    private void callSuccess() {
        if (onSuccess != null) {
            onSuccess.accept(data);
        }
    }

    private void callFailure() {
        if (onFailure != null) {
            onFailure.accept(failReason);
        }
    }

    public boolean hasFailed() {
        return failReason != RESULT_OK;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public interface Consumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         */
        void accept(T t);
    }

    public interface Predicate<T> {

        boolean test(T value);
    }

    public interface Function<I, O> {
        /**
         * Applies this function to the given input.
         *
         * @param input the input
         * @return the function result.
         */
        O apply(I input);
    }

    public static class Result<T> {
        private final T data;
        private final int resultCode;

        private Result(T data, int resultCode) {
            this.data = data;
            this.resultCode = resultCode;
        }

        public boolean isSuccessful() {
            return resultCode == RESULT_OK;
        }

        public T getData() {
            return data;
        }

        public int getResultCode() {
            return resultCode;
        }
    }
}
