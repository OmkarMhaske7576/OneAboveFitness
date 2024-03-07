package com.one.above.fitness.utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.above.fitness.R;
import com.one.above.fitness.pojo.FaceData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class Utility {
    public static float FACE_DISTANCE = 0.7f;
    public static final int MY_CAMERA_REQUEST_CODE = 100;
    public static boolean isBlutoothEnabled = false;
    public static FaceData currentLoginUser = null;
    public static Vibrator vibrator;
    public static float SCREEN_HEIGHT;
    public static float SCREEN_WIDTH;
    public static final String DEFAULT_PASSWORD = "123456";
    public static UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public static void vibrate() {

        try {
            Utility.vibrator.vibrate(30);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void vibrateWithAnim(View v) {
        try {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce));
            Utility.vibrate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDateInDDMMYY() {

        String PATTERN = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern(PATTERN);
        String date1 = dateFormat.format(Calendar.getInstance().getTime());
        return date1;
    }

    public static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {

        Matrix matrix = new Matrix();

        matrix.postRotate(rotationDegrees);

        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);

        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }

        return rotatedBitmap;
    }

    public static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();

        int height = image.getHeight();

        int ySize = width * height;

        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y

        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U

        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();

        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely

            yBuffer.get(nv21, 0, ySize);

            pos += ySize;

        } else {

            long yBufferPos = -rowStride; // not an actual position

            for (; pos < ySize; pos += width) {

                yBufferPos += rowStride;

                yBuffer.position((int) yBufferPos);

                yBuffer.get(nv21, pos, width);

            }

        }

        rowStride = image.getPlanes()[2].getRowStride();

        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());

        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {

            byte savePixel = vBuffer.get(1);

            try {

                vBuffer.put(1, (byte) ~savePixel);

                if (uBuffer.get(0) == (byte) ~savePixel) {

                    vBuffer.put(1, savePixel);

                    vBuffer.position(0);

                    uBuffer.position(0);

                    vBuffer.get(nv21, ySize, 1);

                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }

            } catch (ReadOnlyBufferException ex) {
            }

            vBuffer.put(1, savePixel);
        }

        for (int row = 0; row < height / 2; row++) {

            for (int col = 0; col < width / 2; col++) {

                int vuPos = col * pixelStride + row * rowStride;

                nv21[pos++] = vBuffer.get(vuPos);

                nv21[pos++] = uBuffer.get(vuPos);

            }

        }
        return nv21;
    }

    public static Bitmap toBitmap(Image image) {

        byte[] nv21 = YUV_420_888toNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {

        int width = bm.getWidth();

        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;

        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);

        bm.recycle();

        return resizedBitmap;
    }

    public static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {

        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);

        Canvas cavas = new Canvas(resultBitmap);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        paint.setColor(Color.WHITE);

        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();

        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    public static MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {

        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();

        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static byte[] makeByte(Object modeldata) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(modeldata);
            byte[] employeeAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(employeeAsBytes);
            return employeeAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object readByte(byte[] data) {

        try {

            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            Object dataobj = (Object) ois.readObject();
            return dataobj;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String getUniqueId() throws Exception {
        String id = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        Log.e("ID {}", id);
        return id;
    }

    public static Bitmap getBitmapFromString(String stringPicture) {
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    public static String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public static float[][] convertStringTo2DArray(String input) throws JsonProcessingException {
        float[][] floatVal = new float[5][];
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            floatVal = objectMapper.readValue(input, float[][].class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return floatVal;
    }

    public static Dialog showLoadingDialog(String dialogMessage, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_progress);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        TextView dialogTextView = dialog.findViewById(R.id.msg);
        dialogTextView.setText(dialogMessage);
        return dialog;
    }

}
