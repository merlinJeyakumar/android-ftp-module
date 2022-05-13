package com.nativedevps.ftp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utilitsss {
    /**
     * Read the image from the stream and create a bitmap scaled to the desired
     * size.  Resulting bitmap will be at least as large as the
     * desired minimum specified dimensions and will keep the image proportions
     * correct during scaling.
     */
    public static Bitmap createScaledBitmaps(InputStream inputStream, int desiredBitmapWith, int desiredBitmapHeight) throws IOException {
        BufferedInputStream imageFileStream = new BufferedInputStream(inputStream);
        try {
            // Phase 1: Get a reduced size image. In this part we will do a rough scale down
            int sampleSize = 1;
            if (desiredBitmapWith > 0 && desiredBitmapHeight > 0) {
                final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
                decodeBoundsOptions.inJustDecodeBounds = true;
                imageFileStream.mark(64 * 1024);
                BitmapFactory.decodeStream(imageFileStream, null, decodeBoundsOptions);
                imageFileStream.reset();
                final int originalWidth = decodeBoundsOptions.outWidth;
                final int originalHeight = decodeBoundsOptions.outHeight;
                // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
                sampleSize = Math.max(1, Math.max(originalWidth / desiredBitmapWith, originalHeight / desiredBitmapHeight));
            }
            BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
            decodeBitmapOptions.inSampleSize = sampleSize;
            decodeBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565; // Uses 2-bytes instead of default 4 per pixel

            // Get the roughly scaled-down image
            Bitmap bmp = BitmapFactory.decodeStream(imageFileStream, null, decodeBitmapOptions);

            // Phase 2: Get an exact-size image - no dimension will exceed the desired value
            float ratio = Math.min((float) desiredBitmapWith / (float) bmp.getWidth(), (float) desiredBitmapHeight / (float) bmp.getHeight());
            int w = (int) ((float) bmp.getWidth() * ratio);
            int h = (int) ((float) bmp.getHeight() * ratio);
            return Bitmap.createScaledBitmap(bmp, w, h, true);

        } catch (IOException e) {
            throw e;
        } finally {
            try {
                imageFileStream.close();
            } catch (IOException ignored) {
            }
        }
    }


    public static Bitmap downloadSample(InputStream inputStream) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = calculateInSampleSize(options, 1200, 1200);

        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
