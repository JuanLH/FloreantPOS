package com.floreantpos.services.webprint;

import org.junit.Assert;
import org.junit.Test;

public class WebPrintAudioPlayerTest {

	@Test
	public void testAudioSuppressedSilentlyWhenAssetMissing() {
		WebPrintAudioPlayer player = new WebPrintAudioPlayer();
		
		// Attempting startLooping when asset file is missing must suppress without throwing exception
		try {
			player.startLooping();
			// If file does not exist, playing state remains false
			if (!player.assetExists()) {
				Assert.assertFalse("Audio should not play when asset file is missing", player.isPlaying());
			}
			player.stop();
		} catch (Exception e) {
			Assert.fail("startLooping() threw exception when audio asset was missing: " + e.getMessage());
		}
	}
}
