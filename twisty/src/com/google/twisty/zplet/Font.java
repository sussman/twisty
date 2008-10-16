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

package com.google.twisty.zplet;

import android.graphics.Typeface;

public class Font {

	public static final int PLAIN =  Typeface.NORMAL;
	public static final int BOLD =   Typeface.BOLD;
	public static final int ITALIC = Typeface.ITALIC;

	public Font(String name, int style, int size) {
		this.name = name;
		this.style = style;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public int getStyle() {
		return style;
	}

	public String getName() {
		return name;
	}
	
	private final String name;
	private final int style;
	private final int size;

  @Override
  public boolean equals(Object o) {
    if (o instanceof Font) {
      Font f = (Font) o;
      return (name.equals(f.name) && style == f.style && size == f.size);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Font:{name=" + name + ";style=" + style + ";size=" + size + "}";
  }
	
}
