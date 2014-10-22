var backgroundvideo = {
        start : function(successFunction, errorFunction, filename, camera) {
            cordova.exec(successFunction, errorFunction, "backgroundvideo","start", [filename, camera]);
        },
        stop : function(successFunction, errorFunction) {
            cordova.exec(successFunction, errorFunction, "backgroundvideo","stop", []);
        }
    };

//module.exports = backgroundvideo;

//todo:fixthis
window.Plugin.backgroundvideo = backgroundvideo;


