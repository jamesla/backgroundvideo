cordova.define("io.iclue.backgroundvideo.backgroundvideo", function(require, exports, module) { 
var cordova = require('cordova');

var backgroundvideo = {
    start : function(filename, camera, successFunction, errorFunction) {
    	camera = camera || 'back';
        cordova.exec(successFunction, errorFunction, "backgroundvideo","start", [filename, camera, dontRecordAudio]);
    },
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, "backgroundvideo","stop", []);
    }
};

module.exports = backgroundvideo;
window.Plugin.backgroundvideo = backgroundvideo;

});
