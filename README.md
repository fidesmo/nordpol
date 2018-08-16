![Nordpol icon](/nordpol_super_secret_nfc_project.png?raw=true)
# Fidesmo Nordpol - The Android Support Library for NFC

## Goal of the project
To make it as easy as possible to communicate with a NFC device using ISO 14443-4 commands. Specifically we are targeting devices that communicates using ISO 7816-4 APDUs.

## Rationale
Communicating with NFC devices using Android can sometimes be a bit challenging. There is both the new reader mode and the old intent based method. When doing real processing on the card, timeouts starts being an issue and several of the default values require some tweaking. On top of this, on some devices the NFC implementation require some quirks to work properly. We have gathered up our experience from several years of working with NFC on the Android platform into this library.

## Usage

[![Download](https://api.bintray.com/packages/fidesmo/maven/nordpol-android/images/download.svg)](https://bintray.com/fidesmo/maven/nordpol-android/_latestVersion) [![Build Status](https://travis-ci.org/fidesmo/nordpol.svg?branch=master)](https://travis-ci.org/fidesmo/nordpol)

To include Nordpol into your gradle Android build add these imports to your ```build.gradle```:
```
repositories {
    ...
    jcenter()
}
dependencies {
    ...
    //Nordpol
    compile 'com.fidesmo:nordpol-android:{ INSERT VERSION HERE }'
}
```

If you are using proguard, it will remove methods internal to Nordpol
which are called by the android system when a card is detected. To
alleviate this please add the following to your proguard
configuration:
```
# The Nordpol library contains methods that will be invoked by the
# system and will therefore be removed by proguard. This forces
# proguard to keep those methods.
-keep class nordpol.** { *; }
```

You will also be warned that Nordpol uses classes if you are
supporting pre 4.4 devices. The library detects the platform version
use the right methods internally. Add the following to ignore those
warnings:
```
# The Nordpol library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn nordpol.android.**
```

## API Documentation

- [Latest (master/HEAD) Android API](http://nordpol.fidesmo.com.s3-website-eu-west-1.amazonaws.com/master/android/index.html)
- [Latest (master/HEAD) core API](http://nordpol.fidesmo.com.s3-website-eu-west-1.amazonaws.com/master/core/index.html)

There are two tutorials introducing the Nordpol API.
[The first and simplest one](https://developer.fidesmo.com/tutorials/android)
is part of
[a bigger suite of tutorials](https://developer.fidesmo.com/tutorials/javacard)
on how to program for the Fidesmo
Card. [The second one](https://developer.fidesmo.com/tutorials/android-totp)
is slightly more advanced and shows how to interact with an
[OTP device](https://github.com/Yubico/ykneo-oath).

## Building locally

### Tools

We use SBT for building. Get it
[here](http://www.scala-sbt.org/download.html)

The android subproject expects a local.properties file that points to
the Android SDK. So to build you need to create the file
`android/local.properties` (relative to the root directory of the
repository) containing the following:

On UNIX
```
sdk.dir = /path/to/android/sdk
```

On Windows
```
sdk.dir = Z:\\path\to\android\sdk
```

### Building

After cloning the Nordpol project open a terminal in the project folder and
run the SBT command `sbt publishM2` This command will (locally) publish the
library with the version declared in the version.sbt file in the project folder.

Add `mavenLocal()` to the repository configuration section in your Android
project `build.gradle` file, like so:
```
repositories {
    /***
     * Other repositories
     **/
    mavenLocal()
}
```

Import the latest SNAPSHOT version of Nordpol (or whatever version name you gave
your locally published the project). For example like this:
```
compile 'com.fidesmo:nordpol-android:0.1.20-SNAPSHOT'
```

Many operating systems run into caching issues when you try to publish a new
version with the same name as the last version. The project might simply fetch
the same version as the one it fetched before because the name didn't change.
The current suggested workaround for this is changing the version name after
each change.

## Releasing

If you have the right credentials all that is needed is running the SBT
command `sbt release` and you'll be guided through the process.

## Contributions

Additions, bugfixes and issues are very welcome. Added code should be
formatted and structured according to [Googles
styleguide](http://google.github.io/styleguide/javaguide.html). A pull
request should pass the Travis CI before it is merged. If possible
a pull request should be rebased to the most recent master
commit. Don't forget to add yourself to the list of contributors below.

Thanks for your contributions:

- [adriaan-telcred](https://github.com/adriaan-telcred)
