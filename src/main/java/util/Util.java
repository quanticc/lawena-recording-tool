package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {

  public static String now(String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(Calendar.getInstance().getTime());
  }

  private Util() {}

}
