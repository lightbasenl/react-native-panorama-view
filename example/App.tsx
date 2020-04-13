import React, { Component } from "react";
import { Text, View, StyleSheet, Dimensions } from "react-native";
import { PanoramaView } from "react-native-panorama-view";


export default class App extends Component {
  render() {
    return (
      <View style={styles.container}>
        <PanoramaView
          style={styles.viewer}
          dimensions={{ height: 230, width: Dimensions.get("window").width }}
          inputType="mono"
          imageUrl="https://raw.githubusercontent.com/googlevr/gvr-android-sdk/master/assets/panoramas/testRoom1_2kMono.jpg"
          enableTouchTracking={true}
        />
        <PanoramaView
          style={styles.viewer}
          dimensions={{ height: 230, width: Dimensions.get("window").width }}
          inputType="stereo"
          imageUrl="https://raw.githubusercontent.com/googlevr/gvr-android-sdk/master/assets/panoramas/testRoom1_2kStereo.jpg"
          enableTouchTracking={false}
        />
        <View style={styles.details}>
          <Text style={styles.title}>My wonderful garage</Text>
          <Text style={styles.text}>This is my awesome garage! Do you like it?</Text>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  viewer: {
    height: 230,
  },
  details: {
    flexGrow: 1,
    padding: 16,
  },
  title: {
    color: "#000",
    fontSize: 18,
    fontWeight: "600",
    marginBottom: 10,
  },
  text: {
    color: "#333",
    fontSize: 14,
    marginBottom: 10,
  },
});
