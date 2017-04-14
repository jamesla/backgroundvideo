var cordova = require('cordova');

var backgroundvideo = {
    start : function(fileStorage, filename, camera, quality, successFunction, errorFunction) {
        camera = camera || 'back';
        cordova.exec(successFunction, errorFunction, 'backgroundvideo', 'start', [fileStorage, filename, camera, quality]);
    },
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, 'backgroundvideo','stop', []);
    }
};

module.exports = backgroundvideo;
