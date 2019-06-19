//
//  PanoramaView.m
//
//  Created by Rody Molenaar on 08/02/19.
//  Copyright © 2019 Lightbase B.V. All rights reserved.
//

#import <React/RCTImageLoader.h>
#import "PanoramaView.h"

@implementation PanoramaView {
    UIImage *_image;
    CTPanoramaView *_panoView;
    __weak RCTBridge *_bridge;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        _bridge = bridge;
    }

    return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];

    _panoView = [[CTPanoramaView alloc] initWithFrame:frame];
    _panoView.startAngle = 3.45;

    [self addSubview:_panoView];

    return self;
}

- (void)layoutSubviews
{
    float rootViewWidth = self.frame.size.width;
    float rootViewHeight = self.frame.size.height;

    _panoView.frame = CGRectMake(0, 0, rootViewWidth, rootViewHeight);
}


-(void)setImageUrl:(NSString *)imageUrl
{
    NSLog(@"%@", [NSString stringWithFormat:@"[PanoramaView] Image url: %@", imageUrl]);

    __weak PanoramaView *weakSelf = self;

    if (imageUrl.length && _bridge.imageLoader) {
        NSLog(@"[PanoramaView] Getting ready to load.");

        [_bridge.imageLoader loadImageWithURLRequest: [RCTConvert NSURLRequest: imageUrl]
                                            callback:^(NSError *error, UIImage *image) {
                                                if (image == nil && error) {
                                                    [self imageLoadingFailed];
                                                } else {
                                                    NSLog(@"[PanoramaView] Loading image.");
                                                    dispatch_async([weakSelf methodQueue], ^{
                                                        if (image) {
                                                            self->_panoView.image = image;
                                                            [self imageLoaded];
                                                        } else {
                                                            [self imageLoadingFailed];
                                                        }
                                                    });
                                                }
                                            }];
    } else {
        if (_bridge == nil) {
            NSLog(@"[PanoramaView] Bridge not available.");
        }
        if (!imageUrl.length) {
            NSLog(@"[PanoramaView] Image argument not sufficient.");
        }
        if (!_bridge.imageLoader) {
            NSLog(@"[PanoramaView] Bridge image loader not available.");
        }
        NSLog(@"[PanoramaView] Image argument not sufficient or bridge image loader not available.");
        [self imageLoadingFailed];
    }
}

-(void)setEnableTouchTracking:(BOOL)enableTouchTracking
{
    if (enableTouchTracking) {
        _panoView.controlMethod = CTPanoramaControlMethodTouch;
    }
}

- (void)imageLoadingFailed {
    NSLog(@"[PanoramaView] Could not fetch image.");

    if (_onImageLoadingFailed) {
        _onImageLoadingFailed(nil);
    }
}

- (void)imageLoaded {
    NSLog(@"[PanoramaView] Image loaded.");

    if (_onImageLoaded) {
        _onImageLoaded(nil);
    }
}


@end
