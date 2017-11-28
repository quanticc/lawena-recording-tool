package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemoPreviewFactory {

	private static final Logger log = Logger.getLogger("lawena");
    private static final String n = System.getProperty("line.separator");

	public static DemoPreview getDemoPreview(Path demoPath) throws Exception {
        try (RandomAccessFile dp = new RandomAccessFile(demoPath.toString(), "r")) {
            return new DemoPreview(
                readString(8, dp),
                readIntBackwards(dp),
                readIntBackwards(dp),
                readString(DemoPreview.maxStringLength, dp),
                readString(DemoPreview.maxStringLength, dp),
                readString(DemoPreview.maxStringLength, dp),
                readString(DemoPreview.maxStringLength, dp),
                readFloatBackwards(dp),
                readIntBackwards(dp),
                readIntBackwards(dp),
                readIntBackwards(dp));
        } catch (Exception e) {
            log.log(Level.FINE, "Could not retrieve demo details", e);
            throw e;
        }
    }

	private static String readString(int length, RandomAccessFile r) {
		byte[] aux = new byte[length];
		try {
			r.read(aux);
			String result = new String(aux, Charset.forName("UTF-8"));
			return result.substring(0, result.indexOf(0));
		} catch (IOException e) {
			log.warning("Error while reading demo info: " + e);
		}
		return null;
	}

	private static float readFloatBackwards(RandomAccessFile r) {
		byte[] aux = new byte[4];
		try {
			r.read(aux);
			int value = 0;
			for (int i = 0; i < aux.length; i++) {
				value += (aux[i] & 0xff) << (8 * i);
			}
			return Float.intBitsToFloat(value);
		} catch (IOException e) {
			log.warning("Error while reading demo info: " + e);
		}
		return 0;
	}

	private static int readIntBackwards(RandomAccessFile r) {
		byte[] aux = new byte[4];
		try {
			r.read(aux);
			int value = 0;
			for (int i = 0; i < aux.length; i++) {
				value += (aux[i] & 0xff) << (8 * i);
			}
			return value;
		} catch (IOException e) {
			log.warning("Error while reading demo info: " + e);
		}
		return 0;
	}
}
