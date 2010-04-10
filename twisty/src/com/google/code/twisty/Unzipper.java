/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.code.twisty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Unzips .z* files to /sdcard/Twisty/ and then starts Twisty.
 * 
 * @author clchen@google.com (Charles L. Chen)
 */
public class Unzipper extends Activity {
    private String dataSource;

    private Unzipper self;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        dataSource = this.getIntent().getData().toString().replaceFirst("file://", "");
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
                        Toast.makeText(self, R.string.unzip_success, 1).show();
                    } else {
                        Toast.makeText(self, R.string.unzip_fail, 1).show();
                    }
                }
            };
            self.runOnUiThread(showMessage);
            Intent intent = new Intent(self, Twisty.class);
            startActivity(intent);
            finish();
        }
    }

    public static boolean unzip(String filename) {
        boolean wasValid = false;
        try {
            ZipFile zip = new ZipFile(filename);
            Enumeration<? extends ZipEntry> zippedFiles = zip.entries();
            while (zippedFiles.hasMoreElements()) {
                ZipEntry entry = zippedFiles.nextElement();
                InputStream is = zip.getInputStream(entry);
                String name = entry.getName();
                String nameWithoutNum = name;
                if (nameWithoutNum.length() > 3){
                    nameWithoutNum = nameWithoutNum.substring(0, nameWithoutNum.length() - 1);
                }
                if (nameWithoutNum.endsWith(".z") || nameWithoutNum.endsWith(".Z")) {
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

                    is.close();
                    out.close();
                    wasValid = true;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return wasValid;
    }

}
