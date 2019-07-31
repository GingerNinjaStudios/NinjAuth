package me.gingerninja.authenticator.util.backup;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedList;

import io.reactivex.Observable;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;

public class BackupAccountLabelTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (DataHolder.class.isAssignableFrom(type.getRawType())) {
            return (TypeAdapter<T>) new Adapter();
        }

        return null;
    }

    public static class Adapter extends TypeAdapter<DataHolder> {
        @Override
        public void write(JsonWriter out, DataHolder value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginArray();

            value.<AccountHasLabel>getData()
                    .map(accountHasLabel -> accountHasLabel.getLabel().getUid())
                    .collectInto(out, JsonWriter::value)
                    .ignoreElement()
                    .blockingAwait();

            out.endArray();
            out.flush();
        }

        @Override
        public DataHolder read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (in.peek() == JsonToken.BEGIN_ARRAY) {
                in.beginArray();

                LinkedList<String> ids = new LinkedList<>(); // TODO improve memory usage by returning IDs one-by-one instead of as a list
                while (in.hasNext()) {
                    if (in.peek() == JsonToken.STRING) {
                        ids.add(in.nextString());
                    } else {
                        in.skipValue();
                    }
                }

                in.endArray();

                return new DataHolder(Observable.fromIterable(ids));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Helper class for providing ordered labels for accounts that can be serialized by GSON.
     */
    public static class DataHolder {
        private final Observable data;

        DataHolder(Observable data) {
            this.data = data;
        }

        @SuppressWarnings("unchecked")
        public <T> Observable<T> getData() {
            return (Observable<T>) data;
        }
    }
}
