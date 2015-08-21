#!/usr/bin/env bash

echo ----copying files from plugin project----
pwd
cp -R ../src/* plugins/io.iclue.backgroundvideo/src/
cp -R ../www/* plugins/io.iclue.backgroundvideo/www/
cp ../plugin.xml plugins/io.iclue.backgroundvideo/

