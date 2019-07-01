package me.gingerninja.authenticator.crypto;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.security.KeyException;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Class used for faster PBKDF2 implementation than using {@link javax.crypto.spec.PBEKeySpec}. It creates an
 * SQLCipher database that uses 256,000 iterations of PBKDF2-HMAC-SHA512 (as of 4.0.0) for key
 * derivation.
 */
@Singleton
public class CryptoPasswordHandler {
    private static final String DB_NAME = "_protomat.db";
    private static final int DB_VERSION = 1;

    private Context context;
    private ProtoMatHelper protoMatHelper;

    @Inject
    CryptoPasswordHandler(@NonNull Context context) {
        this.context = context.getApplicationContext();
        protoMatHelper = new ProtoMatHelper(this.context);
    }

    public void create(@NonNull char[] pass, boolean autoClose) {
        protoMatHelper.getWritableDatabase(pass);
        if (autoClose) {
            protoMatHelper.close();
        }
    }

    public boolean delete() {
        protoMatHelper.close();
        return context.deleteDatabase(DB_NAME);
    }

    /**
     * Changes the password of the protected material database.
     * MUST call {@link #authenticate(char[])} before.
     *
     * @param pass the new password
     */
    public void changePassword(@Nullable char[] pass) {
        SQLiteDatabase db = protoMatHelper.getWritableDatabase((byte[]) null);
        db.changePassword(pass);
        protoMatHelper.close();
    }

    public void authenticate(@NonNull char[] pass) throws KeyException {
        protoMatHelper.close();
        SQLiteDatabase db = protoMatHelper.getWritableDatabase(pass);
        if (!protoMatHelper.isKeyValid(db, pass)) {
            throw new KeyException();
        }
    }

    public void close() {
        protoMatHelper.close();
    }

    @NonNull
    public MasterKeyEntry getMasterKeyHandler() {
        SQLiteDatabase db = protoMatHelper.getWritableDatabase((byte[]) null);
        return new MasterKeyEntry(db);
    }

    @NonNull
    public byte[] getMasterKey(@NonNull char[] pass) throws KeyException {
        try {
            authenticate(pass);
            SQLiteDatabase db = protoMatHelper.getWritableDatabase((byte[]) null);
            return new MasterKeyEntry(db).get();
        } finally {
            protoMatHelper.close();
        }
    }

    private static class ProtoMatHelper extends SQLiteOpenHelper {
        private ProtoMatHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            SQLiteDatabase.loadLibs(context);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            new KeyEntry(db).createTable();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private boolean isKeyValid(@NonNull SQLiteDatabase db, @NonNull char[] pass) {
            try {
                db.rawExecSQL("SELECT count(*) FROM sqlite_master");
                return true;
            } catch (Throwable t) {
                Timber.w(t, "Invalid key");
                db.close();
            }
            return false;
        }
    }

    private static class KeyEntry {
        private static final String TABLE_NAME = "key";
        private static final String COL_NAME = "name";
        private static final String COL_VALUE = "value";

        @NonNull
        private final SQLiteDatabase db;

        private KeyEntry(@NonNull SQLiteDatabase db) {
            this.db = db;
        }

        private void createTable() {
            db.execSQL("CREATE TABLE IF NOT EXISTS \"" + TABLE_NAME + "\" (\"" + COL_NAME + "\" TEXT NOT NULL, \"" + COL_VALUE + "\" BLOB NOT NULL, PRIMARY KEY(\"" + COL_NAME + "\"))");
        }

        @Nullable
        protected byte[] get(@NonNull String name) {
            try (Cursor cursor = db.query(TABLE_NAME, new String[]{COL_VALUE}, COL_NAME + "=?", new String[]{name}, null, null, null, null)) {
                if (cursor.moveToFirst()) {
                    return cursor.getBlob(0);
                } else {
                    return null;
                }
            }
        }

        protected boolean set(@NonNull String name, @NonNull byte[] value) {
            ContentValues values = new ContentValues(1);
            values.put(COL_NAME, name);
            values.put(COL_VALUE, value);
            return db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;
        }

        protected boolean remove(@NonNull String name) {
            return db.delete(TABLE_NAME, COL_NAME + "=?", new String[]{name}) != 0;
        }
    }

    public static class MasterKeyEntry extends KeyEntry {
        public static final String NAME = "dbMaster";

        private MasterKeyEntry(@NonNull SQLiteDatabase db) {
            super(db);
        }

        public byte[] get() {
            return get(NAME);
        }

        public boolean set(@NonNull byte[] value) {
            return set(NAME, value);
        }

        public boolean remove() {
            return remove(NAME);
        }
    }
}
