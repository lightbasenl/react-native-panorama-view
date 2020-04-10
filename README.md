# react-native-panorama-view

React Native Panorama viewer for Android and iOS.

Fork from https://github.com/lightbasenl/react-native-panorama-view
Since I couldn't deal with .ts and building BS, I've removed those.

Work in progress for local images

![Example](https://raw.githubusercontent.com/lightbasenl/react-native-panorama-view/master/example.gif)

## Getting started

`$ npm install @lightbase/react-native-panorama-view --save`

`$ yarn add @lightbase/react-native-panorama-view`

### Mostly automatic installation

`$ react-native link @lightbase/react-native-panorama-view`

`$ cd ios && pod install`

### Manual installation

#### iOS (Cocoapods)

This guide assumes you've already set-up your React Native project to use Cocoapods.

1. Open up `ios/Podfile`
2. Add `pod 'PanoramaView', :path => '../node_modules/@lightbase/react-native-panorama-view'` to your dependency block.
3. Run `$ pod install`

We recommend using a minimum platform version of at least 9.0 for your application to ensure that the correct
dependency versions are used. To do this add `platform :ios, '9.0'` to the top of your `ios/Podfile`.

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`

- Add `import nl.lightbase.PanoramaViewPackage;` to the imports at the top of the file
- Add `new PanoramaViewPackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:
   ```
   include ':@lightbase_react-native-panorama-view'
   project(':@lightbase_react-native-panorama-view').projectDir = new File(rootProject.projectDir, 	'../node_modules/@lightbase/react-native-panorama-view/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
     implementation project(':@lightbase_react-native-panorama-view')
   ```

## Troubleshooting iOS

If you're app doesn't compile due to Swift or linker errors. Follow these steps.

1. Make sure you have defined a `SWIFT_VERSION` in your project.
2. Add `/usr/lib/swift` as the first argument to your **Runpath Search Paths**.
3. Add `"$(TOOLCHAIN_DIR)/usr/lib/swift/$(PLATFORM_NAME)"` to your **Library Search Paths**.

## Usage

You can size your panorama anyway you'd like using the regular `View` styles.

Here are some examples:

### Embed a panorama as a part of your screen

```tsx
import { PanoramaView } from "@lightbase/react-native-panorama-view";

const PanoramaDetails = () => (
  <View style={styles.container}>
    <PanoramaView
      style={styles.viewer}
      dimensions={{ height: 230, width: Dimensions.get("window").width }}
      inputType="mono"
      imageUrl="https://raw.githubusercontent.com/googlevr/gvr-android-sdk/master/assets/panoramas/testRoom1_2kMono.jpg"
    />
  </View>
);

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  viewer: {
    height: 230
  }
});
```

### Fullscreen panorama

```tsx
import { PanoramaView } from "@lightbase/react-native-panorama-view";

const FullScreenPanorama = () => (
  <PanoramaView
    style={{ flex: 1 }}
    dimensions={{
      height: Dimensions.get("window").height,
      width: Dimensions.get("window").width
    }}
    inputType="mono"
    imageUrl="https://raw.githubusercontent.com/googlevr/gvr-android-sdk/master/assets/panoramas/testRoom1_2kMono.jpg"
  />
);
```

## Props

| Name                 | Type                                | Description                                                                                               |
| -------------------- | ----------------------------------- | --------------------------------------------------------------------------------------------------------- |
| imageUrl             | string                              | Remote URI for fetching the panorama image.                                                               |
| enableTouchTracking  | boolean                             | Enables dragging the panorama by touch when `true`.                                                       |
| onImageLoadingFailed | callback                            | Fired when something goes wrong while trying to load the panorama.                                        |
| onImageLoaded        | callback                            | Fired when the image was successfully loaded.                                                             |
| style                | ViewStyle                           | Allows to style the `PanoramaView` like a regular `View`.                                                 |
| inputType            | 'mono' or 'stereo'                  | Specify the type of panorama source image. **Android only**                                               |
| dimensions           | `{ height: number, width: number }` | Is used to prevent loading unnecessarily large files into memory. **Currently required for Android only** |
