//
//  PanoramaViewManager.m
//
//  Created by Rody Molenaar on 08/02/19.
//  Copyright © 2019 Lightbase B.V. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(PanoramaViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(enableTouchTracking, BOOL);
RCT_EXPORT_VIEW_PROPERTY(onImageLoadingFailed, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onImageDownloaded, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onImageLoaded, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(imageUrl, NSString);

@end
