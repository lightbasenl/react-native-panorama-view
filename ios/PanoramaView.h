//
//  PanoramaView.h
//
//  Created by Rody Molenaar on 08/02/19.
//  Copyright © 2019 Lightbase B.V. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <React/RCTView.h>

#if __has_include("PanoramaView-Swift.h")
#import "PanoramaView-Swift.h"
#endif

#if __has_include("react_native_panorama_view-Swift.h")
#import "react_native_panorama_view-Swift.h"
#endif


@class RCTBridge;

@interface PanoramaView : RCTView

- (instancetype)initWithBridge:(RCTBridge *)bridge;

@property (nonatomic, copy) RCTDirectEventBlock onImageLoadingFailed;
@property (nonatomic, copy) RCTDirectEventBlock onImageDownloaded;
@property (nonatomic, copy) RCTDirectEventBlock onImageLoaded;

@end
