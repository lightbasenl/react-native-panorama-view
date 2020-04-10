package nl.lightbase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.webkit.URLUtil;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.vr.sdk.widgets.common.VrWidgetView.DisplayMode;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class PanoramaView extends VrPanoramaView {
    private static final String LOG_TAG = "PanoramaView";

    private VrPanoramaView.Options _options = new VrPanoramaView.Options();
    private ImageLoaderTask imageLoaderTask;

    private Integer imageWidth;
    private Integer imageHeight;
    private URL imageUrl;
    private Bitmap image;
    private String _inputType;
    private ThemedReactContext _context;


    public PanoramaView(ThemedReactContext context) {
        super(context.getCurrentActivity());
        _context = context;

        this.setEventListener(new ActivityEventListener());
        this.setDisplayMode(DisplayMode.EMBEDDED);
        this.setStereoModeButtonEnabled(false);
        this.setTransitionViewEnabled(false);
        this.setInfoButtonEnabled(false);
        this.setFullscreenButtonEnabled(false);
    }



    public void setImageSource(String value) {
        Log.i(LOG_TAG, "Image source: " + value);

        if (imageUrl != null && imageUrl.toString().equals(value)) {
            return;
        }

        try {
            imageUrl = new URL(value);

        } catch (MalformedURLException e) {
            image = null;
            emitEvent("onImageLoadingFailed", null);
            return;
        }

        if (imageLoaderTask != null) {
            imageLoaderTask.cancel(true);
        }

        if (imageUrl != null && URLUtil.isValidUrl(imageUrl.toString())) {
            try {
                imageLoaderTask = new ImageLoaderTask();
                imageLoaderTask.execute(Pair.create(imageUrl, _options));
            } catch (Exception e) {
                emitEvent("onImageLoadingFailed", null);
            }
        } else {
            emitEvent("onImageLoadingFailed", null);
        }
    }

    public void setDimensions(ReadableMap dimensions) {
        if ((imageWidth == dimensions.getInt("width")) && (imageHeight == dimensions.getInt("height"))){
            return;
        }

        imageWidth = dimensions.getInt("width");
        imageHeight = dimensions.getInt("height");

        Log.i(LOG_TAG, "Image dimensions: " + imageWidth + ", " + imageHeight);

        if(image != null){
            loadImageFromBitmap(image, _options);
        }
    }

    public void setInputType(String inputType) {
        if(_inputType != null && _inputType.equals(inputType)){
            return;
        }

        _inputType = inputType;
        switch (inputType) {
            case "mono":
                _options.inputType = _options.TYPE_MONO;
                break;
            case "stereo":
                _options.inputType = _options.TYPE_STEREO_OVER_UNDER;
                break;
            default:
                _options.inputType = _options.TYPE_MONO;
        }

        if(image != null){
            loadImageFromBitmap(image, _options);
        }
    }

    public void setEnableTouchTracking(boolean enableTouchTracking) {
        setTouchTrackingEnabled(enableTouchTracking);
    }

    class ImageLoaderTask extends AsyncTask<Pair<URL, VrPanoramaView.Options>, Void, Boolean> {
        protected Boolean doInBackground(Pair<URL, VrPanoramaView.Options>... fileInformation) {

            final URL imageUrl = fileInformation[0].first;
            VrPanoramaView.Options _options = fileInformation[0].second;

            InputStream istr = null;

            try {
                HttpURLConnection connection = (HttpURLConnection) fileInformation[0].first.openConnection();
                connection.connect();

                istr = connection.getInputStream();

                Assertions.assertCondition(istr != null);

                image = decodeSampledBitmap(istr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not load file: " + e);

                emitEvent("onImageLoadingFailed", null);
                return false;

            } finally {
                try {
                    istr.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Could not close input stream: " + e);

                    emitEvent("onImageLoadingFailed", null);
                }
            }

            loadImageFromBitmap(image, _options);

            return true;
        }

        private Bitmap decodeSampledBitmap(InputStream inputStream) throws IOException {
            final byte[] bytes = getBytesFromInputStream(inputStream);
            BitmapFactory.Options options = new BitmapFactory.Options();

            if (imageWidth != 0 && imageHeight != 0) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);
                options.inJustDecodeBounds = false;
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }

        private void copyData(InputStream in, ByteArrayOutputStream out) throws IOException {
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            copyData(inputStream, baos);

            return baos.toByteArray();
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }

    private class ActivityEventListener extends VrPanoramaEventListener {
        @Override
        public void onLoadSuccess() {
            Log.i(LOG_TAG, "Image loaded.");

            emitEvent("onImageLoaded", null);
        }

        @Override
        public void onLoadError(String errorMessage) {
            Log.e(LOG_TAG, "Error loading panorama: " + errorMessage);

            emitEvent("onImageLoadingFailed", null);
        }
    }

    private void emitEvent(String name, @Nullable WritableMap event) {
        if (event == null) {
            event = Arguments.createMap();
        }

        _context.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                name,
                event
        );
    }


}
