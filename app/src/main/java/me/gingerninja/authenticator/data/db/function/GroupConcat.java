package me.gingerninja.authenticator.data.db.function;

import androidx.annotation.Nullable;

import io.requery.query.Expression;
import io.requery.query.function.Function;

public class GroupConcat<V> extends Function<V> {
    private final Expression<V> expression;

    @Nullable
    private final String separator;

    private GroupConcat(Expression<V> expression, @Nullable String separator) {
        super("group_concat", expression.getClassType());
        this.expression = expression;
        this.separator = separator;
    }

    public static <U> GroupConcat<U> groupConcat(Expression<U> expression) {
        return groupConcat(expression, null);
    }

    public static <U> GroupConcat<U> groupConcat(Expression<U> expression, @Nullable String separator) {
        return new GroupConcat<>(expression, separator);
    }

    @Override
    public Object[] arguments() {
        if (separator == null) {
            return new Object[]{expression};
        } else {
            return new Object[]{expression, separator};
        }
    }
}
