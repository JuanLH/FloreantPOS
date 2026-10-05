package com.floreantpos.services.webprint;

import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.floreantpos.model.KitchenTicket;
import com.floreantpos.model.Ticket;
import com.floreantpos.model.dao.KitchenTicketDAO;
import com.floreantpos.report.ReceiptPrintService;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebPrintOrchestrator {
	private static final Log logger = LogFactory.getLog(WebPrintOrchestrator.class);
	private boolean autoPrintEnabled = true;

	public WebPrintOrchestrator() {
	}

	public boolean isAutoPrintEnabled() {
		return autoPrintEnabled;
	}

	public void setAutoPrintEnabled(boolean autoPrintEnabled) {
		this.autoPrintEnabled = autoPrintEnabled;
	}

	public void evaluateAutoPrint(List<WebTicketDTO> tickets) {
		if (tickets == null) {
			return;
		}
		for (WebTicketDTO dto : tickets) {
			dto.updateElapsedSeconds();
			if (dto.getStatus() == WebTicketDTO.PrintStatus.PRINTED || dto.getStatus() == WebTicketDTO.PrintStatus.PRINT_ERROR) {
				continue;
			}

			Ticket ticket = dto.getTicket();
			Integer ownerAutoId = (ticket != null && ticket.getOwner() != null) ? ticket.getOwner().getAutoId() : null;
			Integer ownerUserId = (ticket != null && ticket.getOwner() != null) ? ticket.getOwner().getUserId() : null;
			boolean isWaiterTicket = (ownerAutoId != null && ownerAutoId > 1) || (ownerUserId != null && ownerUserId > 1);

			if (isWaiterTicket) {
				// RF-13 & RF-14: Waiter tickets print immediately, regardless of autoPrintEnabled
				printTicket(dto);
			} else if (autoPrintEnabled && dto.getElapsedSeconds() >= 120) {
				// Standard 2-minute review countdown
				printTicket(dto);
			}
		}
	}

	public boolean printTicket(WebTicketDTO dto) {
		if (dto == null || dto.getTicket() == null) {
			return false;
		}
		Ticket ticket = dto.getTicket();
		try {
			// Call FloreantPOS's existing kitchen print service
			ReceiptPrintService.printToKitchen(ticket);

			// On success across assigned printers, save KitchenTicket record
			KitchenTicket kitchenTicket = new KitchenTicket();
			kitchenTicket.setTicketId(ticket.getId());
			kitchenTicket.setCreateDate(new Date());
			kitchenTicket.setTicketType(ticket.getTicketType());
			KitchenTicketDAO.getInstance().save(kitchenTicket);

			dto.setStatus(WebTicketDTO.PrintStatus.PRINTED);
			dto.setErrorMessage(null);
			logger.info("Successfully printed WEB ticket #" + ticket.getId() + " to kitchen and created KitchenTicket record.");
			return true;
		} catch (Exception e) {
			logger.error("Failed to print WEB ticket #" + ticket.getId() + " to kitchen", e);
			dto.setStatus(WebTicketDTO.PrintStatus.PRINT_ERROR);
			dto.setErrorMessage("Print failed: " + e.getMessage());
			// DO NOT create KitchenTicket record so ticket remains unprinted for retry
			return false;
		}
	}
}
