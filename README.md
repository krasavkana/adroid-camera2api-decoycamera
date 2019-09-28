# android-camera2api-decoycamera

A code example to show how to use Camera2 APIs, how to camouflage device, and how to connect to BLE switches

# Detail Info.

## Precondition

1. When you want to use this app as remote-camera, some BLE switch is needed.

https://github.com/krasavkana/android-ble-switch

2. NOT as remote-camera, especially nothing.

## What the app can do

### Preferences

None

### Function

the app works as a decoy camera(camera2 API):

- minimum finder as 60(dp)x40(dp)
- minimum shooting button
- nobody realize that camera function is working since minimum finder(camouflage)
- background image will be changed every 20s(camouflage)

## Cautions

When shooting, you will hear a sound(depending on the device).
Using camera2 API, no shooting sound will be played, but some policy(japan only)
shooting sound will be added automatically

Note that code up here includes nothing about mediasound.

## More info about the app

Visit below:
how to use and so on on [MY BLOG PAGE](https://krasavkana.com)

Enjoy!
