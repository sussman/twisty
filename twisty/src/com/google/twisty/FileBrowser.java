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

package com.google.twisty;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.database.ArrayCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class FileBrowser extends ListActivity
{
	static final String TAG = "FileBrowser";
	private Pattern fileMatcher;
	private Pattern pathMatcher;

	/**
	 * Called with the activity is first created.
	 * The caller can use Intent.putExtra to pass parameters that control
	 * the behavior of the file browser:
	 * <ul>
	 *   <li>file-filter can contain a regular expression string that must
	 *   match a file's name in order for it to be shown</li>
	 *   <li>Use path-filter to pass a regular expression string that must
	 *   match the full path of a directory in order for it to be shown</li>
	 *   <li>Use start-dir to specify the initial working directory</li>
	 * </ul> 
	 */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		// Pass in a file-filter RE to determine what files are visible
		fileMatcher = null;
		Object o = getIntent().getExtra("file-filter");
		if (o != null && o instanceof String) {
			fileMatcher = Pattern.compile((String) o);
		}
		// Pass in a path-filter RE to determine what directories are visible
		pathMatcher = null;
		o = getIntent().getExtra("path-filter");
		if (o != null && o instanceof String) {
			pathMatcher = Pattern.compile((String) o);
		}
		// Pass in a file-filter RE to determine where browsing starts
		o = getIntent().getExtra("start-dir");
		if (o != null && o instanceof String) {
			mWorkingDir = (String) o;
		} else {
			mWorkingDir = "/";        
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
	}

	void refresh() {
		if (mCursor == null) {
			// First time
			mCursor = new FileListCursor(mWorkingDir);
			// Map Cursor columns to views defined in simple_list_item_2.xml
			ListAdapter adapter = new SimpleCursorAdapter(this,
					android.R.layout.simple_list_item_1, mCursor,
					new String[] { "name" },
					new int[] { android.R.id.text1 });
			setListAdapter(adapter);
		} else {
			// We are revisiting this Screen
			mCursor.requery();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		FileListCursor cur = (FileListCursor) l.obtainItem(position);
		File f = cur.getCurrentFile();
		if (f.isDirectory()) {
			Log.e(TAG, "Selected dir: " + f.getPath());
			mWorkingDir = f.getPath();
			mCursor.close();
			mCursor = null;
			refresh();
		} else if (f.isFile()) {
			Log.e(TAG, "Selected file: " + f.getPath());
			setResult(RESULT_OK, f.getPath());
			finish();
		} else {
			Log.e(TAG, "Selected: not a file - cancelling");
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	private FileListCursor mCursor;
	private String mWorkingDir;

	/** A Cursor that allows navigation of files in a directory */
	class FileListCursor extends ArrayCursor
	{
		public FileListCursor(String wd)
		{
			super(new String[] { "name" });
			File d = new File(mWorkingDir);
			String[] files = d.list();
			ArrayList<FileInfo> al = new ArrayList<FileInfo>();
			if (files == null) {
				al.add(new FileInfo("[ no files found ]", ""));
			} else {
				Arrays.sort(files);
				File parent = d.getParentFile();
				if (parent != null) {
					String path = parent.getPath();
					Log.i(TAG, "Parent: " + d.getPath() + " -> " + path);
					if ((null == pathMatcher) ||
							pathMatcher.matcher(path).matches())
						al.add(new FileInfo("[DIR] ..", path));
				}
				for (String fn : files) {
					if (fn.startsWith("."))
						continue;
					String ffn = mWorkingDir + "/" + fn;
					FileInfo f = new FileInfo(fn, ffn);
					if (f.isFile()) {
						if ((fileMatcher == null) ||
								fileMatcher.matcher(ffn).matches())
							al.add(f);
					}
					if (f.isDirectory()) {
						f.setDisplayName("[DIR] " + fn);
						if ((null == pathMatcher) ||
								pathMatcher.matcher(f.getPath()).matches())
							al.add(f);
					}
				}
			}
			mFiles = al.toArray(new FileInfo[] {});
			mLen = mFiles.length;
		}

		public String getString(int column)
		{
			return mFiles[mPos].getDisplayName();
		}

		public File getCurrentFile()
		{
			return mFiles[mPos];
		}

		FileInfo[] mFiles;
	}

	class FileInfo extends File {
		private static final long serialVersionUID = -173452696432207985L;

		public FileInfo(String display, String fullname) {
			super(fullname);
			if (display != null)
				displayName = display;
			else
                displayName = super.getName();
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String s) {
            displayName = s;
        }

        private String displayName;
    }
}
