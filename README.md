# cordova-background-video

##### A simple Cordova/Phonegap plugin to capture video and then display it onscreen via a transparent overlay without affecting app functionality.


## How to use
### Install
```
cordova plugin add https://github.com/jamesla/backgroundvideo.git
```
### Usage
```
cordova.plugins.backgroundvideo.start(fileStorage, filename, camera, quality, successfn, errorfn);
```

### Getting started
###### start recording
```
cordova.plugins.backgroundvideo.start('cacheDirectory', 'myvideo', 'front', 'medium', null, null);
```
###### stop recording
```
cordova.plugins.backgroundvideo.stop(successFn, errorFn);
```
### Other bits
**Quality**
can be medium, low, high

**Camera**
'front' or 'back' to specify camera direction.

**File**
- Outputs as mp4. You do not need to specify file extension.
- Video files are saved to approot/tmp folder (cordova.plugins.backgroundvideo.stop() will return the file path).

### File System Layouts

Although technically an implementation detail, it can be very useful to know how
the `cordova.file.*` properties map to physical paths on a real device.

#### iOS File System Layout

| Device Path                                    | `cordova.file.*`            | `iosExtraFileSystems` | r/w? | persistent? | OS clears | sync | private |
|:-----------------------------------------------|:----------------------------|:----------------------|:----:|:-----------:|:---------:|:----:|:-------:|
| `/var/mobile/Applications/<UUID>/`             | applicationStorageDirectory | -                     | r    |     N/A     |     N/A   | N/A  |   Yes   |
| &nbsp;&nbsp;&nbsp;`appname.app/`               | applicationDirectory        | bundle                | r    |     N/A     |     N/A   | N/A  |   Yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`www/`     | -                           | -                     | r    |     N/A     |     N/A   | N/A  |   Yes   |
| &nbsp;&nbsp;&nbsp;`Documents/`                 | documentsDirectory          | documents             | r/w  |     Yes     |     No    | Yes  |   Yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`NoCloud/` | -                           | documents-nosync      | r/w  |     Yes     |     No    | No   |   Yes   |
| &nbsp;&nbsp;&nbsp;`Library`                    | -                           | library               | r/w  |     Yes     |     No    | Yes? |   Yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`NoCloud/` | dataDirectory               | library-nosync        | r/w  |     Yes     |     No    | No   |   Yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`Cloud/`   | syncedDataDirectory         | -                     | r/w  |     Yes     |     No    | Yes  |   Yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`Caches/`  | cacheDirectory              | cache                 | r/w  |     Yes*    |  Yes\*\*\*| No   |   Yes   |
| &nbsp;&nbsp;&nbsp;`tmp/`                       | tempDirectory               | -                     | r/w  |     No\*\*  |  Yes\*\*\*| No   |   Yes   |


#### Android File System Layout

| Device Path                                     | `cordova.file.*`            | `AndroidExtraFileSystems` | r/w? | persistent? | OS clears | private |
|:------------------------------------------------|:----------------------------|:--------------------------|:----:|:-----------:|:---------:|:-------:|
| `file:///android_asset/`                        | applicationDirectory        | assets                    | r    |     N/A     |     N/A   |   Yes   |
| `/data/data/<app-id>/`                          | applicationStorageDirectory | -                         | r/w  |     N/A     |     N/A   |   Yes   |
| &nbsp;&nbsp;&nbsp;`cache`                       | cacheDirectory              | cache                     | r/w  |     Yes     |     Yes\* |   Yes   |
| &nbsp;&nbsp;&nbsp;`files`                       | dataDirectory               | files                     | r/w  |     Yes     |     No    |   Yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`Documents` |                             | documents                 | r/w  |     Yes     |     No    |   Yes   |
| `<sdcard>/`                                     | externalRootDirectory       | sdcard                    | r/w  |     Yes     |     No    |   No    |
| &nbsp;&nbsp;&nbsp;`Android/data/<app-id>/`      | externalApplicationStorageDirectory | -                 | r/w  |     Yes     |     No    |   No    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`cache`     | externalCacheDirectry       | cache-external            | r/w  |     Yes     |     No\*\*|   No    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`files`     | externalDataDirectory       | files-external            | r/w  |     Yes     |     No    |   No    |


### Support
Please use the github issue tracker and we will come back to you as soon as we can.

### Contribution
There's lots of Android phones all with their own quirks so we'd love it if you could contribute and help us support all of the devices out there.
