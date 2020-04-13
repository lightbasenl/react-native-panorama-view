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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class PanoramaViewManager extends SimpleViewManager<PanoramaView> {
    public static final String REACT_CLASS = "PanoramaView";

    ReactApplicationContext mCallerContext;

    public PanoramaViewManager(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public PanoramaView createViewInstance(ThemedReactContext context) {
        return new PanoramaView(context);
    }

    @Override
    public void onDropViewInstance(PanoramaView view) {
        view.onHostDestroy();
        super.onDropViewInstance(view);
    }

    @Override
    public @Nullable Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "onImageLoadingFailed",
                MapBuilder.of("registrationName", "onImageLoadingFailed"),
                "onImageDownloaded",
                MapBuilder.of("registrationName", "onImageDownloaded"),
                "onImageLoaded",
                MapBuilder.of("registrationName", "onImageLoaded")
        );
    }


    @ReactProp(name = "imageUrl")
    public void setImageSource(PanoramaView view, String value) {
        view.setImageSource(value);
    }

    @ReactProp(name = "dimensions")
    public void setDimensions(PanoramaView view, ReadableMap dimensions) {
        view.setDimensions(dimensions);
    }

    @ReactProp(name = "inputType")
    public void setInputType(PanoramaView view, String inputType) {
        view.setInputType(inputType);
    }

    @ReactProp(name = "enableTouchTracking")
    public void setEnableTouchTracking(PanoramaView view, boolean enableTouchTracking) {
        view.setEnableTouchTracking(enableTouchTracking);
    }
}