package com.floreantpos.services.webprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.floreantpos.services.webprint.model.WebPrintServiceState;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebPrintController {
	private static final Log logger = LogFactory.getLog(WebPrintController.class);

	private WebPrintHeartbeatService heartbeatService;
	private WebTicketPollingService pollingService;
	private WebPrintAlarmManager alarmManager;
	private WebPrintOrchestrator orchestrator;

	private WebPrintServiceState state = WebPrintServiceState.STOPPED;
	private List<WebTicketDTO> activeTickets = Collections.synchronizedList(new ArrayList<WebTicketDTO>());
	private List<WebPrintStateListener> listeners = Collections.synchronizedList(new ArrayList<WebPrintStateListener>());

	public WebPrintController() {
		this.alarmManager = new WebPrintAlarmManager();
		this.orchestrator = new WebPrintOrchestrator();
		initServices();
	}

	public WebPrintController(WebPrintAlarmManager alarmManager, WebPrintOrchestrator orchestrator) {
		this.alarmManager = alarmManager;
		this.orchestrator = orchestrator;
		initServices();
	}

	private void initServices() {
		this.heartbeatService = new WebPrintHeartbeatService(new WebPrintHeartbeatService.HeartbeatListener() {
			@Override
			public void onHeartbeatSuccess() {
				notifyHeartbeatStatus(true, "Heartbeat Active");
			}

			@Override
			public void onHeartbeatFailure(Exception e) {
				notifyHeartbeatStatus(false, "DB Heartbeat Error: " + e.getMessage());
			}
		});

		this.pollingService = new WebTicketPollingService(new WebTicketPollingService.PollingListener() {
			@Override
			public void onTicketsPolled(List<WebTicketDTO> polledTickets) {
				handlePolledTickets(polledTickets);
			}

			@Override
			public void onPollingFailure(Exception e) {
				notifyHeartbeatStatus(false, "DB Polling Error: " + e.getMessage());
			}
		});
	}

	public synchronized void startService() {
		if (state == WebPrintServiceState.RUNNING) {
			return;
		}
		heartbeatService.start();
		pollingService.start();
		state = WebPrintServiceState.RUNNING;
		notifyStateChanged(state);
		logger.info("WebPrintController service started.");
	}

	public synchronized void stopService() {
		if (state == WebPrintServiceState.STOPPED) {
			return;
		}
		heartbeatService.stop();
		pollingService.stop();
		alarmManager.stopAlarm();
		state = WebPrintServiceState.STOPPED;
		notifyStateChanged(state);
		logger.info("WebPrintController service stopped.");
	}

	public boolean isRunning() {
		return state == WebPrintServiceState.RUNNING;
	}

	public WebPrintServiceState getState() {
		return state;
	}

	public void setAutoPrintEnabled(boolean enabled) {
		orchestrator.setAutoPrintEnabled(enabled);
	}

	public boolean isAutoPrintEnabled() {
		return orchestrator.isAutoPrintEnabled();
	}

	public void silenceAlarm() {
		synchronized (activeTickets) {
			alarmManager.silenceAlarm(activeTickets);
			notifyTicketsUpdated(new ArrayList<WebTicketDTO>(activeTickets));
		}
	}

	public void manualPrintTicket(Integer ticketId) {
		WebTicketDTO target = null;
		synchronized (activeTickets) {
			for (WebTicketDTO dto : activeTickets) {
				if (dto.getTicketId() != null && dto.getTicketId().equals(ticketId)) {
					target = dto;
					break;
				}
			}
		}
		if (target != null) {
			boolean success = orchestrator.printTicket(target);
			if (!success) {
				notifyPrintError(ticketId, target.getErrorMessage());
			}
			synchronized (activeTickets) {
				alarmManager.evaluateAlarm(activeTickets);
				notifyTicketsUpdated(new ArrayList<WebTicketDTO>(activeTickets));
			}
		}
	}

	private void handlePolledTickets(List<WebTicketDTO> polledTickets) {
		synchronized (activeTickets) {
			Map<Integer, WebTicketDTO> existingMap = new HashMap<Integer, WebTicketDTO>();
			for (WebTicketDTO existing : activeTickets) {
				if (existing.getTicketId() != null) {
					existingMap.put(existing.getTicketId(), existing);
				}
			}

			activeTickets.clear();
			if (polledTickets != null) {
				for (WebTicketDTO polled : polledTickets) {
					if (polled.getTicketId() != null && existingMap.containsKey(polled.getTicketId())) {
						WebTicketDTO prev = existingMap.get(polled.getTicketId());
						polled.setStatus(prev.getStatus());
						polled.setErrorMessage(prev.getErrorMessage());
						polled.setSilenced(prev.isSilenced());
					}
					activeTickets.add(polled);
				}
			}
			orchestrator.evaluateAutoPrint(activeTickets);
			alarmManager.evaluateAlarm(activeTickets);
			notifyTicketsUpdated(new ArrayList<WebTicketDTO>(activeTickets));
		}
	}

	public void addStateListener(WebPrintStateListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeStateListener(WebPrintStateListener listener) {
		listeners.remove(listener);
	}

	private void notifyStateChanged(WebPrintServiceState newState) {
		for (WebPrintStateListener listener : listeners) {
			try {
				listener.onServiceStateChanged(newState);
			} catch (Exception e) {
				logger.error("Error notifying listener of state change", e);
			}
		}
	}

	private void notifyTicketsUpdated(List<WebTicketDTO> tickets) {
		for (WebPrintStateListener listener : listeners) {
			try {
				listener.onTicketsUpdated(tickets);
			} catch (Exception e) {
				logger.error("Error notifying listener of tickets update", e);
			}
		}
	}

	private void notifyHeartbeatStatus(boolean connected, String message) {
		for (WebPrintStateListener listener : listeners) {
			try {
				listener.onHeartbeatStatusChanged(connected, message);
			} catch (Exception e) {
				logger.error("Error notifying listener of heartbeat status", e);
			}
		}
	}

	private void notifyPrintError(Integer ticketId, String errorMessage) {
		for (WebPrintStateListener listener : listeners) {
			try {
				listener.onPrintError(ticketId, errorMessage);
			} catch (Exception e) {
				logger.error("Error notifying listener of print error", e);
			}
		}
	}

	public List<WebTicketDTO> getActiveTickets() {
		synchronized (activeTickets) {
			return new ArrayList<WebTicketDTO>(activeTickets);
		}
	}
}
