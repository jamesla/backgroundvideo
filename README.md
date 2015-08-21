# Backgroundvideo

##### A simple Cordova/Phonegap plugin to capture video and then display it onscreen via a transparent overlay without affecting app functionality.


##How to use
###Install
```
cordova plugin add io.iclue.backgroundvideo
```
###Usage
```
cordova.plugins.backgroundvideo.start(filename, cameradirection, successfn, errorfn);
```

###Getting started
######start recording
```
cordova.plugins.backgroundvideo.start('myvideo', 'front', null, null);
```
######stop recording
```
cordova.plugins.backgroundvideo.stop(successFn, errorFn);
```
###Other bits
**Camera**
'front' or 'back' to specify camera direction.

**File**
- Outputs as mp4. You do not need to specify file extension.
- Video files are saved to approot/tmp folder (cordova.plugins.backgroundvideo.stop() will return the file path).

###Support
Please use the github issue tracker and we will come back to you as soon as we can.

###Contribution
There's lots of Android phones all with their own quirks so we'd love it if you could contribute and help us support all of the devices out there. 

##How to run the demo
###
```
git clone https://github.com/jamesla/backgroundvideo.git
cd backgroundvideo/demo
cordova plugin add io.iclue.backgroundvideo
cordova plugin add cordova-plugin-console
cordova platform add ios
cordova run --device
```
