/*global Windows:true, URL:true, WinJS:true */

var cordova = require('cordova');

var EXTENTION = '.mp4';

var oMediaCapture;
var profile;
var captureInitSettings;
var deviceList = [];
var recordState = false;
var storageFile;
var dispRequest = null;

var filename;
var preview;
var cameraFacing;

var startSuccessCb;
var startErrorCb;
var stopSuccessCb;
var stopErrorCb;


function errorHandler(e) {
    console.error(e);
}

function startErrorHander(e) {
    errorHandler(e);
    if(startErrorCb){
        startErrorCb(e);
    }
}

function stopErrorHander(e) {
    errorHandler(e);
    if (stopErrorCb) {
        stopErrorCb(e);
    }
} 


// Identify available cameras.
function enumerateCameras() {
    deviceList = [];

    var deviceInfo = Windows.Devices.Enumeration.DeviceInformation;
    return deviceInfo.findAllAsync(Windows.Devices.Enumeration.DeviceClass.videoCapture)
        .then(function (devices) {
            // Add the devices to deviceList
        
            for (var i = 0; i < devices.length; i++) {
                if (devices[i].enclosureLocation.panel === cameraFacing) {
                    deviceList.push(devices[i]);
                }
            }

            if (deviceList.length === 0) {
                throw 'No camera device found';
            }
    }, startErrorHander);
}

// Initialize the MediaCaptureInitialzationSettings.
function initCaptureSettings() {
    captureInitSettings = null;
    captureInitSettings = new Windows.Media.Capture.MediaCaptureInitializationSettings();
    captureInitSettings.audioDeviceId = "";
    captureInitSettings.videoDeviceId = "";
    captureInitSettings.streamingCaptureMode = Windows.Media.Capture.StreamingCaptureMode.audioAndVideo;
    captureInitSettings.photoCaptureSource = Windows.Media.Capture.PhotoCaptureSource.videoPreview;
    captureInitSettings.realTimeModeEnabled = true;

    if (deviceList.length > 0) {
        captureInitSettings.videoDeviceId = deviceList[0].id;
    }
}

// Create a profile.
function createProfile() {
    profile = Windows.Media.MediaProperties.MediaEncodingProfile.createMp4(
        Windows.Media.MediaProperties.VideoEncodingQuality.qvga //Set Quality of recording.
        );
}

// Start the video capture.
function startMediaCaptureSession() {
    //TODO: change naming convention? so that duplicate files increment vid.mp4, vid_1.mp4 etc
    Windows.Storage.ApplicationData.current.localFolder.createFileAsync(filename + EXTENTION, Windows.Storage.CreationCollisionOption.generateUniqueName)
        .then(function (newFile) {
            storageFile = newFile;
            filename = storageFile.displayName;

            oMediaCapture.startRecordToStorageFileAsync(profile, storageFile)
                .then(function (result) {

                    if (preview) {
                        preview.src = URL.createObjectURL(oMediaCapture, { oneTimeOnly: true });
                        preview.play();
                    }

                    dispRequest = new Windows.System.Display.DisplayRequest();
                    dispRequest.requestActive();

                    recordState = true;

                    if (startSuccessCb) {
                        startSuccessCb();
                    }
                }, startErrorHander);
        });
}

// Stop the video capture.
function stopMediaCaptureSession() {

    if (preview) {
        if (preview.paused)
            preview.pause();
        preview.src = null;
    }

    if (dispRequest !== null) {
        // Deactivate the display request and set the var to null.
        // https://msdn.microsoft.com/en-us/library/windows/apps/jj152725.aspx
        dispRequest.requestRelease();
        dispRequest = null;
    }

    if (oMediaCapture !== null) {
        return oMediaCapture.stopRecordAsync()
            .then(function (result) {
                recordState = false;

                if (stopSuccessCb) {
                    stopSuccessCb(filename + EXTENTION);
                }
            }, stopErrorHander);
    }
}

// Create and initialze the MediaCapture object.
function initMediaCapture() {
    oMediaCapture = null;
    oMediaCapture = new Windows.Media.Capture.MediaCapture();

    oMediaCapture.addEventListener("failed", function (e) {
        releaseMediaCapture();
        startErrorHander(e);
    });

    return oMediaCapture.initializeAsync(captureInitSettings)
        .then(function () {
            // Set Rotation for preview and output file
            // TODO: there is an issue with the video being squished.
            // TODO: This only rotates if the camera is on the front
            //      - The front and back camera had different starting rotations for the test device, requires testing on more devices
            if (cameraFacing === Windows.Devices.Enumeration.Panel.front) {
                oMediaCapture.setPreviewRotation(Windows.Media.Capture.VideoRotation.clockwise270Degrees);
                oMediaCapture.setRecordRotation(Windows.Media.Capture.VideoRotation.clockwise270Degrees);
            } else {
                oMediaCapture.setPreviewRotation(Windows.Media.Capture.VideoRotation.clockwise90Degrees);
                oMediaCapture.setRecordRotation(Windows.Media.Capture.VideoRotation.clockwise90Degrees);
            }
        })
        .then(createProfile, startErrorHander);
}

// Begin initialization.
function initialization() {
    createPreview();

    if (oMediaCapture && recordState) {
        //Already Recording
        return;
    } 

    releaseMediaCapture();

    enumerateCameras()
        .then(initCaptureSettings, startErrorHander)
        .then(initMediaCapture, startErrorHander)
        .then(startMediaCaptureSession, startErrorHander);
}


function disposeMediaCapture() {
    oMediaCapture.close();
    oMediaCapture = null;
}

function releaseMediaCapture() {
    if (oMediaCapture) {
        if (recordState) {
            return stopMediaCaptureSession().then(disposeMediaCapture);
        } else {
            disposeMediaCapture();
        }
    }

    return WinJS.UI.processAll();
}

function createPreview() {
    var id = 'backgroundVideoPreview';
    if (!preview) {
        var exists = document.getElementById(id);

        if (exists) {
            preview = exists;
            return;
        }

        preview = document.createElement("video");
        preview.setAttribute('id', id);
        preview.msZoom = true;      //https://msdn.microsoft.com/en-us/library/windows/apps/hh452807.aspx
        preview.msRealtime = true;  //https://msdn.microsoft.com/en-us/library/windows/apps/hh452742.aspx
        preview.style.cssText = 'pointer-events:none; transform:translate3d(0px,0px,0px); position:fixed; width:100%; height:100%; opacity:0.3;';

        document.body.appendChild(preview);
    }
}

function setCameraFacing(requestedFace) {
    var ns = Windows.Devices.Enumeration.Panel;
    requestedFace = (requestedFace || 'BACK').toUpperCase();

    switch (requestedFace) {
        case 'BACK':   cameraFacing = ns.back;
            break;
        case 'FRONT':  cameraFacing = ns.front;
            break;
        case 'BOTTOM': cameraFacing = ns.bottom;
            break;
        case 'LEFT':   cameraFacing = ns.left;
            break;
        case 'RIGHT':  cameraFacing = ns.right;
            break;
        case 'TOP':    cameraFacing = ns.top;
            break;
        default:       cameraFacing = ns.back;
            break;
    }
    return cameraFacing;
}

// Release Camera on suspend.
Windows.UI.WebUI.WebUIApplication.addEventListener("suspending", function (eventArgs) {
    var deferral = eventArgs.suspendingOperation.getDeferral();
    releaseMediaCapture()
        .then(function () {
            deferral.complete();
        });
}, false);


module.exports = {

    // args will contain :
    //  [0] = filename
    //  [1] = cameraFacing (front/back)
    //  [2] = sucess callback (optional)
    //  [3] = error callback (optional)
    //  ...  it is an array, so be careful


    start: function (successCallback, errorCallback, args) {

        if (args === null || args.length === 0 || args[0] === '') {
            errorCallback('Filename required');
        }

        startSuccessCb = successCallback;
        startErrorCb = errorCallback;
        filename = args[0];

        setCameraFacing(args[1] || null);

        initialization();
    },
    stop: function (successCallback, errorCallback, args) {
        stopSuccessCb = successCallback;
        stopErrorCb = errorCallback;
        stopMediaCaptureSession();
    }
};

require("cordova/exec/proxy").add("backgroundvideo", module.exports);