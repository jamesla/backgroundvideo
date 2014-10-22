#import "backgroundvideo.h"
#import <MediaPlayer/MediaPlayer.h>
#import <AVFoundation/AVFoundation.h>

@implementation backgroundvideo

@synthesize parentView, view, callbackId, session, output, outputPath, mergedOutputPath, isFinished, previewLayer;

#ifndef __IPHONE_3_0
@synthesize webView;
#endif

-(CDVPlugin*) initWithWebView:(UIWebView*)theWebView
{
    self = (backgroundvideo*)[super initWithWebView:theWebView];
    return self;
}

#pragma mark -
#pragma mark backgroundvideo

- (void) StartRecording:(CDVInvokedUrlCommand *)command
{
    //stop the device from being able to sleep
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    
    self.token = [command.arguments objectAtIndex:0];
    
    //get rid of the old dumb view (causes issues if the app is resumed)
    self.parentView = nil;
    
    //make the view
    CGRect viewRect = CGRectMake(
                                 1,
                                 1,
                                 self.webView.superview.frame.size.width,
                                 self.webView.superview.frame.size.height
                                 );
    self.parentView = [[UIView alloc] initWithFrame:viewRect];
    [self.webView.superview addSubview:self.parentView];
    
    self.parentView.backgroundColor = [UIColor clearColor];
    self.view = [[UIView alloc] initWithFrame: self.parentView.bounds];
    [self.parentView addSubview: view];
    view.alpha = 0.2f;
    //view.userInteractionEnabled = NO;
    self.parentView.userInteractionEnabled = NO;
    
    //camera stuff
    
    //Capture session
    session = [[AVCaptureSession alloc] init];
    [session setSessionPreset:AVCaptureSessionPresetLow];
    
    //Get the front camera and set the capture device
    AVCaptureDevice *inputDevice = [self frontFacingCameraIfAvailable];
    
    
    //write the file
    outputPath = [self getFileName];
    NSURL *fileURI = [[NSURL alloc] initFileURLWithPath:outputPath];
    
    //capture device output
    CMTime maxDuration = CMTimeMakeWithSeconds(1800, 1);
    
    output = [[AVCaptureMovieFileOutput alloc]init];
    output.maxRecordedDuration = maxDuration;
    
    
    if ( [session canAddOutput:output])
        [session addOutput:output];
    
    //Capture audio input
    AVCaptureDevice *audioCaptureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeAudio];
    AVCaptureDeviceInput *audioInput = [AVCaptureDeviceInput deviceInputWithDevice:audioCaptureDevice error:nil];
    if (audioInput)
        [session addInput:audioInput];
    
    
    
    //Capture device input
    AVCaptureDeviceInput *deviceInput = [AVCaptureDeviceInput deviceInputWithDevice:inputDevice error:nil];
    if ( [session canAddInput:deviceInput] )
        [session addInput:deviceInput];
    
    
    //preview view
    self.previewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:session];
    [self.previewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
    
    CALayer *rootLayer = [[self view] layer];
    [rootLayer setMasksToBounds:YES];
    [self.previewLayer setFrame:CGRectMake(-70, 0, rootLayer.bounds.size.height, rootLayer.bounds.size.height)];
    [rootLayer insertSublayer:self.previewLayer atIndex:0];
    
    //go
    NSLog(@"it started recording");
    [session startRunning];
    [output startRecordingToOutputFileURL:fileURI recordingDelegate:self ];
    
    //return true to ensure callback fires
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

- (void)StopRecording:(CDVInvokedUrlCommand *)command
{
    NSLog(@"stopping the recording");
    //[self.previewLayer removeFromSuperlayer];
    [output stopRecording];
    //[session stopRunning];
    self.view.alpha = 0;
}


- (void)SerializeAndUploadRecording:(CDVInvokedUrlCommand *)command
{
    [self StopRecording:nil];
    
    //put this in another thread so it doesn't block
    [self.commandDelegate runInBackground:^{
        [self mergeTestVideos];
        
        //check the other asynchronous methods have finished
        while(!self.isFinished){
            sleep(1);
        }
        
        //build return object
        NSURL *fileURI = [[NSURL alloc] initFileURLWithPath:self.mergedOutputPath];
        NSString *name = [[NSString alloc] initWithFormat:@"%@", fileURI.pathComponents[fileURI.pathComponents.count-1]];
        NSString *path = [[NSString alloc] initWithFormat:@"%@", fileURI];
        NSString *pathNoPrefix = [path substringFromIndex:7];
        
        //nice little hack for ios 6 as it randomly puts localhost on the front of the filepath
        NSString *doespathcontainlocalhost = [pathNoPrefix substringToIndex:9];
        if ([doespathcontainlocalhost isEqualToString:@"localhost"]) {
            path = [pathNoPrefix substringFromIndex:9];
            pathNoPrefix = path;
        }
        
        
        
        
        uint64_t fileSize = [[[NSFileManager defaultManager] attributesOfItemAtPath:self.mergedOutputPath error:nil] fileSize];
        NSLog(@"mergedoutputpath=%@", self.mergedOutputPath);
        if([[NSFileManager defaultManager] fileExistsAtPath:self.mergedOutputPath]){
            NSLog(@"merged file exists");
        }

        
        NSDictionary *dictVideoDetails = [[NSDictionary alloc]initWithObjectsAndKeys:
                                          name, @"name",
                                          pathNoPrefix, @"path",
                                          [NSString stringWithFormat:@"%llu", fileSize], @"size",
                                          nil];
        
        NSArray *videoDetails = [[NSArray alloc]initWithObjects:dictVideoDetails, nil];
        
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:videoDetails options:NSJSONWritingPrettyPrinted error:nil];
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        NSString *escapedJsonString = [jsonString stringByReplacingOccurrencesOfString:@"\\" withString:@""];
        NSLog(@"escaped json= %@",escapedJsonString);
        
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:escapedJsonString];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    }];
    
}

-(NSString*)getFileName
{
    int fileNameIncrementer = 1;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSString *tempPath = [[NSString alloc] initWithFormat:@"%@%@-part-%i%@",NSTemporaryDirectory(), self.token, fileNameIncrementer, FileExtension];
    
    while ([fileManager fileExistsAtPath:tempPath]) {
        tempPath = [NSString stringWithFormat:@"%@%@-part-%i%@",NSTemporaryDirectory(), self.token, fileNameIncrementer, FileExtension];
        fileNameIncrementer++;
    }
    
    return tempPath;
}

-(NSString*)getMergedVideoFileName
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSString *fileName = [NSString stringWithFormat:@"%@",self.token];
    NSString *fullPath = [NSString stringWithFormat:@"%@%@%@", NSTemporaryDirectory(), fileName, FileExtension ];
    NSString *filePostfix = [NSString stringWithFormat:@"-part-0"];
    NSString *locationOfPartZeroFile = [NSString stringWithFormat:@"%@%@%@%@", NSTemporaryDirectory(), fileName,filePostfix,FileExtension];
    
    if([fileManager fileExistsAtPath:fullPath]){
        //NSLog(@"File exists at full path");
        
        // if token-part-0.mp4 and token.mp4 exist, delete token-part-0.mp4 as we already have a more up to date merge at token.mp4
        // and then move token.mp4 to token-part-0.mp4
        if([fileManager fileExistsAtPath: locationOfPartZeroFile]){
            [fileManager removeItemAtPath:locationOfPartZeroFile error:nil];
        }
        
        [fileManager moveItemAtPath:fullPath toPath:locationOfPartZeroFile error:nil];
    }
    return [NSString stringWithFormat:@"%@%@%@", NSTemporaryDirectory(), fileName, FileExtension ];
}

-(void)mergeTestVideos
{
    self.isFinished = NO;
    //setup composition and track
    AVMutableComposition *composition = [[AVMutableComposition alloc]init];
    AVMutableCompositionTrack *track = [composition addMutableTrackWithMediaType:AVMediaTypeVideo preferredTrackID:kCMPersistentTrackID_Invalid];
    AVMutableCompositionTrack *audio = [composition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
    
    
    //setup file manager
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    
    // trys to save the merged file as token.mp4
    self.mergedOutputPath = [self getMergedVideoFileName];
    
    NSArray *files = [fileManager contentsOfDirectoryAtPath:NSTemporaryDirectory() error:nil];
    if (files == nil) {
        //no files to merge
        return;
    }
    
    //files = [files sortedArrayUsingSelector:@selector(caseInsensitiveCompare:)];
    
    CMTime currentTrackLength = kCMTimeZero;
    
    for (NSString *file in files) {
        
        //NSLog(@"files = %@", files );
        
        NSRange tokenRange = [file rangeOfString:self.token options:NSCaseInsensitiveSearch];
        
        if(tokenRange.location == 0){
            NSLog(@"adding: %@",file);
            NSString *assetPath = [NSString stringWithFormat:@"%@%@", NSTemporaryDirectory(),file];
            AVAsset *asset = [AVAsset assetWithURL:[NSURL fileURLWithPath:assetPath]];
            
            if(asset.isReadable && asset.isPlayable && asset.isExportable){
                [track insertTimeRange:CMTimeRangeMake(kCMTimeZero, asset.duration) ofTrack:[[asset tracksWithMediaType:AVMediaTypeVideo]objectAtIndex:0] atTime:currentTrackLength  error:nil];
                [audio insertTimeRange:CMTimeRangeMake(kCMTimeZero, asset.duration) ofTrack:[[asset tracksWithMediaType:AVMediaTypeAudio]objectAtIndex:0] atTime:currentTrackLength error:nil];
            }
            
            //keep track of total length of video
            currentTrackLength = CMTimeAdd(currentTrackLength, asset.duration);
        }
    }
    
    //exporter related
    AVAssetExportSession *exporter = [[AVAssetExportSession alloc] initWithAsset:composition presetName:AVAssetExportPresetPassthrough];
    
    
    exporter.outputURL=[NSURL fileURLWithPath:mergedOutputPath];
    exporter.outputFileType = AVFileTypeMPEG4;
    exporter.shouldOptimizeForNetworkUse = YES;
    
    [exporter exportAsynchronouslyWithCompletionHandler:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            [self exportDidFinish:exporter];
        });
    }];
    
    
    
    
}

-(void)exportDidFinish:(AVAssetExportSession*)session {
    NSLog(@"export method");
    NSLog(@"%i", session.status);
    NSLog(@"%@", session.error);
    
    //clean up son
    if(session.status == 3){
        
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSArray *files = [fileManager contentsOfDirectoryAtPath:NSTemporaryDirectory() error:nil];
        if (files != nil) {
            for (NSString *file in files) {
                NSRange tokenRange = [file rangeOfString:self.token options:NSCaseInsensitiveSearch];
                NSRange isPartFile = [file rangeOfString:@"part" options:NSCaseInsensitiveSearch];
                
                if(tokenRange.location == 0 && isPartFile.length == 4){
                    NSString * fullpath = [NSString stringWithFormat:@"%@%@", NSTemporaryDirectory(), file];
                    NSLog(@"Deleting: %@",fullpath);
                    [fileManager removeItemAtPath:fullpath error:nil];
                }
            }
        }
    }
    
    self.isFinished = YES;
    
    
    
    
    
}

-(AVCaptureDevice *)frontFacingCameraIfAvailable
{
    NSArray *videoDevices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    AVCaptureDevice *captureDevice = nil;
    for (AVCaptureDevice *device in videoDevices)
    {
        if (device.position == AVCaptureDevicePositionFront)
        {
            captureDevice = device;
            break;
        }
    }
    
    //  couldn't find one on the front, so just get the default video device.
    if ( ! captureDevice)
    {
        captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    }
    
    return captureDevice;
}

- (void)captureOutput:(AVCaptureFileOutput *)captureOutput didFinishRecordingToOutputFileAtURL:(NSURL *)outputFileURL fromConnections:(NSArray *)connections error:(NSError *)error
{
}

@end