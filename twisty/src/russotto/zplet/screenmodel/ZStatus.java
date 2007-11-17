/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import com.google.twisty.zplet.Label;

public class ZStatus {
		 boolean timegame;
		 boolean initialized;
		 boolean chronograph;
		 String location;
		 int score;
		 int turns;
		 int hours;
		 int minutes;
		 Label Right;
		 Label Left;
		
		 public ZStatus(Label left, Label right) {
				 Right = right;
				 Left = left;
				 chronograph = false;
		 }

/*
		 public boolean gotFocus(Event evt, Object what)
		 {
				 System.err.println("ZStatus got focus");
				 return false;
		 }
		
		 public boolean lostFocus(Event evt, Object what)
		 {
				 System.err.println("ZStatus lost focus");
				 return false;
		 }
*/

		 public void update_score_line(String location, int score, int turns) {
				 this.timegame = false;
				 this.location = location;
				 this.score = score;
				 this.turns = turns;
				 Left.setText(location);
				 Right.setText(score + "/" + turns);
				 layout();
				 repaint();
		 }
		
		 public void update_time_line(String location, int hours, int minutes) {
				 String meridiem;

				 this.timegame = true;
				 this.location = location;
				 this.hours = hours;
				 this.minutes = minutes;
				 Left.setText(location);
				 if (chronograph) {
						 Right.setText(hours + ":" + minutes);
				 }
				 else {
						 if (hours < 12)
								 meridiem = "AM";
						 else
								 meridiem = "PM";
						 hours %= 12;
						 if (hours == 0)
								 hours = 12;
						 Right.setText(hours + ":" + minutes + meridiem);
				 }
				 layout();
				 repaint();
		 }
	/*	
		 public Dimension minimumSize() {
				 return new Dimension(100,10);
		 }
		
		 public Dimension preferredSize() {
				 return new Dimension(500,20);
		 }
		 */
		 
		 private void layout() {
		 }
		 
		 private void repaint() {
		 }
 }


