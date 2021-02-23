package nl.lightbase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.net.Uri;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class PanoramaView extends VrPanoramaView implements LifecycleEventListener {
    private static final String LOG_TAG = "PanoramaView";
    private final static String SCHEME_FILE = "file";

    private VrPanoramaView.Options _options = new VrPanoramaView.Options();
    private ImageLoaderTask imageLoaderTask;

    private Integer imageWidth;
    private Integer imageHeight;
    private String imageUrl;
    private Bitmap image;
    private String _inputType;
    private ThemedReactContext _context;


    public PanoramaView(ThemedReactContext context) {
        super(context.getCurrentActivity());

        _context = context;

        context.addLifecycleEventListener(this);

        this.setEventListener(new ActivityEventListener());
        this.setDisplayMode(DisplayMode.EMBEDDED);
        this.setStereoModeButtonEnabled(false);
        this.setTransitionViewEnabled(false);
        this.setInfoButtonEnabled(false);
        this.setFullscreenButtonEnabled(false);

    }



    public void setImageSource(String value) {
        //Log.i(LOG_TAG, "Image source: " + value);

        if(value == null){
            return;
        }

        if (imageUrl != null && imageUrl.equals(value)) {
            return;
        }

        imageUrl = value;

        try {
            if (imageLoaderTask != null) {
                imageLoaderTask.cancel(true);
            }

            imageLoaderTask = new ImageLoaderTask();
            imageLoaderTask.execute(Pair.create(imageUrl, _options));

        } catch (Exception e) {
            emitImageLoadingFailed(e.toString());
        }
    }

    public void setDimensions(ReadableMap dimensions) {

        imageWidth = dimensions.getInt("width");
        imageHeight = dimensions.getInt("height");

        //Log.i(LOG_TAG, "Image dimensions: " + imageWidth + ", " + imageHeight);

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

    class ImageLoaderTask extends AsyncTask<Pair<String, VrPanoramaView.Options>, Void, Boolean> {

        protected Boolean doInBackground(Pair<String, VrPanoramaView.Options>... fileInformation) {

            if(isCancelled()){
                return false;
            }

            VrPanoramaView.Options _options = fileInformation[0].second;

            InputStream istr = null;

            try {

                String value = fileInformation[0].first;
                Uri imageUri = Uri.parse(value);
                String scheme = imageUri.getScheme();

                if(scheme == null || scheme.equalsIgnoreCase(SCHEME_FILE)){
                    istr = new FileInputStream(new File(imageUri.getPath()));

                }
                else{
                    URL url = new URL(value);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    istr = connection.getInputStream();
                }

                Assertions.assertCondition(istr != null);
                image = decodeSampledBitmap(istr);

            } catch (Exception e) {
                if(isCancelled()){
                    return false;
                }

                Log.e(LOG_TAG, "Could not load file: " + e);

                emitImageLoadingFailed("Failed to load source file.");
                return false;

            } finally {
                try {
                    if(istr != null){
                        istr.close();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Could not close input stream: " + e);
                }
            }

            emitEvent("onImageDownloaded", null);
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
            emitImageLoadingFailed(errorMessage);
        }
    }

    private void emitImageLoadingFailed(String error) {
        WritableMap params = Arguments.createMap();
        params.putString("error", error);
        emitEvent("onImageLoadingFailed", params);
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

    public void cleanUp(){
        if (imageLoaderTask != null) {
            imageLoaderTask.cancel(true);
        }

        this.setEventListener(null);
        _context.removeLifecycleEventListener(this);

        try{
            this.pauseRendering();
            this.shutdown();
        }
        catch(Exception e){

        }
    }

    @Override
    public void onHostResume() {
        //Log.i(LOG_TAG, "onHostResume");
    }

    @Override
    public void onHostPause() {
        //Log.i(LOG_TAG, "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        this.cleanUp();
        //Log.i(LOG_TAG, "onHostDestroy");
    }
}
