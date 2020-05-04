//
//  PanoramaView.h
//
//  Created by Rody Molenaar on 08/02/19.
//  Copyright © 2019 Lightbase B.V. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <React/RCTView.h>

@import CTPanoramaView;

@class RCTBridge;

@interface PanoramaView : RCTView

- (instancetype)initWithBridge:(RCTBridge *)bridge;

@property (nonatomic, copy) UIImage* image;
@property (nonatomic, copy) NSString* imageUrl;
@property (nonatomic, assign) BOOL _enableTouchTracking;
@property (nonatomic, copy) RCTDirectEventBlock onImageLoadingFailed;
@property (nonatomic, copy) RCTDirectEventBlock onImageDownloaded;
@property (nonatomic, copy) RCTDirectEventBlock onImageLoaded;

@end
