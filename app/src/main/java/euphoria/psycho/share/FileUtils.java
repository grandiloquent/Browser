// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package euphoria.psycho.share;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Locale;

import euphoria.psycho.browser.base.ThreadUtils;

/**
 * Helper methods for dealing with Files.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    public static void copyAssetFile(Context context, String src, String dst) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream in = manager.open(src);
        FileOutputStream out = new FileOutputStream(dst);
        IoUtils.copy(in, out);
        IoUtils.closeQuietly(in);
        IoUtils.closeQuietly(out);
    }


    /**
     * Atomically copies the data from an input stream into an output file.
     *
     * @param is      Input file stream to read data from.
     * @param outFile Output file path.
     * @param buffer  Caller-provided buffer. Provided to avoid allocating the same
     *                buffer on each call when copying several files in sequence.
     * @throws IOException in case of I/O error.
     */
    public static void copyFileStreamAtomicWithBuffer(InputStream is, File outFile, byte[] buffer)
            throws IOException {
        File tmpOutputFile = new File(outFile.getPath() + ".tmp");
        try (OutputStream os = new FileOutputStream(tmpOutputFile)) {
            Log.i(TAG, "Writing to %s", outFile);

            int count = 0;
            while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, count);
            }
        }
        if (!tmpOutputFile.renameTo(outFile)) {
            throw new IOException();
        }
    }

    public static byte[] createChecksum(InputStream fis) throws Exception {

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static File createDirectoryIfNotExists(String dirPath) {
        return createDirectoryIfNotExists(new File(dirPath));
    }

    public static File createDirectoryIfNotExists(File dir) {
         /*
         Tests whether the file denoted by this abstract pathname is a
         directory.
         Where it is required to distinguish an I/O exception from the case
         that the file is not a directory, or where several attributes of the
         same file are required at the same time, then the java.nio.file.Files#readAttributes(Path,Class,LinkOption[])
         Files.readAttributes method may be used.
         @return true if and only if the file denoted by this
         abstract pathname exists and is a directory;
         false otherwise
         @throws  SecurityException
         If a security manager exists and its java.lang.SecurityManager#checkRead(java.lang.String)
         method denies read access to the file
         */
        if (!dir.isDirectory()) {
         /*
         Creates the directory named by this abstract pathname, including any
         necessary but nonexistent parent directories.  Note that if this
         operation fails it may have succeeded in creating some of the necessary
         parent directories.
         @return  true if and only if the directory was created,
         along with all necessary parent directories; false
         otherwise
         @throws  SecurityException
         If a security manager exists and its java.lang.SecurityManager#checkRead(java.lang.String)
         method does not permit verification of the existence of the
         named directory and all necessary parent directories; or if
         the java.lang.SecurityManager#checkWrite(java.lang.String)
         method does not permit the named directory and all necessary
         parent directories to be created
         */
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Extracts an asset from the app's APK to a file.
     *
     * @param context
     * @param assetName Name of the asset to extract.
     * @param dest      File to extract the asset to.
     * @return true on success.
     */
    public static boolean extractAsset(Context context, String assetName, File dest) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getAssets().open(assetName);
            outputStream = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[8192];
            int c;
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    /**
     * Returns the file extension, or an empty string if none.
     *
     * @param file Name of the file, with or without the full path.
     * @return empty string if no extension, extension otherwise.
     */
    public static String getExtension(String file) {
        int index = file.lastIndexOf('.');
        if (index == -1) return "";
        return file.substring(index + 1).toLowerCase(Locale.US);
    }

    public static String getMD5Checksum(InputStream fis) throws Exception {
        byte[] b = createChecksum(fis);
        StringBuilder result = new StringBuilder();

        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        return getMD5Checksum(new FileInputStream(filename));
    }

    public static boolean isFile(String path) {
        return new File(path).isFile();
    }


    /**
     * Delete the given File and (if it's a directory) everything within it.
     */
    public static void recursivelyDeleteFile(File currentFile) {
        ThreadUtils.assertOnBackgroundThread();
        if (currentFile.isDirectory()) {
            File[] files = currentFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    recursivelyDeleteFile(file);
                }
            }
        }

        if (!currentFile.delete()) Log.e(TAG, "Failed to delete: " + currentFile);
    }

    public static void writeAllBytes(String path, byte[] bytes) throws IOException {
        FileOutputStream fs = new FileOutputStream(path);
        fs.write(bytes, 0, bytes.length);
        fs.close();
    }
}
