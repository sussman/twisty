Twisty, a text-adventure interpreter for Android.
-------------------------------------------------

Twisty is a game interpreter for the Android operating system.  It
allaws you to play both classic and modern "text adventures" on your
mobile device.

You can find numerous games to download at http://ifarchive.org.  We
also recommend visiting http://www.ifwiki.org for general information
on Interactive Fiction.

The z-machine is a virtual machine designed in 1979 by Infocom for
playing text adventures, and it has been re-implemented on nearly every
computer and PDA since then.  This application allows Android users to
play Infocom classics just as 'Zork', as well hundreds of newer text
adventures written in the last ten years or so.

The glulx-machine is an modernized version of the z-machine capable of
abstracted I/O, 32-bit operations, and other nifty things possible
without ancient memory constraints.  Most modern text adventures
written in Inform (www.inform7.com) create games of this type.

Twisty is theoretically capable of identifying and playing both types
of games.


History
-------

The codebase originally began as an Android Activity constructed
around a heavily-hacked port of 'zplet', an early lightweight
z-machine interpreter written for Java 1.0.

 *** This project is now undergoing a huge rewrite! ***

We are now attempting to use Android's Native Development Kit (NDK) to
make JNI calls to various VM-interpreters running as C libraries.
This should give us a ~10x speed improvement, and also allow us to
support a wider range of z-machines (via 'nitfol'), glulx games (via
'git') and perhaps someday even TADS or other interpreters.

Specifically, we're using the GlkJNI layer to provide an abstract UI to 
the various C interpreters.  GLK is a UI "abstraction layer" created by
Andrew Plotkin which can be implemented in any GUI toolkit, and GlkJNI
ports this abstraction to JNI via a set of java interfaces.  It then
provides an Android-specific implementation of those interfaces.  The
best C interpreters out there expect to use GLK for I/O, so this
allows us our pick of C interpreters.


Redistribution and Licensing
-----------------------------

Twisty is open source software, and is distributed as a whole under
the GPL v3 license.  It depends heavily on the GlkJNI project, which
is also under GPL v3; on 'nitfol', a z-machine interpreter, under the
GPL v2 (which we're releasing under a "later version", GPL v3); and
also on 'git', a Glulx interpreter, under an MIT license.  The
collective Twisty product is thus safely all GPLv3-able.

Three free, classic games are packaged with the application;  for
details on their licensing, see the LICENSE file.  (This doesn't
violate the GPL as far as we know, as these games aren't linked into
the code, nor are they necessary for the code to build or run.  They
are merely interpreted and provided for convenience to users.)

Patches are welcome!  Please mail us at twisty-dev@googlegroups.com.


Installing the App
---------------------

IMPORTANT:

The version of Twisty in Google Play Store is from 2009 -- it's built
from a totally different codebase that is 100% Java and only
understands 'z machine' games.  It's /not/ able to play most modern
Inform-based glulx games (e.g. ones that end with .gblorb).

The rewritten version of Twisty isn't yet suitable for release.  While
it also knows how to play glulx games, it's still buggy and missing
many features.  That said, if you're a techie who knows how to install
apps directly (e.g. loading an .apk package via the 'adb install'
command), then you can find the experimental app in the downloads area
of this Bitbucket project:

  https://bitbucket.org/sussman/twisty/downloads



Building from Source
--------------------

For developers contributing:  see the BUILD.md file.

