package com.floreantpos.services.webprint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebPrintAlarmManager {
	private WebPrintAudioPlayer audioPlayer;
	private Set<Integer> silencedTicketIds = new HashSet<Integer>();

	public WebPrintAlarmManager() {
		this(new WebPrintAudioPlayer());
	}

	public WebPrintAlarmManager(WebPrintAudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	public synchronized void evaluateAlarm(List<WebTicketDTO> tickets) {
		boolean shouldSoundAlarm = false;
		if (tickets != null) {
			for (WebTicketDTO ticket : tickets) {
				ticket.updateElapsedSeconds();
				if (silencedTicketIds.contains(ticket.getTicketId())) {
					ticket.setSilenced(true);
					if (ticket.getStatus() == WebTicketDTO.PrintStatus.PENDING || ticket.getStatus() == WebTicketDTO.PrintStatus.ALARMING) {
						ticket.setStatus(WebTicketDTO.PrintStatus.SILENCED);
					}
				}

				if (!ticket.isSilenced() && ticket.getStatus() != WebTicketDTO.PrintStatus.PRINTED && ticket.getElapsedSeconds() <= 120) {
					shouldSoundAlarm = true;
					if (ticket.getStatus() == WebTicketDTO.PrintStatus.PENDING) {
						ticket.setStatus(WebTicketDTO.PrintStatus.ALARMING);
					}
				} else if (ticket.getElapsedSeconds() > 120 && (ticket.getStatus() == WebTicketDTO.PrintStatus.ALARMING || ticket.getStatus() == WebTicketDTO.PrintStatus.PENDING)) {
					ticket.setStatus(WebTicketDTO.PrintStatus.PENDING);
				}
			}
		}

		if (shouldSoundAlarm) {
			audioPlayer.startLooping();
		} else {
			audioPlayer.stop();
		}
	}

	public synchronized void silenceAlarm(List<WebTicketDTO> activeTickets) {
		if (activeTickets != null) {
			for (WebTicketDTO ticket : activeTickets) {
				if (ticket.getTicketId() != null) {
					silencedTicketIds.add(ticket.getTicketId());
					ticket.setSilenced(true);
					if (ticket.getStatus() == WebTicketDTO.PrintStatus.ALARMING || ticket.getStatus() == WebTicketDTO.PrintStatus.PENDING) {
						ticket.setStatus(WebTicketDTO.PrintStatus.SILENCED);
					}
				}
			}
		}
		audioPlayer.stop();
	}

	public synchronized void stopAlarm() {
		audioPlayer.stop();
	}

	public WebPrintAudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
}
