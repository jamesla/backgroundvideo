/* globals window, document, cordova, exports, console */
/* jshint jasmine: true */

'use strict';

exports.defineAutoTests = function() {
    describe('Background video', function () {
        it("should exist", function () {
            expect(cordova.plugins.backgroundvideo).toBeDefined();
        });

        it("should contain a start function", function () {
            expect(cordova.plugins.backgroundvideo.start).toBeDefined();
            expect(typeof cordova.plugins.backgroundvideo.start == 'function').toBe(true);
        });

        it("should contain a stop function", function () {
            expect(cordova.plugins.backgroundvideo.stop).toBeDefined();
            expect(typeof cordova.plugins.backgroundvideo.stop == 'function').toBe(true);
        });
    });
};

/******************************************************************************/
/******************************************************************************/
/******************************************************************************/

exports.defineManualTests = function (contentEl, createActionButton) {
    var recordAudio = false;
    var camera = 'FRONT';
    var filename = 'test-video';
    var content = '<h1>Background Video</h1><div id="status"></div>';

    function Log(tag, value) {
        console.log(tag, value);
        document.getElementById('status').textContent += '\r\n ' + tag + ': '  + value;
    }
    function successFn(a){Log('success', a);}
    function errorFn(a){Log('error', a);}

    // We need to wrap this code due to Windows security restrictions
    // see http://msdn.microsoft.com/en-us/library/windows/apps/hh465380.aspx#differences for details
    if (window.MSApp && window.MSApp.execUnsafeLocalFunction) {
        MSApp.execUnsafeLocalFunction(function() {
            contentEl.innerHTML = content;
        });
    } else {
        contentEl.innerHTML = content;
    }

    createActionButton('Start video', function () {
        cordova.plugins.backgroundvideo.start(filename, camera, recordAudio, successFn, errorFn);
    });

    createActionButton('Stop video', function () {
        cordova.plugins.backgroundvideo.stop(successFn, errorFn);
    });

    createActionButton('Toggle audio', function () { recordAudio = !recordAudio; Log('Record audio set', recordAudio); });
    createActionButton('Toggle camera', function () { camera = (camera === 'FRONT' ? 'BACK' : 'FRONT'); Log('Camera set', camera); });
};
