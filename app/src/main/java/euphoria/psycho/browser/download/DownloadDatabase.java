package euphoria.psycho.browser.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

class DownloadDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public DownloadDatabase(@Nullable Context context, @Nullable String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE  IF NOT EXISTS  \"downloads\" (\n" +
                "    \"id\" INTEGER  PRIMARY KEY AUTOINCREMENT,\n" +
                "    \"status\" TEXT,\n" +
                "    \"total_bytes\" INTEGER,\n" +
                "    \"current_bytes\" INTEGER,\n" +
                "    \"uri\" TEXT UNIQUE,\n" +
                "    \"eTag\" TEXT,\n" +
                "    \"file_name\" TEXT,\n" +
                "    \"error_msg\" TEXT,\n" +
                "    \"mime_type\" TEXT,\n" +
                "    \"retry_after\" INTEGER, \n" +
                "\t\"create_at\" INTEGER,\n" +
                "\t\"update_at\" INTEGER\n" +
                ");");
    }

    public void addDownloadInfo(DownloadInfo downloadInfo) {
        ContentValues values = new ContentValues();

        values.put("status", downloadInfo.getStatus());
        values.put("total_bytes", downloadInfo.getTotalBytes());
        values.put("current_bytes", downloadInfo.getCurrentBytes());
        values.put("uri", downloadInfo.getUri());
        values.put("eTag", downloadInfo.getETag());
        values.put("file_name", downloadInfo.getFileName());
        values.put("mime_type", downloadInfo.getMimeType());
        values.put("retry_after", downloadInfo.getRetryAfter());
        values.put("create_at", System.currentTimeMillis() / 1000);
        values.put("update_at", System.currentTimeMillis() / 1000);

        getWritableDatabase().insertWithOnConflict(
                "downloads", null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
