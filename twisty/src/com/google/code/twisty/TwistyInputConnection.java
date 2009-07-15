// Copyright 2009 Google Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.


package com.google.code.twisty;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

// This whole class is a hack to work around a bug in Cupcake, whereby
// the software-keyboard doesn't send an onKey event for the "Enter" key.

public class TwistyInputConnection extends BaseInputConnection implements InputConnection {
  
  public TwistyInputConnection(View targetView, boolean fullEditor) {
    super(targetView, fullEditor);
  }

  @Override
  public boolean sendKeyEvent(KeyEvent event) {
     return super.sendKeyEvent(event);
  }

  @Override
  public boolean performEditorAction(int editorAction) {
    sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    return super.performEditorAction(editorAction);
  }
  
}
