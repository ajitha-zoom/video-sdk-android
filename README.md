> :warning: The following sample application is a personal, open-source project shared by the app creator and not an officially supported Zoom Video Communications, Inc. sample application. Zoom Video Communications, Inc., its employees and affiliates are not responsible for the use and maintenance of this application. Please use this sample application for inspiration, exploration and experimentation at your own risk and enjoyment. You may reach out to the app creator and broader Zoom Developer community on https://devforum.zoom.us/ for technical discussion and assistance, but understand there is no service level agreement support for this application. Thank you and happy coding!

# Getting Started Guide for Video SDK(Android)

The Zoom Video SDK for Android allows you to build custom video meeting applications with access to raw video and audio data, enabling highly-interactive, custom user experiences. This repository contains a sample app written in "Java" to accompany the "Integrate" session in our documentation.

## Pre-requisites
To run the sample app, you must have:
- Zoom Video SDK package for Android
- Android Studio
- SDK Key and secret values
- A physical Android device with Android API Level 21+
- Experience building Android Apps

Note: This sample application is built on top of v1.8.5 in Java. "Kotlin" variant of this repository shall be provided shortly.

## Download and run
- Clone the git repository
- Open the project in Android Studio
- From the extracted SDK, copy mobilertc/mobilertc.aar and paste it inside mobilertc/ in the downloaded repository
- Clone https://github.com/ajitha-zoom/videosdk-demo-endpoint and run the web app by following the instructions in README.md file 
- WebApp runs in https://localhost:4000 by default
- Use ngrok to create url
- Copy the url and paste in JWTGenerator for myURL field value

```sh
private String myURL = "https://ngrok.app";
```

- You can choose to retain the sample session name and password or change it as per your convenience.
```sh
private String SAMPLE_SESSION_NAME = "newsession";
private String SAMPLE_SESSION_PWD = "zoom";
private String SAMPLE_USER_IDENTITY = "zoom dev";
```

- Sync gradle files
- Connect your device, ensure it's selected, and click the Run app button in the top toolbar.
- Click on "Join Session" button, enter session name as given in SAMPLE_SESSION_NAME and password as given in SAMPLE_SESSION_PWD and Join

## Features
This repository demonstrates the basic capabilities of the Video SDK.
- Creating/joining a session
- Mute/unmute audio
- Start/stop video and share screen.

## Screenshots

![1](https://github.com/ajitha-zoom/video-sdk-android/assets/123167421/ff6f2ab5-8ab5-4a08-924b-294a01b7231a) ![2](https://github.com/ajitha-zoom/video-sdk-android/assets/123167421/db9634c9-e5ba-4d27-beec-c60aa7727db5) ![3](https://github.com/ajitha-zoom/video-sdk-android/assets/123167421/a64fbbe5-cd9d-4465-9eca-d1d1db285978)

## Need Help?
If you're looking for help, try Developer Support or our Developer Forum. Priority support is also available with Premier Developer Support plans.
