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

public class Font implements java.io.Serializable {

	public static final int PLAIN =  0x01;
	public static final int BOLD =   0x10;
	public static final int ITALIC = 0x20;

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
}
