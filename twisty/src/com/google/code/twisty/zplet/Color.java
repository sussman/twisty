//   Copyright 2007 Google Inc.
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

package com.google.code.twisty.zplet;

public class Color {
  Color(int argb) {
    this.argb = argb;
  }

  public int getARGB() {
    return argb;
  }

  public static final Color black   = new Color(android.graphics.Color.BLACK);
  public static final Color red     = new Color(android.graphics.Color.RED);
  public static final Color green   = new Color(android.graphics.Color.GREEN);
  public static final Color blue    = new Color(android.graphics.Color.BLUE);
  public static final Color cyan    = new Color(android.graphics.Color.CYAN);
  public static final Color magenta = new Color(android.graphics.Color.MAGENTA);
  public static final Color yellow  = new Color(android.graphics.Color.YELLOW);
  public static final Color white   = new Color(android.graphics.Color.WHITE);
  public static final Color gray    = new Color(android.graphics.Color.GRAY);

  private final int argb;
  
  public String toString() {
    return "{argb=" + argb + "}";
  }
}
