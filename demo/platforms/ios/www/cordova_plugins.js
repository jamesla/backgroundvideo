cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/io.iclue.backgroundvideo/www/backgroundvideo.js",
        "id": "io.iclue.backgroundvideo.backgroundvideo",
        "clobbers": [
            "cordova.plugins.backgroundvideo"
        ]
    },
    {
        "file": "plugins/cordova-plugin-console/www/logger.js",
        "id": "cordova-plugin-console.logger",
        "clobbers": [
            "cordova.logger"
        ]
    },
    {
        "file": "plugins/cordova-plugin-console/www/console-via-logger.js",
        "id": "cordova-plugin-console.console",
        "clobbers": [
            "console"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "io.iclue.backgroundvideo": "0.0.7",
    "cordova-plugin-console": "1.0.1"
}
// BOTTOM OF METADATA
});