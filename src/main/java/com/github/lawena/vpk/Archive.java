package com.github.lawena.vpk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a VPK archive.
 * 
 * @author Connor Haigh
 * @see <a href="https://github.com/Contron/JavaVPK">GitHub Repository</a>
 */
public class Archive {
  /**
   * Creates a new VPK archive.
   * 
   * @param file the archive file
   * @throws ArchiveException if the archive file is null
   */
  public Archive(File file) {
    if (file == null)
      throw new IllegalArgumentException("Archive file cannot be null");

    this.file = file;
    this.multiPart = false;

    this.signature = 0;
    this.version = 0;
    this.treeLength = 0;
    this.headerLength = 0;

    this.directories = new ArrayList<>();
  }

  /**
   * Load the raw data from file to this archive.
   * 
   * @throws IOException if the archive could not be read
   * @throws ArchiveException if a general archive exception occurs
   * @throws EntryException if a general entry exception occurs
   */
  public void load() throws IOException, ArchiveException, EntryException {
    try (FileInputStream fileInputStream = new FileInputStream(this.file)) {
      // check for multiple child archives
      this.multiPart = this.file.getName().contains("_dir");

      // read header
      this.signature = this.readUnsignedInt(fileInputStream);
      this.version = this.readUnsignedInt(fileInputStream);
      this.treeLength = this.readUnsignedInt(fileInputStream);

      // check signature and version
      if (this.signature != Archive.SIGNATURE)
        throw new ArchiveException("Invalid signature");
      if (this.version < Archive.MINIMUM_VERSION || this.version > Archive.MAXIMUM_VERSION)
        throw new ArchiveException("Unsupported version");

      // version handling
      switch (this.version) {
        case Archive.VERSION_ONE: {
          this.headerLength = Archive.VERSION_ONE_HEADER_SIZE;

          break;
        }
        case Archive.VERSION_TWO: {
          this.headerLength = Archive.VERSION_TWO_HEADER_SIZE;

          // read extra data
          // serves no purpose right now
          this.readUnsignedInt(fileInputStream);
          this.readUnsignedInt(fileInputStream);
          this.readUnsignedInt(fileInputStream);
          this.readUnsignedInt(fileInputStream);
          break;
        }
        default:
          break;
      }

      // extension loop
      while (true) {
        // get extension
        String extension = this.readString(fileInputStream);
        if (extension.isEmpty())
          break;

        // path loop
        while (true) {
          // get path
          String path = this.readString(fileInputStream);
          if (path.isEmpty())
            break;

          // directory
          Directory directory = new Directory(path);
          this.directories.add(directory);

          // filename loop
          while (true) {
            // get filename
            String filename = this.readString(fileInputStream);
            if (filename.isEmpty())
              break;

            // read data
            int crc = this.readUnsignedInt(fileInputStream);
            short preloadSize = this.readUnsignedShort(fileInputStream);
            short archiveIndex = this.readUnsignedShort(fileInputStream);
            int entryOffset = this.readUnsignedInt(fileInputStream);
            int entryLength = this.readUnsignedInt(fileInputStream);
            short terminator = this.readUnsignedShort(fileInputStream);

            // check preload data
            byte[] preloadData = null;
            if (preloadSize > 0) {
              // read preload data
              preloadData = new byte[preloadSize];
              fileInputStream.read(preloadData);
            }

            // create entry
            Entry entry =
                new Entry(this, archiveIndex, preloadData, filename, extension, crc, entryOffset,
                    entryLength, terminator);
            directory.addEntry(entry);
          }
        }
      }
    }
  }

  /**
   * Returns a child archive that belongs to this parent.
   * 
   * @param index the index of the archive
   * @return the child archive, or null
   * @throws ArchiveException if this archive is not made up of multiple children
   */
  public File getChildArchive(int index) throws ArchiveException {
    // check
    if (!this.multiPart)
      throw new ArchiveException("Archive is not multi-part");

    // get parent
    File parent = this.file.getParentFile();
    if (parent == null)
      throw new ArchiveException("Archive has no parent");

    // get child name
    String fileName = this.file.getName();
    String rootName = fileName.substring(0, fileName.length() - 8);
    String childName = String.format("%s_%03d.vpk", rootName, index);

    return new File(parent, childName);
  }

  /**
   * Reads a stream character by character until a null terminator is reached.
   * 
   * @param fileInputStream the stream to read
   * @return the assembled string
   * @throws IOException if the stream could not be read
   */
  private String readString(FileInputStream fileInputStream) throws IOException {
    // builder
    StringBuilder stringBuilder = new StringBuilder();

    // read
    int character = 0;
    while ((character = fileInputStream.read()) != Archive.NULL_TERMINATOR)
      stringBuilder.append((char) character);

    return stringBuilder.toString();
  }

  /**
   * Reads an unsigned integer (4 bytes) from a stream.
   * 
   * @param fileInputStream the stream to read
   * @return the unsigned integer
   * @throws IOException if the stream could not be read
   */
  private int readUnsignedInt(FileInputStream fileInputStream) throws IOException {
    // byte array
    byte[] buffer = new byte[4];
    fileInputStream.read(buffer);

    // byte buffer
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    return byteBuffer.getInt();
  }

  /**
   * Reads an unsigned short (2 bytes) from a stream.
   * 
   * @param fileInputStream the stream to read
   * @return the unsigned short
   * @throws IOException if the stream could not be read
   */
  private short readUnsignedShort(FileInputStream fileInputStream) throws IOException {
    // byte array
    byte[] buffer = new byte[2];
    fileInputStream.read(buffer);

    // byte buffer
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    return byteBuffer.getShort();
  }

  /**
   * Returns the VPK archive file for this archive.
   * 
   * @return the VPK archive file
   */
  public File getFile() {
    return this.file;
  }

  /**
   * Returns if this archive is made of multiple children (separate VPK archives).
   * 
   * @return if this archive is made of multiple children
   */
  public boolean isMultiPart() {
    return this.multiPart;
  }

  /**
   * Returns the signature of this archive. In most cases, this should be 0x55AA1234.
   * 
   * @return the signature
   */
  public int getSignature() {
    return this.signature;
  }

  /**
   * Returns the internal version of this archive. In most cases, this should be 2.
   * 
   * @return the internal version
   */
  public int getVersion() {
    return this.version;
  }

  /**
   * Returns the length of the root tree for this archive.
   * 
   * @return the length of the root tree
   */
  public int getTreeLength() {
    return this.treeLength;
  }

  /**
   * Returns the length of the header for this archive.
   * 
   * @return the length of the header
   */
  public int getHeaderLength() {
    return this.headerLength;
  }

  /**
   * Returns the list of directories in this archive.
   * 
   * @return the list of directories
   */
  public List<Directory> getDirectories() {
    return this.directories;
  }

  public static final int SIGNATURE = 0x55AA1234;
  public static final char NULL_TERMINATOR = 0x0;

  public static final int MINIMUM_VERSION = 1;
  public static final int MAXIMUM_VERSION = 2;

  public static final int VERSION_ONE = 1;
  public static final int VERSION_TWO = 2;
  public static final int VERSION_ONE_HEADER_SIZE = 12;
  public static final int VERSION_TWO_HEADER_SIZE = 28;

  private File file;
  private boolean multiPart;

  private int signature;
  private int version;
  private int treeLength;
  private int headerLength;

  private ArrayList<Directory> directories;
}
