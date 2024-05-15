package supersocksr.ppp.android.openppp2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public final class DrawingX { /* Graphics */
    // Gets the drawn bitmap from the drawing object.
    @Nullable
    public static Bitmap bitmap_drawable_to_bitmap(@NotNull Drawable drawable) {
        try {
            BitmapDrawable b = (BitmapDrawable) drawable;
            Bitmap r = b.getBitmap();
            if (r != null) {
                return r;
            }
        } catch (Throwable ignored) {
        }

        try {
            int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
            int height = drawable.getIntrinsicHeight();

            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565; // 取drawable的颜色格式
            Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建立对应bitmap

            Canvas canvas = new Canvas(bitmap); // 建立对应bitmap的画布
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas); // 把drawable内容画到画布中
            return bitmap;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // According to the X, Y scale to the scale of the bitmap.
    @Nullable
    public static Drawable bitmap_drawable_zoom(@NotNull Context context, @NotNull Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        float scaleWidth = ((float) w / width); // 计算缩放比例
        float scaleHeight = ((float) h / height);

        Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
        matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例

        Bitmap oldBmp = bitmap_drawable_to_bitmap(drawable); // drawable转换成bitmap
        if (oldBmp == null) {
            return null;
        }

        try {
            Bitmap newBmp = Bitmap.createBitmap(oldBmp, 0, 0, width, height, matrix, true); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            return new BitmapDrawable(context.getResources(), newBmp); // 把bitmap转换成drawable并返回
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Save the bitmap to a file.
    public static boolean bitmap_save_to_file(Drawable drawable, String path, Bitmap.CompressFormat format) {
        if (drawable == null) {
            return false;
        }

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            Bitmap bmp = bitmap_drawable_to_bitmap(drawable);
            if (bmp == null) {
                return false;
            }

            if (!FileX.file_create_new(path)) {
                return false;
            }

            FileOutputStream out = new FileOutputStream(new File(path));
            boolean ok = bmp.compress(format, 100, out);
            out.flush();
            out.close();
            return ok;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Converts the bitmap to a base64 string.
    @Nullable
    public static String bitmap_to_base64(Drawable drawable, Bitmap.CompressFormat format) {
        if (drawable == null) {
            return null;
        }

        try {
            Bitmap bmp = bitmap_drawable_to_bitmap(drawable);
            if (bmp == null) {
                return null;
            } else {
                return bitmap_to_base64(bmp, format);
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Converts the bitmap to a base64 string.
    @Nullable
    public static String bitmap_to_base64(Bitmap bmp, Bitmap.CompressFormat format) {
        if (bmp == null) {
            return null;
        }

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bmp.compress(format, 100, baos);

            baos.flush();
            baos.close();

            byte[] bitmapBytes = baos.toByteArray();
            return base64_to_string(bitmapBytes);
        } catch (Throwable ignored) {
            return null;
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    // Converts a base64 string to a bitmap.
    @Nullable
    public static Bitmap base64_to_bitmap(String base64Data) {
        if (TextUtils.isEmpty(base64Data)) {
            return null;
        }

        try {
            byte[] bytes = base64_to_bytes(base64Data);
            if (bytes == null || bytes.length < 1) {
                return null;
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Converts byte arrays to base64 strings.
    @Nullable
    public static String base64_to_string(byte[] bytes) {
        if (bytes == null || bytes.length < 1) {
            return null;
        }

        try {
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Converts a base64 string to a byte array.
    @Nullable
    public static byte[] base64_to_bytes(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }

        try {
            return Base64.decode(s, Base64.DEFAULT);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
