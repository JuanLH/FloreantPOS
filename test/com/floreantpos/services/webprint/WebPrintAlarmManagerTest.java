package com.floreantpos.services.webprint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebPrintAlarmManagerTest {
	private MockAudioPlayer mockAudioPlayer;
	private WebPrintAlarmManager alarmManager;

	private static class MockAudioPlayer extends WebPrintAudioPlayer {
		private boolean looping = false;

		@Override
		public boolean assetExists() {
			return true;
		}

		@Override
		public synchronized void startLooping() {
			looping = true;
		}

		@Override
		public synchronized void stop() {
			looping = false;
		}

		@Override
		public boolean isPlaying() {
			return looping;
		}
	}

	@Before
	public void setUp() {
		mockAudioPlayer = new MockAudioPlayer();
		alarmManager = new WebPrintAlarmManager(mockAudioPlayer);
	}

	@Test
	public void testAlarmSoundsWhenUnprintedTicketUnder2MinutesExists() {
		List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		WebTicketDTO dto = new WebTicketDTO();
		dto.setTicketId(101);
		dto.setOrderTime(new Date()); // 0 elapsed seconds
		tickets.add(dto);

		alarmManager.evaluateAlarm(tickets);

		Assert.assertTrue("Alarm should be playing for unprinted ticket under 2m", mockAudioPlayer.isPlaying());
		Assert.assertEquals(WebTicketDTO.PrintStatus.ALARMING, dto.getStatus());
	}

	@Test
	public void testAlarmAutoStopsWhenTicketExceeds2Minutes() {
		List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		WebTicketDTO dto = new WebTicketDTO();
		dto.setTicketId(102);
		// Set orderTime to 130 seconds ago
		dto.setOrderTime(new Date(System.currentTimeMillis() - 130000L));
		tickets.add(dto);

		alarmManager.evaluateAlarm(tickets);

		Assert.assertFalse("Alarm should auto-stop when ticket exceeds 2m", mockAudioPlayer.isPlaying());
	}

	@Test
	public void testSilenceAlarmMutesAudioWhileRetainingUnprintedState() {
		List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		WebTicketDTO dto = new WebTicketDTO();
		dto.setTicketId(103);
		dto.setOrderTime(new Date());
		tickets.add(dto);

		alarmManager.evaluateAlarm(tickets);
		Assert.assertTrue(mockAudioPlayer.isPlaying());

		alarmManager.silenceAlarm(tickets);

		Assert.assertFalse("Alarm audio should be muted after silenceAlarm()", mockAudioPlayer.isPlaying());
		Assert.assertTrue(dto.isSilenced());
		Assert.assertEquals(WebTicketDTO.PrintStatus.SILENCED, dto.getStatus());
	}
}
