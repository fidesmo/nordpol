![Nordpol icon](/nordpol_super_secret_nfc_project.png?raw=true)
# Fidesmo Nordpol - The easiest way to connect a Fidesmo Device

## Goal of the project
To make it as easy as possible to communicate with a NFC device using ISO 14443-4 commands. Specifically we are targeting devices that communicates using ISO 7816-4 APDUs.

## Rationale
Communicating with NFC devices using Android can sometimes be a bit challenging. There is both the new reader mode and the old intent based method. When doing real processing on the card, timeouts starts being an issue and several of the default values require some tweaking. On top of this, on some devices the NFC implementation require some quirks to work properly. We have gathered up our experience from several years of working with NFC on the Android platform into this library.

## Usage

To include the project into your gradle android build:
```
repositories {
    ...
    maven {
        url 'http://releases.marmeladburk.fidesmo.com'
    }
}
dependencies {
    ...
    compile group: 'com.fidesmo', name: 'nordpol-android', version: '0.1.4', ext: 'aar', transitive: true
}
```
## API

TBD

## Contributions

Additions, bugfixes and issues are very welcome. Added code should be
formatted and structured according to [Googles
styleguide](http://google.github.io/styleguide/javaguide.html). A pull
request should pass the Travis CI before it is merged. If possible
a pull request should be rebased to the most recent master
commit. Don't forget to add yourself to the list of contributors below.

Thanks for your contributions:

- [adriaan-telcred](https://github.com/adriaan-telcred)
