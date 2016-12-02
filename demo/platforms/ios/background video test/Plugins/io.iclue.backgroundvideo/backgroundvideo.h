#import <UIKit/UIKit.h>

#import <Cordova/CDVPlugin.h>
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>

#define FileExtension @".mp4"

@interface backgroundvideo : CDVPlugin <UITabBarDelegate, AVCaptureFileOutputRecordingDelegate> {
}

@property AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, retain) UIView* parentView;
@property (nonatomic, retain) UIView* view;
@property AVCaptureSession *session;
@property AVCaptureMovieFileOutput *output;
@property NSString *outputPath;
@property NSString *token;
@property (assign) BOOL isFinished;
@property NSString *camera;

- (void)start:(CDVInvokedUrlCommand *)command;
- (void)stop:(CDVInvokedUrlCommand *)command;


@end
