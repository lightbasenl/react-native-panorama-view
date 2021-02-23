import * as React from "react";
import { requireNativeComponent, ViewStyle, Platform } from "react-native";

export type PanoramaViewProps = {
  imageUrl: string;
  dimensions?: { width: number; height: number }; // Android-only
  inputType?: "mono" | "stereo"; // Android-only
  enableTouchTracking?: boolean;
  onImageLoadingFailed?: () => void;
  onImageDownloaded?: () => void;
  onImageLoaded?: () => void;
  style: ViewStyle;
};

export const PanoramaView: React.FC<PanoramaViewProps> = ({
  onImageLoadingFailed,
  onImageDownloaded,
  onImageLoaded,
  dimensions,
  inputType,
  ...props
}) => {
  const _onImageLoadingFailed = () => {
    if (onImageLoadingFailed) {
      onImageLoadingFailed();
    }
  };

  const _onImageLoaded = () => {
    if (onImageLoaded) {
      onImageLoaded();
    }
  };

  const _onImageDownloaded = () => {
    if (onImageDownloaded) {
      onImageDownloaded();
    }
  };

  if (Platform.OS === "android" && !dimensions) {
    console.warn('The "dimensions" property is required for PanoramaView on Android devices.');
    return null;
  }

  if (Platform.OS === "ios" && inputType === "stereo") {
    console.warn("The stereo inputType is currently only supported on Android devices.");
  }

  return (
    <NativePanoramaView
      {...props}
      dimensions={dimensions}
      inputType={inputType}
      onImageDownloaded={_onImageDownloaded}
      onImageLoaded={_onImageLoaded}
      onImageLoadingFailed={_onImageLoadingFailed}
    />
  );
};

export default PanoramaView;

const NativePanoramaView = requireNativeComponent<PanoramaViewProps>("PanoramaView");
