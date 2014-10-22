#import <UIKit/UIKit.h>

#import <Cordova/CDVPlugin.h>
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>

#define FileExtension @".mp4"

@interface backgroundvideo : CDVPlugin <UITabBarDelegate, AVCaptureFileOutputRecordingDelegate> {
}

@property AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, copy) NSString* callbackId;
@property (nonatomic, retain) UIView* parentView;
@property (nonatomic, retain) UIView* view;
@property AVCaptureSession *session;
@property AVCaptureMovieFileOutput *output;
@property NSString *outputPath;
@property NSString *token;
@property NSString *mergedOutputPath;
@property (assign) BOOL isFinished;


- (NSString*)getFileName;
- (void)StartRecording:(CDVInvokedUrlCommand *)command;
- (void)StopRecording:(CDVInvokedUrlCommand *)command;
- (void)SerializeAndUploadRecording:(CDVInvokedUrlCommand *)command;

@end