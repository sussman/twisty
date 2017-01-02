
** For Developers Only. Warning: this is a little complex!


A.  Prerequisites

 1. Android SDK and Eclipse setup. The easiest way to do this is with
    the ADT bundle at http://developer.android.com/sdk. (You can add the
    Android Developer tools to an existing Eclipse install as well, if
    you are so determined. See the instructions on that page about
    installing the ADT plugin from within Eclipse).

 2. The Android Native Development Kit (NDK) -- at least version r9c --
    from http://developer.android.com/tools/sdk/ndk. Download and
    unpack the NDK somewhere (call it $NDK). This is the GCC toolchain
    to cross-compile for Android ARM/x86/MIPS chips (we're using ARM).

 3. NDK plugin for Eclipse. This will allow you to build the native
    code right from Eclipse rather than as a separate command line
    step, and also enables useful functionality like Eclipse debugger
    integration. Launch Eclipse > Help > Install New Software... and
    set the source to Work With "Android Developer Tools Update Site".
    Check the box next to NDK Plugins and then click the "Next >"
    button at the bottom, agree to the license, and install.
    Note that it might appear to hang for ~5 minutes while
    "Calculating requirements and dependencies". After it finishes,
    you'll need to restart the ADT.
    (More about configuring the NDK to build your Twisty project below
    under Building the code).

 4. A copy of the Twisty codebase -- which you must already have
    if you're reading this.  (Say, at location $TWISTY.)  To work on
    the 2.0 rewrite be sure you're looking at the DEFAULT mercurial
    branch: run 'hg up -c DEFAULT' if unsure.


B.  Code Overview

The Twisty codebase contains the main Twisty "activity", a few sample
games, and some C interpreters that expect to use GLK for I/O.  The
Twisty activity is essentially a high-level consumer of GlkJNI.

The GlkJNI codebase contains 'glkjni' (the C code implementing
a trampoline to Java), as well as specific java
implementations of these interfaces. The one we care about is
'roboglk', which maps the interfaces to Android's UI.


C.  Building the code

Fire up Eclipse, and make sure you have the latest ADT installed at
pointed at your Android SDK directory.

 1. Start a new "generic project" and choose "Android project".  In
    the following dialog box, choose "based on existing source" and
    then select $TWISTY/twisty/.

 2. Now open the project's properties (Project->Properties) and choose
    "Java Build Path" from the list on the left.  Then choose the
    'Source' tab and press the 'Link Source' button.  Navigate to the
    'roboglk' directory at the top of twisty tree and and select that.
    It should now appear in the list with $TWISTY/src and
    $TWISTY/gen. Now select the 'Order and Export' tab and arrange the
    build class order from top to bottom as Twisty/roboglk,
    Twisty/src, Twisty/gen, and Android 4.4.  The box next to Android
    4.4 should be checked.

 3. In the same Project->Properties panel, choose 'Java Compiler' and
    make sure it's set to Java compliance level 1.6.
 
 4. To make the Native (C) code happy, you'll need to:
    A. Eclipse > Window > Preferences > Android > NDK >
       set path to the NDK and fill in your $NDK path
    B. right-click on the toplevel Twisty project and choose
       Android Tools > Add Native Support
    C. If you plan on debugging Native (C) code, add NDK_DEBUG=1
       to the native build command in
       Project->Properties->C/C++ Build.
       It should say "ndk-build NDK_DEBUG=1"
       (If you change this, do a Project>Clean...>Clean all projects
       and rebuild).

D.  Running Twisty in the Android Virtual Device (AVD)

 1. Twisty should now build correctly. To run it in an AVD, you must first
    configure an AVD. This can be done through the menu Window->Android SDK
    and AVD manager. Select 'Virtual devices' on the left, then press the
    'New' button to create a new entry. Add a name and specify the target as 
    'Android 2.1 - API level 7'. For SD card, you can make a new, blank card
    by just indicating the size, e.g., 64M. Finally, choose a skin. You may
    make more than one AVD. 

 2. To specify the AVD to run, select Run->Run Configuration. Select the 
    'target' tab, and then check the box next to your choice of AVD.

 3. Select 'Run'. The emulator should launch, report mounting of the SDcard,
    and then begin the Twisty activity.


E. Debugging Twisty Native code

With the NDK plugin configured, you can debug C code within eclipse,
on both the emulator (AVD) and a real device!

 1. Run > Debug Configurations... and choose Android Native App. Create a
    new one and name it Twisty Native or something.
 
 2. Make sure you set NDK_DEBUG=1 in the C/C++ build option (see the Building
    the code section above). (TODO: There might be a slicker way to do this
    with Eclipse variables).
    
 3. Run your debug configuration; it's just Eclipse CDT so you can set
    breakpoints, inspect the stack and variables, etc., like any other
    C/C++ application.
    