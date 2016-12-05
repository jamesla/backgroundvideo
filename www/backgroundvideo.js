var cordova = require('cordova');

var backgroundvideo = {
    start : function(filename, camera, shouldRecordAudio, successFunction, errorFunction) {
        camera = camera || 'back';
        cordova.exec(successFunction, errorFunction, "backgroundvideo","start", [filename, camera, shouldRecordAudio]);
    },
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, "backgroundvideo","stop", []);
    }
};

module.exports = backgroundvideo;
