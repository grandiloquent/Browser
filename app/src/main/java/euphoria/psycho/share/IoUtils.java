package euphoria.psycho.share;

import android.database.Cursor;
import android.os.ParcelFileDescriptor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class IoUtils {
    private static final int BUFFER_SIZE = 8192;

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        try {
            if (fd != null) fd.close();
        } catch (Throwable t) {
        }
    }

    public static void closeSilently(Cursor cursor) {
        try {
            if (cursor != null) cursor.close();
        } catch (Throwable t) {
        }
    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
        }
    }

    public static long copy(InputStream source, OutputStream sink)
            throws IOException {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    public static long copy(String source, OutputStream out) {
        InputStream in = null;
        try {
            in = new FileInputStream(source);
            return copy(in, out);
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static String toString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\r\n");
        }
        reader.close();
        return sb.toString();
    }
}
