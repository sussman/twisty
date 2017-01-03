Prerequisites
-------------

Android Studio, which can be found at
https://developer.android.com/studio/index.html . The default install options
are fine. Once installed and running, you also need the NDK. It can be added
by going to File -> Settings and then Appearance & Behaviour ->
System Settings -> Android SDK. Go to the SDK Tools tab and check the NDK box
in the list. Click OK.


Code Overview
-------------

The Twisty codebase contains the main Twisty "activity", a few sample
games, and some C interpreters that expect to use GLK for I/O.  The
Twisty activity is essentially a high-level consumer of GlkJNI.

The GlkJNI codebase contains 'glkjni' (the C code implementing
a trampoline to Java), as well as specific java
implementations of these interfaces. The one we care about is
'roboglk', which maps the interfaces to Android's UI.


Building the code
-----------------

1. In Android Studio you can either import the project if you've already
   cloned the git repository to your local machine or let it do that for
   you. Assuming you have no other projects open, you can use the "Import
   project" or "Check out project from Version Control" options from the
   welcome screen to do this.

2. Say yes to and install everything Android Studio asks you to when it opens
   the project. First it'll install the Gradle wrapper. It might say there's
   an "Unregistered VCS Root detected", in which case you should click
   "Add Root". There will be a progress bar at the bottom of the screen while
   Gradle is syncing. It will pop up a message at the bottom with clickable
   links to install any missing platform or build tools. It will also complain
   if the NDK is missing if you forgot that as part of the prerequisites.
   After installing the NDK you'll need to manually start a Gradle sync for it
   to continue. If all goes well and Gradle finishes, you should now have a
   compiled Twisty.

3. To run the app, click the play button and create an AVD if you don't already
   have one. The default options are fine to start with.
