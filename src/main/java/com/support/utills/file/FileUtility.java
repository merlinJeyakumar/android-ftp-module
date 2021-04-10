package com.support.utills.file;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtility {
    public static void copyAssets(Context context, String assetFolderName, String targetPath) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetFolderName);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(assetFolderName + File.separator + filename);
                File outFile = new File(targetPath, filename);
                out = new FileOutputStream(outFile);
                copy(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[1024];
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /*
     * Android Q required
     * uses content Resolver to create and store image by output stream
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Pair<Boolean, String> saveMedia(Context context,
                                                  Object sourceFile,
                                                  Uri contentUri,
                                                  String fileName,
                                                  String mime,
                                                  String storePath) throws IOException {
        new File(storePath).mkdirs(); //create parent directory
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, storePath);

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(contentUri, contentValues);
        OutputStream imageOutStream = null;
        InputStream fileInputStream = null;

        try { // NOSONAR
            if (uri == null) {
                throw new IOException("Failed to insert MediaStore row");
            }

            if (sourceFile instanceof File) {
                fileInputStream = new FileInputStream(((File) sourceFile));
            } else if (sourceFile instanceof Uri) {
                fileInputStream = resolver.openInputStream((Uri) sourceFile);
            }
            imageOutStream = resolver.openOutputStream(uri);
            outStreamWrite(fileInputStream, imageOutStream);
            return new Pair(true, uri.getPath());
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (imageOutStream != null) {
                imageOutStream.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return new Pair(false, null); // NOSONAR
    }

    /*
     * writes input stream data to output stream
     * handled by reading with memory allocation
     */
    public static Boolean outStreamWrite(InputStream inputStream, OutputStream outputStream) {
        try (InputStream in = inputStream;
             OutputStream out = outputStream) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            // write the output file (You have now copied the file)
            out.flush();
            return true;
        } catch (IOException e) {
            Log.d("@@@", "file utils - file moving - exception - ", e);
            e.printStackTrace();
        }
        return false;
    }

    public static String fileMoving(File file, File dir) {
        if (!dir.exists()) {
            boolean result = dir.mkdirs();
            Log.i("@@@", "file - result " + result);
        }
        File targetFile = new File(dir, file.getName());
        try (InputStream in = new FileInputStream(file);
             OutputStream out = new FileOutputStream(targetFile);) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
// write the output file (You have now copied the file)
            out.flush();
            return targetFile.getAbsolutePath();
        } catch (IOException e) {
            Log.d("@@@", "file utils - file moving - exception - ", e);
        }
        return null;
    }

    public static void notifyMediaGallery(String filePath, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); // NOSONAR
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent); // NOSONAR
    }
}
