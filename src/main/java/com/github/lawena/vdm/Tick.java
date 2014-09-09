package com.github.lawena.vdm;

public class Tick {

  private String demoname;
  private int start;
  private int end;

  public Tick(String demoname, int start, int end) {
    if (demoname == null)
      throw new IllegalArgumentException("Must enter demoname");
    if (start < 0 || end < 0)
      throw new IllegalArgumentException("Tick values must not be negative");
    if (start >= end)
      throw new IllegalArgumentException("Starting tick must be lower than ending tick");
    this.demoname = demoname;
    this.start = start;
    this.end = end;
  }

  public String getDemoname() {
    return demoname;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((demoname == null) ? 0 : demoname.hashCode());
    result = prime * result + end;
    result = prime * result + start;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Tick other = (Tick) obj;
    if (demoname == null) {
      if (other.demoname != null)
        return false;
    } else if (!demoname.equals(other.demoname))
      return false;
    if (end != other.end)
      return false;
    if (start != other.start)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return demoname + ": " + start + "-" + end;
  }

}
