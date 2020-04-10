import * as React from "react";
import { requireNativeComponent, Platform } from "react-native";

interface Props {
  imageUrl: string;
  dimensions?: { width: number; height: number }; // Android-only
  inputType?: "mono" | "stereo"; // Android-only
  enableTouchTracking?: boolean;
  onImageLoadingFailed?: () => void;
  onImageLoaded?: () => void;
  style: ViewStyle;
}