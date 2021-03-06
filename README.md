# Voxeet Android SDK

The SDK is a Java library allowing users to:

  - Create demo/normal conferences
  - Join conferences
  - Change sounds angle and direction for each conference user
  - Broadcast messages to other participants
  - Mute users/conferences
  - Enable/disable camera
  - Screen share
  - Record conferences
  - Replay recorded conferences
  - If you use External login like O365, LDAP, or custom login to retrieve contact details, it is now possible to also add your contact ID with the display name and the photo url avatar.
   This allows you to ask guest users to introduce themselves and provide their display name and for your authenticated users in your enterprise or for your clients the ID that can be retrieved from O365 (name, department, etc).

### Installing the Android SDK using Gradle

To install the SDK directly into your Android project using the Grade build system and an IDE like Android Studio, add the following entry: "compile 'com.voxeet.sdk:core:0.8.021'" to your build.gradle file as shown below:

```java
dependencies {
    compile 'com.voxeet.sdk.android:core:0.8.021'
    
    
    //add this one aswell if you want to use the voxeet ui toolkit
    compile 'com.voxeet.sdk.android:toolkit:1.0.023'
}
```
### Recommended settings for API compatibility:

```java
apply plugin: 'com.android.application'

android {
    compileSdkVersion 21+
    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 21+
    }
}
```

### Consumer Key & Secret

Add your consumer key & secret to the xml string file of your application.

```java
 <string name="consumer_key">your consumer key</string>
 <string name="consumer_secret">your consumer password</string>
```

### Permissions

Add the following permissions to your Android Manifest file:

```java
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />    
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" />

  // Used to change audio routes
  <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

In order to target Android API level 21 or later, you will need to ensure that your application requests runtime permissions for microphone and camera access. To do this, perform the following step:

Request microphone and camera permissions from within your activity/fragment :

```java
ActivityCompat.requestPermissions(this,
    new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
}
```

```java
ActivityCompat.requestPermissions(this,
    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
}
```

You can also request both at the same time: 

```java
ActivityCompat.requestPermissions(this,
    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
}
```

See the [Official Android Documentation] for more details.

### FCM

Please notice the following steps are required only if you plan on using fcm.
To enable Voxeet notifications (getting a new call, conference ended and so on...) on your applications:
  1. Send us the application fcm token
  2. Add the google.json file to your project
  2. Add this to your Android Manifest: 

```java
    <service android:name="voxeet.com.sdk.firebase.VoxeetFirebaseMessagingService">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        <service android:name="voxeet.com.sdk.firebase.VoxeetFirebaseInstanceIDService">
            <intent-filter>
                <action android:name="com.google.firebase.INSTANCE_ID_EVENT" />
            </intent-filter>
        </service>
```

### Logger

A logger has been added to the SDK allowing users to track events more easily. 3 different levels for 3 different types of informations:

  1. DEBUG for every event dispatched through the eventbus.
  2. INFO to display methods results when calling a SDK method.
  3. ERROR when an error occurs.
  
Please also note that WebRTC has its own logger for WebRTC related events.

## Available methods

### Initializing  

```java
// To be called from the application class

// if you have external info
UserInfo externalInfo = new UserInfo(externalName, externalName, externalPhotoUrl);
// else
UserInfo externalInfo = new UserInfo();

// Deprecated
VoxeetSdk.sdkInitialize(this, consumerKey, consumerSecret, externalInfo);

// Enable firebase or not depending on your needs
VoxeetSdk.sdkInitialize(this, consumerKey, consumerSecret, externalInfo, enableFirebase);

// Set Default activity to launch when receiving notifications if enabled
VoxeetSdk.setDefaultActivity(YourIncomingCall.class.getCanonicalName());

// Set a timer to stop the conference if none has joined it at the end of the defined timeout
VoxeetSdk.setSdkTimeout(45000);

// requires the voxeet toolkit lib in the gradle app file
VoxeetToolkit.initialize(this);
VoxeetToolkit.enableOverlay(true);
```

### Open Session

```java
// Open Sdk Session. Equivalent to login. Included in the initialize method. Use it after disconnecting or encountering an issue requiring to reidentify. (Opens the websocket if call is successful)
VoxeetSdk.openSdkSession();
```

### Logout

```java
// Basic logout and socket disconnect
VoxeetSdk.sdkLogout();
```

### Enabling / Disabling the Voxeet overlay
Enables a view (VoxeetConferenceView) on top of your current view when joining/creating a conference and will allow you to manage the current conference easily and in a stylish fashion. It regroups many objects from the Voxeet UI toolkit. Can be turned on/off at any point in time.

```java
VoxeetToolkit.enableOverlay(boolean enable);
```

### Creating a demo conference  

```java
VoxeetSdk.createSdkDemo();
```

### Creating a conference  

```java
VoxeetSdk.createSdkConference();
```

### Joining a conference

```java
// Used to join someone's conference otherwise joining is automatic
// Joining a non-existing conference will create it
VoxeetSdk.joinSdkConference(String conferenceId);
```

### Joining a conference as a listener (no camera nor microphone)

```java
VoxeetSdk.listenConference(String conferenceId);
```

### Leaving a conference  

```java
VoxeetSdk.leaveSdkConference();
```

### Toggling own video

```java
// if successful, a ConferenceUserUpdatedEvent will be posted with the mediastream updated
VoxeetSdk.toggleSdkVideo();
```

### Retrieving list of invited (not in the conference yet) users to the conference

```java
VoxeetSdk.getSdkInvitedUsers();
```

### Enabling/disabling conference recording

```java
// if successful, a RecordingStatusUpdateEvent will be posted with the updated recording conference status
VoxeetSdk.toggleSdkRecording();
```

### Replaying a recorded conference

```java
VoxeetSdk.replaySdkConference(String conferenceId);
```

### Checking if a conference is live  

```java
VoxeetSdk.isSdkConferenceLive();
```

### Changing user position  

```java
// Change user position using an angle and a distance
// Values for x, y are between : x = [-1, 1] and y = [0, 1]
VoxeetSdk.changePeerPosition(String userId, double x, double y);
```

### Sending message in a conference

```java
// Send messages such as JSON commands...
VoxeetSdk.sendSdkBroadcast(String message);
```

### Figure out if a conference is live

```java
VoxeetSdk.isSdkConferenceLive();
```

### Getting current conference users

```java
VoxeetSdk.getSdkConferenceUsers();
```

### Getting microphone state

```java
VoxeetSdk.isSdkMuted();
```

### Muting microphone

```java
VoxeetSdk.muteSdkConference(boolean mute);
```

### Muting user

```java
// Muting or unmmuting an user depending on the boolean value
VoxeetSdk.muteSdkUser(String userId, boolean mute);
```

### Checking if an user is muted

```java
VoxeetSdk.isSdkUserMuted(String userId);
```

### Getting available audio routes

```java
// Get available audio routes
VoxeetSdk.getSdkAvailableRoutes();
```

### Getting current audio route

```java
VoxeetSdk.currentSdkRoute();
```

### Setting audio route

```java
VoxeetSdk.setSdkoutputRoute(AudioRoute route);
```

### Setting a timeout

```java
// Set a timer to stop the conference if none has joined it at the end of the defined timeout
VoxeetSdk.setSdkTimeout(45000);
```

### Registering the SDK

Registering is mandatory before starting a conference or else it will crash. Since version 0.8.001, the context and the object subscribing to receive the conference events are now decoupled offering more flexibility.

```java
// Deprecated
VoxeetSdk.register(Context context);

// Susbcribe to the SDK events
VoxeetSdk.register(Context context, Object subscriber);
```

### Unregistering the SDK

It's important to use this method once the conference is ended and that you want to finish your current view/activiy/ fragment.

```java
// Unsusbcribe from the SDK events
// Mandatory
VoxeetSdk.unregister(Context context);
```

### Attaching the media stream

It is advised to use the VideoView object available in the SDK. Check the part about the Voxeet UI Toolkit below.

```java
// Attach the renderer to the media so we can get the rendering working
VoxeetSdk.attachMediaSdkStream(String peerId, MediaStream stream, VideoRenderer.Callbacks render);
```

### Unattaching the media stream

```java
// Unattach the renderers to avoid leaks
VoxeetSdk.unAttachMediaSdkStream(String peerId, MediaStream stream);
```

### OBSOLETE - Registering the media stream listener

```java
// Get notified when streams are added/removed
VoxeetSdk.setMediaSdkStreamListener(Media.MediaStreamListener listener);
```

### OBSOLETE - Setting up the Video capturer

```java
//Init the video capturer in the onCreate of your activity.
VideoCapturer capturer = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice(), null);

// Use to retrieved the front camera stream
VoxeetSdk.setSdkVideoCapturer(VideoCapturer capturer);
```

## SDK Initialization

Initialize the SDK in the onCreate() method of your custom application class:

```java
@Override
public void onCreate() {
    super.onCreate();
    
    // if you have external info
    UserInfo externalInfo = new UserInfo(externalName, externalName, externalPhotoUrl);
    // else
    UserInfo externalInfo = new UserInfo();
    
    VoxeetSdk.sdkInitialize(this, consumerKey, consumerSecret, externalInfo);
    VoxeetSdk.enableOverlay(boolean enabled);
}
```

## Activity Structure

In order to work properly, it is necessary to register and unregister the SDK respectively in the onCreate() and onDestroy() methods of your activity/fragment holding the conference.

```java
@Override

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        
    VoxeetSdk.register(getApplicationContext(), this);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    
    VoxeetSdk.unregister(this);  
}   
```

## Media Stream Listener

As of the latest version of the sdk (0.8.000+), the media stream listener is implemented in the sdk and it is no longer required to implement it in your activity / fragment.

## ConferenceUser Model

ConferenceUser model now has an userInfo object where infos are stored such as the external user id, the url avatar and display name. It has to be passed when initializing the sdk in the application class.

```java
public UserInfo getUserInfo();
```

## Events

The SDK will dispatch events to the suscribed classes such as activities and fragments holding the conferences. To get notified, the only necessary step is to add those methods below:

### Conference creation success

```java
@Subscribe
public void onEvent(final ConferenceCreationSuccess event) {
}
```

### Conference creation error

```java
@Subscribe
public void onEvent(final ConferenceCreatedError event) {
}
```

### Conference joined successfully

```java
@Subscribe
public void onEvent(final ConferenceJoinedSuccessEvent event) {
    // Action to be called when joining successfully the conference
}
```

### Conference joined error

```java
@Subscribe
public void onEvent(final ConferenceJoinedError event) {
    // Action to be called if joining failed
}
```

### Conference left success

```java
@Subscribe
public void onEvent(final ConferenceLeftSuccessEvent event) {
    // Action to be called when leaving successfully the conference
}
```

### Conference left error

```java
@Subscribe
public void onEvent(final ConferenceLeftError event) {
    // Action to be called when leaving the conference failed
}
```

### Conference ended

```java
@Subscribe
public void onEvent(final ConferenceEnded event) {
    // Action to be called when a conference has ended (typically a replay ending)
}
```

### Conference user joined
```java
@Subscribe
public void onEvent(final ConferenceUserJoinedEvent event) { 
    // Action to be called when a new participant joins the conference
}
```

### Conference user updated
```java
@Subscribe
public void onEvent(final ConferenceUserUpdatedEvent event) {
    // Action to be called when a participant has been updated (e.g conference user's camera has been turned on/off)
}
```

### Conference user left
```java
@Subscribe
public void onEvent(final ConferenceUserLeftEvent event) {
    // Action to be called when a participant has left (e.g unattaching mediastream if needed)
}
```

### Screen stream added
```java
@Subscribe
public void onEvent(final ScreenStreamAddedEvent event) {
    // Action to be called when a screen share stream is available
}
```

### Screen stream removed
```java
@Subscribe
public void onEvent(final ScreenStreamRemovedEvent event) {
    // Action to be called when a screen share stream is no longer available
}
```

### Recording conference status updated
```java
@Subscribe
public void onEvent(final RecordingStatusUpdateEvent event) {
    // Action to be called when the recording status has changed (RECORDING or NOT_RECORDING)
}
```

### Message received
```java
@Subscribe
public void onEvent(MessageReceived event) {
    // Action to be called when a message is received
}
```

## MediaStream

Present in the ConferenceUserJoinedEvent and ConferenceUserUpdatedEvent, this object is supposed to be attached/unattached from a renderer to display/hide a conference user video or screen share stream. 
It has a boolean flag called hasVideo set to true if the user associated with it is currently streaming his camera/screen. Set to false, it means the user is not streaming or has stopped streaming his camera/screen.

## Voxeet UI toolkit

A few UI objects are available such as the VideoView made to ease up the use of mediastreams and many others. All of them have customizable attributes you can use programmaticaly or in the xml. Here is a comprehensive list:

### VoxeetConferenceView

This view regroups all of the others custom components available. We will now go into further details about each one of them.

### VoxeetTimer

It displays a timer for the conference which starts when you join the conference. Also shows a green light which turns to red when the conference is being recorded.

```java

  // indicator color when conference is being recorded
  <attr name="recording_color" format="color" />

  // indicator color when conference is live
  <attr name="default_color" format="color" />

  // indicator color when you not in a conference
  <attr name="not_in_conference_color" format="color" />

  // text color of the timer
  <attr name="text_color" format="color" />
  
  // defines when the timer starts (when joining a conference or when the view is created...)
  <attr name="timer_mode" format="integer" />
  
  // display indicator or not
  <attr name="color_enabled" format="boolean" />
```

### VideoView

This is the component used to display someone's stream. Two main methods are available to attach and unattach the different streams: 

```java
// Sets the videoview's behavior when already attached. Should it auto attach or just stay attached to the previous stream.
 public void setAutoUnAttach(boolean autoUnAttach) {
        this.autoUnAttach = autoUnAttach;
}
```

```java
// Determines if the videovideo is already attached to a stream
 public boolean isAttached() 
```

```java
// Sets the videoview to mirrored or not depending on the boolean value
public void getRenderer().setMirror(shouldBeMirrored);

<attr name="mirrored" format="boolean" />
```

```java
// Attach the renderer to the media so we can get the rendering working
public void attach(String peerId, MediaStream stream);
```

```java
public void unAttach();
```

### VoxeetIncomingCallButton

Press and swipe out type of button used to answer/decline a call for example. Callback listener will be triggered if set when the swipe has reached the minimum distance.

```java
public void setIncomingCallListener(IncomingCallListener listener)
```

```java
<attr name="view_src" format="integer" />
```

### VoxeetCurrentSpeakerView

Displays the current speaker with it's avatar picture and a scaling circle representating it's current voice level.

```java
<attr name="vu_meter_color" format="color" />
```

### VoxeetVuMeter

It's a part of the VoxeetCurrentSpeakerView mentionned above but can definitely work on it's own.

```java
<attr name="background_color" format="color" />
```

## VoxeetConferenceBarView

Contains different buttons allowing you to manage the conference. Here are the different available buttons: 

  - toggle own camera
  - leave conference
  - mute 
  - change audio output
  - toggle conference recording
  

All of them are supposed to have a default state as well as a selected state. Since (1.0.012) you can set your own drawables to the conference bar's buttons through the xml or by using one the following methods: 

```java
public void setCameraSelector(Drawable drawable);
public void setCameraSelector(int cameraSelector);

public void setRecordSelector(Drawable drawable);
public void setRecordSelector(int recordSelector);

public void setHangUpSelector(Drawable drawable);
public void setHangUpSelector(int hangUpSelector);

public void setAudioSelector(Drawable drawable);
public void setAudioSelector(int audioSelector);

public void setMuteSelector(Drawable drawable);
public void setMuteSelector(int muteSelector);
```


```java
// set your own selector to each button
<attr name="mute_selector" format="integer" />
<attr name="audio_selector" format="integer" />
<attr name="hang_up_selector" format="integer" />
<attr name="camera_selector" format="integer" />
<attr name="record_selector" format="integer" />
```

To hide/make visible buttons, here are the different methods: 

```java
public void setDisplayRecord(boolean displayRecord);

public void setDisplayAudio(boolean displayAudio);

public void setDisplayMute(boolean displayMute);

public void setDisplayCamera(boolean displayCamera);
    
public void setDisplayLeave(boolean displayLeave);
```

```java
<attr name="record_button" format="boolean" />
<attr name="audio_button" format="boolean" />
<attr name="mute_button" format="boolean" />
<attr name="video_button" format="boolean" />
<attr name="leave_button" format="boolean" />
```

You can also use the builder provided, set the buttons order as you wish and finally add the conference bar programmatically: 

```java
FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT);
         params.gravity = Gravity.BOTTOM | Gravity.CENTER;

VoxeetConferenceBarView conferenceBarView = new VoxeetConferenceBarView.Builder()
        .with(this)
        .setLayoutParams(params)
        .hangUp(R.drawable.selector_hangup, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "hangup");
            }
        }).mute(R.drawable.selector_mute, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "mute");
            }
        }).build();

addContentView(conferenceBarView, conferenceBarView.getLayoutParams());
```

## VoxeetParticipantView

The participant view shows the current conference's participants. Also displays the users' cams when they are enabled. You can set a listener so you get notified when a participant is selected/unselected. This allows interactions with others views. You can check the VoxeetConferenceView which locks the currentSpeakerView when someone is selected.

```java
public void setParticipantListener(ParticipantViewListener listener)
```

```java
<attr name="overlay_color" format="color" />
<attr name="name_enabled" format="boolean" />
<attr name="display_self" format="boolean" />
```

## VoxeetLoadingView

A custom view designed to display an ongoing task like an outgoing/incoming call.

```java
<attr name="loading_color" format="color" />
```

## Conference event flow

1. ConferenceCreatedEvent (if you're the one creating the conference, joining it is automatic)

2. ConferenceJoinedSuccessEvent or ConferenceJoinedErrorEvent after joining it 
  
3.  a. ConferenceUserJoinedEvent when someone joins the conf

    b. ConferenceUserUpdatedEvent when someone starts/stop streaming
    
    c. ConferenceUserLeftEvent when someone left
    
4. ConferenceLeftSuccessEvent or ConferenceLeftErrorEvent after leaving the conference

5. ConferenceEndedEvent if a conference has ended such a replay ending

6. ConferenceDestroyedEvent when the conference is destroyed

## Best practice regarding conferences

Only one instance of a conference is allowed to be live. Leaving the current conference before creating or joining another one is mandatory. Otherwise, a IllegalStateException will be thrown.

## Version
core: 0.8.021

toolkit: 1.0.023

## Tech

The Voxeet Android SDK uses a number of open source projects to work properly:

* [Retrofit2] - A type-safe HTTP client for Android and Java.
* [GreenRobot/EventBus] - Android optimized event bus.
* [Jackson] - Jackson is a suite of data-processing tools for Java.
* [Butterknife] - Bind Android views and callbacks to fields and methods.
* [Picasso] - A powerful image downloading and caching library for Android.
* [Recyclerview] - An android support library.
* [Apache Commons] - Collection of open source reusable Java components from the Apache/Jakarta community.
* [RxAndroid] - RxJava is a Java VM implementation of Reactive Extensions: a library for composing asynchronous and event-based programs by using observable sequences.

## Sample Application

A sample application is available on this [public repository][sample] on GitHub.

© Voxeet, 2017

   [Official Android Documentation]: <http://developer.android.com/training/permissions/requesting.html>
   [sample]: <https://github.com/voxeet/android-sdk-sample.git>
   [GreenRobot/EventBus]: <https://github.com/greenrobot/EventBus>
   [Jackson]: <https://github.com/FasterXML/jackson>
   [Picasso]: <http://square.github.io/picasso>
   [Recyclerview]: <https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html>
   [Butterknife]: <http://jakewharton.github.io/butterknife>
   [Apache Commons]: <https://commons.apache.org>
   [RxAndroid]: <https://github.com/ReactiveX/RxAndroid>
   [Retrofit2]: <http://square.github.io/retrofit/>
