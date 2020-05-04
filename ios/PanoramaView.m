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

    RCTImageLoaderCancellationBlock _cancel;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
    if ((self = [super init])) {
        _bridge = bridge;
        _cancel = nil;
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

    if (_cancel != nil) {
        _cancel();
        _cancel = nil;
    }

    if (imageUrl.length && _bridge) {
        NSLog(@"[PanoramaView] Getting ready to load.");

        _cancel = [[_bridge moduleForName:@"ImageLoader" lazilyLoadIfNecessary:YES] loadImageWithURLRequest:[RCTConvert NSURLRequest:imageUrl] callback:^(NSError *error, UIImage *image) {

            if (error) {
                NSLog(@"[PanoramaView] Loading image error: %@.", error);
                if(self -> _cancel != nil){
                    [self imageLoadingFailed];
                }
            }
            else{
                NSLog(@"[PanoramaView] Image downloaded.");

                dispatch_async([weakSelf methodQueue], ^{
                    [self imageDownloaded];
                });
                dispatch_async([weakSelf methodQueue], ^{
                    if (image) {
                        self->_panoView.image = image;
                        [self imageLoaded];
                    } else {
                        [self imageLoadingFailed];
                    }
                });
            }
            self->_cancel = nil;
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
        //_panoView.controlMethod = CTPanoramaControlMethodTouch;
        _panoView.controlMethod = CTPanoramaControlMethodBoth;
    } else {
        _panoView.controlMethod = CTPanoramaControlMethodMotion;
    }
}

- (void)imageLoadingFailed {
    if (_onImageLoadingFailed) {
        _onImageLoadingFailed(nil);
    }
}

- (void)imageDownloaded {
    if (_onImageDownloaded) {
        _onImageDownloaded(nil);
    }
}

- (void)imageLoaded {
    if (_onImageLoaded) {
        _onImageLoaded(nil);
    }
}

- (void)dealloc
{
    if(_cancel != nil){
        _cancel();
        _cancel = nil;
    }
}

- (void)willMoveToSuperview:(nullable UIView *)newSuperview;
{
    // cleanup on deallocate
    // cancel any request in progress
    if(newSuperview == nil){
        if(_cancel != nil){
            _cancel();
            _cancel = nil;
       }
    }

    [super willMoveToSuperview:newSuperview];
}

@end
