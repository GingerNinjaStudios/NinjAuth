package me.gingerninja.authenticator.data.db.function;

import io.requery.query.function.Function;

public class CountHack extends Function<Integer> {

    private Name name;

    public CountHack(int size) {
        super("CAST(" + size + " AS INTEGER)", Integer.class);
        name = new Name("CAST(" + size + " AS INTEGER)", true);
    }

    @Override
    public Object[] arguments() {
        return new Object[]{};
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public Name getFunctionName() {
        return name;
    }
}