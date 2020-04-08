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

public class PanoramaViewManager extends SimpleViewManager<VrPanoramaView> {
    private static final String REACT_CLASS = "PanoramaView";

    private ReactApplicationContext _context;
    private VrPanoramaView vrPanoramaView;

    private VrPanoramaView.Options _options = new VrPanoramaView.Options();
    private Map<URL, Bitmap> imageCache = new HashMap<>();
    private ImageLoaderTask imageLoaderTask;
    private Integer imageWidth;
    private Integer imageHeight;
    private URL imageUrl;

    public PanoramaViewManager(ReactApplicationContext context) {
        super();
        _context = context;
    }

    public ReactApplicationContext getContext() {
        return _context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public VrPanoramaView createViewInstance(ThemedReactContext context) {
        vrPanoramaView = new VrPanoramaView(context.getCurrentActivity());
        vrPanoramaView.setEventListener(new ActivityEventListener());

        vrPanoramaView.setDisplayMode(DisplayMode.EMBEDDED);
        vrPanoramaView.setStereoModeButtonEnabled(false);
        vrPanoramaView.setTransitionViewEnabled(false);
        vrPanoramaView.setInfoButtonEnabled(false);
        vrPanoramaView.setFullscreenButtonEnabled(false);

        return vrPanoramaView;
    }

    @Override
    protected void onAfterUpdateTransaction(VrPanoramaView view) {
        super.onAfterUpdateTransaction(view);

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

    @Override
    public @Nullable Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "onImageLoadingFailed",
                MapBuilder.of("registrationName", "onImageLoadingFailed"),
                "onImageLoaded",
                MapBuilder.of("registrationName", "onImageLoaded")
        );
    }


    @ReactProp(name = "imageUrl")
    public void setImageSource(VrPanoramaView view, String value) {
        Log.i(REACT_CLASS, "Image source: " + value);

        if (imageUrl != null && imageUrl.toString().equals(value)) {
            return;
        }

        try {
            imageUrl = new URL(value);
        } catch (MalformedURLException e) {
            emitEvent("onImageLoadingFailed", null);
        }
    }

    @ReactProp(name = "dimensions")
    public void setDimensions(VrPanoramaView view, ReadableMap dimensions) {
        imageWidth = dimensions.getInt("width");
        imageHeight = dimensions.getInt("height");

        Log.i(REACT_CLASS, "Image dimensions: " + imageWidth + ", " + imageHeight);
    }

    @ReactProp(name = "inputType")
    public void setInputType(VrPanoramaView view, String inputType) {
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
    }

    @ReactProp(name = "enableTouchTracking")
    public void setEnableTouchTracking(VrPanoramaView view, boolean enableTouchTracking) {
        view.setTouchTrackingEnabled(enableTouchTracking);
    }

    class ImageLoaderTask extends AsyncTask<Pair<URL, VrPanoramaView.Options>, Void, Boolean> {
        protected Boolean doInBackground(Pair<URL, VrPanoramaView.Options>... fileInformation) {
            final URL imageUrl = fileInformation[0].first;
            VrPanoramaView.Options _options = fileInformation[0].second;

            InputStream istr = null;
            Bitmap image;

            if (!imageCache.containsKey(imageUrl)) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) fileInformation[0].first.openConnection();
                    connection.connect();

                    istr = connection.getInputStream();

                    Assertions.assertCondition(istr != null);

                    imageCache.put(imageUrl, decodeSampledBitmap(istr));
                } catch (IOException e) {
                    Log.e(REACT_CLASS, "Could not load file: " + e);

                    emitEvent("onImageLoadingFailed", null);
                    return false;
                } finally {
                    try {
                        istr.close();
                    } catch (IOException e) {
                        Log.e(REACT_CLASS, "Could not close input stream: " + e);

                        emitEvent("onImageLoadingFailed", null);
                    }
                }
            }

            image = imageCache.get(imageUrl);

            vrPanoramaView.loadImageFromBitmap(image, _options);

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
            Log.i(REACT_CLASS, "Image loaded.");

            emitEvent("onImageLoaded", null);
        }

        @Override
        public void onLoadError(String errorMessage) {
            Log.e(REACT_CLASS, "Error loading panorama: " + errorMessage);

            emitEvent("onImageLoadingFailed", null);
        }
    }

    private void emitEvent(String name, @Nullable WritableMap event) {
        if (event == null) {
            event = Arguments.createMap();
        }

        getContext().getJSModule(RCTEventEmitter.class).receiveEvent(
                vrPanoramaView.getId(),
                name,
                event
        );
    }
}