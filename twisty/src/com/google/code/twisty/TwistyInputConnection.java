// Copyright 2009 Google Inc. All Rights Reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.google.code.twisty;

import android.view.View;
import android.util.Log;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

// This whole class is a hack to work around a bug in Cupcake, whereby
// the software-keyboard doesn't send an onKey event for the "Enter" key.

public class TwistyInputConnection extends BaseInputConnection implements InputConnection {
  
  private static String TAG = "TwistyInputConnection";
  
  public TwistyInputConnection(View targetView, boolean fullEditor) {
    super(targetView, fullEditor);
  }
  
  @Override
  public boolean performEditorAction(int editorAction) {
    // Even on a software-keyboard, this method will be called for "Enter".
    Log.i(TAG, "I hear a keyboard clicking somewhere...");
    return true;  // success
  }
  
}
