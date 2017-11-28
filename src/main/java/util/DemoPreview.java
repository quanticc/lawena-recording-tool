package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemoPreview {

	private static final Logger log = Logger.getLogger("lawena");
    private static final String n = System.getProperty("line.separator");

	public final static int maxStringLength = 260;

	private int demoProtocol;
	private int networkProtocol;
	private String demoStamp;

    public int getDemoProtocol() {
        return demoProtocol;
    }

    public int getNetworkProtocol() {
        return networkProtocol;
    }

    public String getDemoStamp() {
        return demoStamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMapName() {
        return mapName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getGameDirectory() {
        return gameDirectory;
    }

    public double getPlaybackTime() {
        return playbackTime;
    }

    public int getTickNumber() {
        return tickNumber;
    }

    public int getFrames() {
        return frames;
    }

    public int getSignOnLength() {
        return signOnLength;
    }

    public int getTickRate() {
        return tickRate;
    }

    private String playerName;
	private String mapName;
	private String serverName;
	private String gameDirectory;
	private double playbackTime;
	private int tickNumber;
	private int frames;
	private int signOnLength;
	private int tickRate;

	public DemoPreview(String demoStamp, int demoProtocol, int networkProtocol, String serverName, String playerName,
                        String mapName, String gameDirectory, float playbackTime, int tickNumber, int frames,
                        int signOnLength) {

        this.demoStamp = demoStamp;
        this.demoProtocol = demoProtocol;
        this.networkProtocol = networkProtocol;
        this.serverName = serverName;
        this.playerName = playerName;
        this.mapName = mapName;
        this.gameDirectory = gameDirectory;
        this.playbackTime = playbackTime;
        this.tickNumber = tickNumber;
        this.frames = frames;
        this.signOnLength = signOnLength;
        this.tickRate = Math.round(tickNumber / Math.round(playbackTime));
	}

	private String formatSeconds(double seconds) {
		long s = (long) seconds;
		return String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(s),
				TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s)),
				TimeUnit.SECONDS.toSeconds(s) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(s)));
	}

	@Override
	public String toString() {
        return String.join(n,
            "Stamp: " + demoStamp,
            "DemoProtocol: " + demoProtocol,
            "NetworkProtocol: " + networkProtocol,
            "GameDirectory: " + gameDirectory,
            "PlaybackTime: " + formatSeconds(playbackTime),
            "Server: " + serverName,
            "Player: " + playerName,
            "Map: " + mapName,
            "Ticks: " + tickNumber,
            "Frames: " + frames,
            "SignOnLength: " + signOnLength,
            "TickRate: " + tickRate);
	}

}
