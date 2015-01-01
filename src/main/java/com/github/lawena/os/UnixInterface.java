package com.github.lawena.os;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public abstract class UnixInterface extends OSInterface {

  protected static Set<PosixFilePermission> perms777 = new HashSet<PosixFilePermission>();

  {
    perms777.add(PosixFilePermission.OWNER_READ);
    perms777.add(PosixFilePermission.OWNER_WRITE);
    perms777.add(PosixFilePermission.OWNER_EXECUTE);
    perms777.add(PosixFilePermission.GROUP_READ);
    perms777.add(PosixFilePermission.GROUP_WRITE);
    perms777.add(PosixFilePermission.GROUP_EXECUTE);
    perms777.add(PosixFilePermission.OTHERS_READ);
    perms777.add(PosixFilePermission.OTHERS_WRITE);
    perms777.add(PosixFilePermission.OTHERS_EXECUTE);
  }

  @Override
  public void setLookAndFeel() {
    // use java default: Nimbus
  }

  @Override
  public void closeHandles(Path path) {
    // no need to implement this yet
  }

  @Override
  public void delete(Path path) {
    // no need to implement this yet
  }

  @Override
  public String getSystemDxLevel() {
    return "90";
  }

  @Override
  public void setSystemDxLevel(String dxlevel) {

  }

  @Override
  public String getVTFCmdLocation() {
    throw new UnsupportedOperationException("Skybox previews are not supported on this platform");
  }

}
