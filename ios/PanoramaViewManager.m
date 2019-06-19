//
//  PanoramaViewManager.m
//
//  Created by Rody Molenaar on 08/02/19.
//  Copyright © 2019 Lightbase B.V. All rights reserved.
//

#import "PanoramaViewManager.h"
#import "PanoramaView.h"
#import <UIKit/UIKit.h>

@implementation PanoramaViewManager

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
    return [[PanoramaView alloc] initWithBridge:self.bridge];
}

RCT_EXPORT_VIEW_PROPERTY(enableTouchTracking, BOOL);
RCT_EXPORT_VIEW_PROPERTY(onImageLoadingFailed, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onImageLoaded, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(imageUrl, NSString);

@end
