import React from 'react';
import {PanoramaView} from '@lightbase/react-native-panorama-view';
import {View, Dimensions, StyleSheet} from 'react-native';

const App = () => (
  <View style={styles.container}>
    <PanoramaView
      style={styles.viewer}
      dimensions={{height: 230, width: Dimensions.get('window').width}}
      inputType="mono"
      imageUrl="https://raw.githubusercontent.com/googlevr/gvr-android-sdk/master/assets/panoramas/testRoom1_2kMono.jpg"
    />
  </View>
);

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  viewer: {
    height: '100%',
  },
});

export default App;
