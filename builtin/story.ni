"Twisty" by Google

Chapter 1 - Initial Scene

The Lobby is a room. "You are standing in the lobby of the prestigious Hotel El Goog. There is an open-looking area to the south, and you can just hear the distant tinkle of a piano to the north."

Chapter 2 - Deaths

The Atrium is north of the Lobby. "You are in a tall sunlit Atrium. When you entered, the music from an unseen piano stopped abruptly. The Hotel Lobby is visible to the south."
Instead of waiting in the Atrium for the second time: say "You are unexpectedly crushed by a falling piano."; end the game in death.

The Driveway is south of the Lobby. "You are standing in the middle of a driveway. It is unusually quiet and empty for the driveway of such a prestigious hotel. The Hotel El Goog lies to the north. You feel a bit unsafe standing in the middle of the road."
Instead of waiting in the Driveway: say "As if out of nowhere, a huge truck appears and smashes into you. What a waste."; end the game in death.

A person can be happy or grieving. The player is happy.

Instead of dropping the phone:
	remove the phone from play;
	say "The phone smashes into tiny pieces!";
	cleanup occurs in one turn from now;
	heartbreak occurs in five turns from now;
	now the player is grieving.

[TODO: make this apply only to the pieces of phone]
Counting is an action applying to one thing.
Understand "count" as counting.
Carry out counting:
	say "You swiftly determine that there are 69,105 pieces."

At the time when cleanup occurs:
	say "A Janitorial Googlebot appears out of thin air, cleans up the mess, and disappears back in to thin air."
At the time when heartbreak occurs:
	if the player is grieving, end the game saying "Your guilt and grief at the loss of your phone has taken you to an early grave."
Every turn when the player is grieving:
	choose a random row in the Table of Grief Messages;
	say "[message entry][paragraph break]".
	
Table of Grief Messages
message
"You feel sad."
"You reach for your phone, but it's not there."
"You mourn the loss of the phone."
"You wonder how to replace your phone."
"You are having trouble thinking about anything other than your destroyed phone."
"You hear the distant ring of someone else's phone, which triggers a pang of jealousy."

Chapter 3 - Mechanics of the Phone

The phone is a switched on device in the Lobby. "You see here a modern-looking phone.".
The description of the phone is "It's a modern-looking phone that includes [a list of components which are part of the phone]."

A component is a kind of thing.
A battery, a SIM card, a keypad and a screen are components.
The compartment is part of the phone.
A battery and a SIM card are in the compartment.
The keypad is part of the phone. The screen is part of the phone.
The description of the battery is "[if the phone is switched on]The battery is discharging merrily.[otherwise]It's a perfectly ordinary looking [noun].".
The description of the SIM card is "[if the SIM card is in the compartment]The SIM card is inside the phone, so you can't see it.[otherwise]It's a perfectly ordinary looking [noun].".
The description of the screen is "[if the phone is switched on]You notice that the phone has some games installed on it: you can play [a list of games which are part of the phone].[otherwise]The [noun] is very clean and shiny. It's also blank.".
Check switching on the phone: if the battery is not in the compartment, say "It needs a battery." instead; if the SIM card is not in the compartment, say "The screen flashes a message about needing a SIM card, and then goes blank." instead.
Instead of taking the SIM card when the battery is in the compartment: say "The battery is in the way."
After taking the battery when the phone is switched on: say "The phone's screen goes blank."; now the phone is switched off.

Check inserting SIM card into compartment: if the battery is in the compartment, say "The battery is in the way." instead.
Instead of inserting battery into phone: now the battery is in the compartment; say "Done.".
Instead of inserting sim card into phone:
	if the battery is in a compartment begin;
		say "The battery is in the way.";
	otherwise;
		now the sim card is in the compartment;
		say "Done.";
	end if.
test phone with "take battery / take sim card / put battery in phone / put sim card in phone / take battery"

Chapter 3a - Making Calls, since it is a phone after all

Calling is an action applying to nothing.
[TODO: allow anything at all to be the object of "call", so you can "call bob"]
[TODO: as an awesome bonus, add address book integration instead of the following]
Understand "call" as calling.
Understand "dial" as calling.
Understand "phone" as calling.
Understand "ring" as calling.
Check calling: if the player does not have the phone, say "You need a phone in order to do that." instead; if the phone is switched off, say "The phone is switched off." instead.
Carry out calling:
	choose a random row in the Table of Caller Messages;
	say "[message entry][paragraph break]".
	
Table of Caller Messages
message
"You have second thoughts."
"There's no answer."
"The number is busy."
"It goes to voicemail, but you don't leave a message."
"You talk for a while."

Chapter 4 - Playing Games

A game is a kind of thing.
Playing is an action applying to one thing.
Understand "play" as playing.
Understand "play [thing]" as playing.
[TODO: these should be in a table]
A game called Adventure is part of the phone.
A game called Zork 1 is part of the phone.
A game called Zork 2 is part of the phone.
A game called Zork 3 is part of the phone.
Understand "zork" or "zork1" or "zork i" as Zork 1.
Understand "zork2" or "zork ii" as Zork 2.
Understand "zork3" or "zork iii" as Zork 3.

[TODO: provide a way to play games off the filesystem]
Check playing: if the player does not have the phone, say "You don't have anything that would let you do that." instead; if the phone is switched off, say "The phone is switched off." instead.
Carry out playing a game:
	say "The world around you grows dim...";
	say "+*+*+*BUILTIN [noun]";
	[The interpreter should catch this, not display it, and switch to the other game;
	return to this point when the other game exits]

test games with "take phone / play zork / play zork 2 / play zork iii"

Chapter 5 - A nod to Homestar

The flask is a backdrop. The flask is everywhere.
Understand "ye flask" as the flask.
instead of taking the flask: say "Ye cannot get the FLASK. It is firmly bolted to a wall which is bolted to the rest of the dungeon which is probably bolted to a castle. Never you mind."
[TODO: since "Thy Dungeonman" is a small 3-room game, it would be really easy to build in the whole thing]
