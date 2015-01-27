# Backgroundvideo

##### A simple Cordova/Phonegap plugin to capture video and then display it onscreen via a transparent overlay without affecting app functionality.


##How to use
###Install
```
cordova plugin add io.iclue.backgroundvideo
```
###Usage
```
window.Plugin.backgroundvideo.start(filename, cameradirection, successfn, errorfn);
```

###Getting started
######start recording
```
window.Plugin.backgroundvideo.start('myvideo', 'front', null, null);
```
######stop recording
```
window.Plugin.backgroundvideo.stop(successFn, errorFn);
```
###Other bits
**Camera**
'front' or 'back' to specify camera direction.

**File**
- Outputs as mp4. You do not need to specify file extension.
- Video files are saved to approot/tmp folder.

###Support
Please use the github issue tracker and we will come back to you as soon as we can.

###Contribution
There's lots of Android phones all with their own quirks so we'd love it if you could contribute and help us support all of the devices out there. Email me at james@iclue.io.

