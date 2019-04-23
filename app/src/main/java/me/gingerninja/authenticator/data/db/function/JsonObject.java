package me.gingerninja.authenticator.data.db.function;

import androidx.core.util.Pair;

import java.util.LinkedList;

import io.requery.query.Expression;
import io.requery.query.function.Function;

public class JsonObject<V> extends Function<V> {
    private final Pair<String, Expression<V>>[] pairs;

    private JsonObject(Pair<String, Expression<V>>[] pairs) {
        super("json_object", pairs[0].second.getClassType());
        this.pairs = pairs;
    }

    public static <U> Builder create(String fieldName, Expression<U> expression) {
        return new Builder().add(fieldName, expression);
    }

    @Override
    public Object[] arguments() {
        Object[] args = new Object[pairs.length * 2];
        for (int i = 0, a = 0; i < args.length; i += 2, a++) {
            args[i] = pairs[a].first;
            args[i + 1] = pairs[a].second;
        }

        return args;
    }

    public static class Builder {
        LinkedList<Pair<String, Expression<?>>> pairs = new LinkedList<>();

        public Builder add(String fieldName, Expression<?> expression) {
            pairs.add(new Pair<>(fieldName, expression));
            return this;
        }

        public <U> JsonObject<U> jsonObject() {
            return new JsonObject<>(pairs.toArray(new Pair[0]));
        }
    }
}
