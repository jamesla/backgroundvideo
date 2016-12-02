cordova.define("io.iclue.backgroundvideo.backgroundvideo", function(require, exports, module) { 
var cordova = require('cordova');

var backgroundvideo = {
    start : function(filename, camera, recordAudio, successFunction, errorFunction) {
    	camera = camera || 'back';
        cordova.exec(successFunction, errorFunction, "backgroundvideo","start", [filename, camera, recordAudio]);
    },
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, "backgroundvideo","stop", []);
    }
};

module.exports = backgroundvideo;
window.Plugin.backgroundvideo = backgroundvideo;

});
