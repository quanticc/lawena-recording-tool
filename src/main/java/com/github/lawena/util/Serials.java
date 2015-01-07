package com.github.lawena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@SuppressWarnings("resource")
public class Serials {

  private Serials() {}

  public static void write(Serializable s, File dest) throws IOException {
    FileOutputStream fos = new FileOutputStream(dest);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(s);
    oos.close();
    fos.close();
  }

  public static <T> T read(Class<T> cls, File src) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(src);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object o = ois.readObject();
    ois.close();
    fis.close();
    return cls.cast(o);
  }

  public static void gzWrite(Serializable s, File dest) throws IOException {
    FileOutputStream fos = new FileOutputStream(dest);
    GZIPOutputStream gz = new GZIPOutputStream(fos);
    ObjectOutputStream oos = new ObjectOutputStream(gz);
    oos.writeObject(s);
    oos.close();
    fos.close();
  }

  public static <T> T gzRead(Class<T> cls, File src) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(src);
    GZIPInputStream gs = new GZIPInputStream(fis);
    ObjectInputStream ois = new ObjectInputStream(gs);
    Object o = ois.readObject();
    ois.close();
    fis.close();
    return cls.cast(o);
  }

}
