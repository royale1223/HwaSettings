#!/bin/bash

# build apk
ant clean
ant release

# copy apk to script tree
echo "removing /update/system/app/HwaSettings.apk"
rm /update/system/app/HwaSettings.apk
echo "adding /update/system/app/HwaSettings.apk"
cp bin/HwaSettings-release.apk update/system/app/HwaSettings.apk

# create zip
cd update
echo "creating zip"
rm *.zip
zip -r HwaSettings-update.zip ./*
cd ..

#sign zip
echo "signing zip"
jarsigner -verbose -keystore ./testkey.jks -storepass android \
-keypass android -signedjar update/HwaSettings-update-signed.zip \
update/HwaSettings-update.zip androiddebugkey
echo "done"
exit 0
