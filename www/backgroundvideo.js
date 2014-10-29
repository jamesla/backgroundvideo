var backgroundvideo = {
    start : function(filename, camera, successFunction, errorFunction) {
    	camera = camera || 'back';
        cordova.exec(successFunction, errorFunction, "backgroundvideo","start", [filename, camera]);
    },
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, "backgroundvideo","stop", []);
    }
};

//module.exports = backgroundvideo;

//todo:fixthis
window.Plugin.backgroundvideo = backgroundvideo;