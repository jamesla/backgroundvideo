# Backgroundvideo

##### A simple Cordova/Phonegap plugin to capture video and then display it onscreen via a transparent overlay without affecting app functionality.


##How to use
###Install
```
cordova plugin add io.iclue.backgroundvideo
```
###Usage
```
window.Plugins.backgroundvideo.start(successfn, errorfn, filename, cameradirection);
```

###Getting started
######start recording
```
window.Plugins.backgroundvideo.start(null, null, 'myvideo', 'front');
```
######stop recording
```
window.Plugins.backgroundvideo.stop(successFn, errorFn);
```
###Options
**Camera**
'front' or 'back' to specify front or camera

**File**
Outputs as mp4. You do not need to specify file extension.

###Support
Please use the github issue tracker and we will come back to you as soon as we can.

###Contribution
There's lots of Android phones all with their own quirks so we'd love it if you could contribute and help us support all of the devices out there.

