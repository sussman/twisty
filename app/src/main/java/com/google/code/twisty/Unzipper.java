// Copyright 2010 Google Inc.
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Unzips .z* files to /sdcard/Twisty/ and then starts Twisty.
 * 
 * @author clchen@google.com (Charles L. Chen)
 */
public class Unzipper extends Activity {
    private static String TAG = "Twisty Unzipper";

    private Uri dataSource;

    private Unzipper self;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        dataSource = this.getIntent().getData();
        self = this;
        setContentView(new ProgressBar(this));
        (new Thread(new UnzipThread())).start();
    }

    public class UnzipThread implements Runnable {
        public void run() {
            final boolean unzipOk = unzip(dataSource);
            Runnable showMessage = new Runnable() {
                public void run() {
                    if (unzipOk) {
                        Toast.makeText(self, R.string.unzip_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(self, R.string.unzip_fail, Toast.LENGTH_LONG).show();
                    }
                }
            };
            self.runOnUiThread(showMessage);
            Intent intent = new Intent(self, Twisty.class);
            startActivity(intent);
            finish();
        }
    }

    public boolean unzip(Uri zipUri) {
        boolean wasValid = false;

        if(zipUri.getScheme().equals("content")) {
            ZipInputStream zipStream = null;
            try {
                zipStream = new ZipInputStream(getContentResolver().openInputStream(zipUri));
                while (zipStream.available() > 0) {
                    ZipEntry entry = zipStream.getNextEntry();
                    String name = entry.getName();
                    wasValid |= unzip(zipStream, name);
                }
            }
            catch (FileNotFoundException e) {
                Log.i(TAG, "Failed to open zip file: " + zipUri.toString());
            }
            catch (IOException e) {
                Log.i(TAG, "Error reading zip file: " + zipUri.toString());
            }
            finally {
                try {
                    if (zipStream != null)
                        zipStream.close();
                } catch (IOException e) {}

            }
        }
        else {
            ZipFile zip = null;
            try {
                String filename = zipUri.toString().replaceFirst("file://", "");
                zip = new ZipFile(filename);
                Enumeration<? extends ZipEntry> zippedFiles = zip.entries();
                while (zippedFiles.hasMoreElements()) {
                    ZipEntry entry = zippedFiles.nextElement();
                    InputStream is = zip.getInputStream(entry);
                    String name = entry.getName();
                    wasValid |= unzip(is, name);
                }
            } catch (IOException e) {
                Log.i(TAG, "Failed to read zip file: " + zipUri.toString());
            } finally {
                try {
                    if (zip != null)
                        zip.close();
                } catch (IOException e) {}
            }
        }

        return wasValid;
    }

    private boolean unzip(InputStream is, String name) {
        try {
            if(name.matches(Twisty.EXTENSIONS)) {
                File outputFile = new File(Environment.getExternalStorageDirectory()
                        + "/Twisty/" + name);
                String outputPath = outputFile.getCanonicalPath();
                name = outputPath.substring(outputPath.lastIndexOf("/") + 1);
                outputPath = outputPath.substring(0, outputPath.lastIndexOf("/"));
                File outputDir = new File(outputPath);
                outputDir.mkdirs();
                outputFile = new File(outputPath, name);
                outputFile.createNewFile();
                FileOutputStream out = new FileOutputStream(outputFile);

                byte buf[] = new byte[16384];
                do {
                    int numread = is.read(buf);
                    if (numread <= 0) {
                        break;
                    } else {
                        out.write(buf, 0, numread);
                    }
                } while (true);

                out.close();
            }
        }
        catch(IOException e) {
            Log.i(TAG, "Failed to unzip file: " + name);
            return false;
        }

        return true;
    }

}
