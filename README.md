MIXARE [![Build Status](https://travis-ci.org/Odyno/mixare.png?branch=development)](https://travis-ci.org/Odyno/mixare)
======
Clone to support Mixare (mix Augmented Reality Engine) a free open source augmented reality browser, which is published under the GPLv3. 

HowTo Build with Maven
======================
This step is the same used on Travis CI: [Read config File](.travis.yml)

* Download a AndroidSDK  ( wget http://dl.google.com/android/android-sdk_r21.1-linux.tgz ; tar xvzf android-sdk_r21.1-linux.tgz )
* Set Eviroment Variable ( export ANDROID_HOME=$(pwd)/android-sdk-linux; export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform )
* Download or Update the SDK ( odyno@Itaca:~$ android update sdk --no-ui --filter 1,2,6,44 -o )
* Install specific AddOns:
  - dowload https://github.com/mosabua/maven-android-sdk-deployer/archive/master.zip
  - unzip
  - go to maven-android-sdk-deployer/platforms
  - run "mvn install -P 4.0.3"
  - go to ../add-ons
  - run "mvn install -P 4.0.3"
* Go to project root and run Maven (mvn clean install)

Refer to Maven-Android-Plugin to other info



