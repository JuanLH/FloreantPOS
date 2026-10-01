package com.floreantpos.services.webprint;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebPrintAudioPlayer {
	private static final Log logger = LogFactory.getLog(WebPrintAudioPlayer.class);
	private static final String ALARM_FILE_PATH_WAV = "assets/WebPrintAlarm.wav";
	private static final String ALARM_FILE_PATH_MP3 = "assets/WebPrintAlarm.mp3";
	private Clip audioClip;
	private boolean playing = false;

	public WebPrintAudioPlayer() {
	}

	public File getAudioFile() {
		File wavFile = new File(ALARM_FILE_PATH_WAV);
		if (wavFile.exists() && wavFile.isFile()) {
			return wavFile;
		}
		File mp3File = new File(ALARM_FILE_PATH_MP3);
		if (mp3File.exists() && mp3File.isFile()) {
			return mp3File;
		}
		return null;
	}

	public boolean assetExists() {
		return getAudioFile() != null;
	}

	public synchronized void startLooping() {
		if (playing) {
			return;
		}
		File audioFile = getAudioFile();
		if (audioFile == null) {
			logger.warn("Alarm asset file does not exist (assets/WebPrintAlarm.wav or assets/WebPrintAlarm.mp3). Audio suppressed silently.");
			return;
		}
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			audioClip = AudioSystem.getClip();
			audioClip.open(audioStream);
			audioClip.loop(Clip.LOOP_CONTINUOUSLY);
			audioClip.start();
			playing = true;
			logger.info("Alarm audio playback started using " + audioFile.getName());
		} catch (Exception e) {
			logger.error("Failed to play alarm audio asset: " + audioFile.getPath() + ". Audio suppressed.", e);
			stop();
		}
	}

	public synchronized void stop() {
		if (audioClip != null) {
			try {
				if (audioClip.isRunning()) {
					audioClip.stop();
				}
				audioClip.close();
			} catch (Exception e) {
				logger.error("Error closing audio clip", e);
			}
			audioClip = null;
		}
		playing = false;
	}

	public boolean isPlaying() {
		return playing;
	}
}
