/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.dts.spell.event.ProgressListener;

/**
 *
 * @author developer
 */
public class FileUtils {

  public static File getUserDir() {
    return new File(System.getProperty("user.home"));
  }

  public static File getJMySpellDir() {
    File dir = new File(getUserDir(), ".jmyspell");

    dir.mkdirs();

    return dir;
  }

  public static File getDictionariesDir() {
    File dir = new File(getJMySpellDir(), "dictionaries") ;

    dir.mkdirs();

    return dir ;
  }

  public static String extractNameAndExtension(String name) {
    int index = name.lastIndexOf('/');

    if (index != -1) {
      name = name.substring(index + 1);
    }

    return name;
  }

  public static String extractOnlyName(String name) {
    int index = name.lastIndexOf('.');
    String rootName;

    if (index != -1) {
      rootName = name.substring(0, index);
    } else {
      rootName = name;
    }

    index = name.lastIndexOf('/');

    if (index != -1) {
      rootName = rootName.substring(index + 1);
    }

    return rootName;
  }

  public static String extractOnlyName(File file) {
    return extractOnlyName(file.getName());
  }

  public static File extractRootFile(File file) {
    return new File(file.getParent(), extractOnlyName(file));
  }

  public static String getFileExtension(File file) {
    String name = extractOnlyName(file);
    String fullName = file.getName();

    return fullName.substring(name.length());
  }

  public static void copyStream(InputStream in, OutputStream out, ProgressListener listener) throws IOException {
    byte[] bytes = new byte[64 * 1024];
    int r = in.read(bytes);

    while (r > 0) {
      out.write(bytes, 0, r);
      r = in.read(bytes);
    }
  }

  public static void copyStreamToFile(File file, InputStream in, ProgressListener listener) throws IOException {
    FileOutputStream fo = new FileOutputStream(file);
    BufferedOutputStream out = new BufferedOutputStream(fo);

    try {
      copyStream(in, fo, listener);
    } finally {
      out.close();
    }
  }
}
