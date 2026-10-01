package com.floreantpos.services.webprint;

import java.util.List;
import com.floreantpos.services.webprint.model.WebPrintServiceState;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public interface WebPrintStateListener {
	void onServiceStateChanged(WebPrintServiceState state);
	void onTicketsUpdated(List<WebTicketDTO> tickets);
	void onHeartbeatStatusChanged(boolean connected, String message);
	void onPrintError(Integer ticketId, String errorMessage);
}
