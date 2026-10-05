package com.floreantpos.services.webprint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import com.floreantpos.model.Ticket;
import com.floreantpos.model.User;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebPrintOrchestratorTest {

	private static class MockWebPrintOrchestrator extends WebPrintOrchestrator {
		private List<Integer> printedTicketIds = new ArrayList<Integer>();

		@Override
		public boolean printTicket(WebTicketDTO dto) {
			if (dto != null && dto.getTicketId() != null) {
				printedTicketIds.add(dto.getTicketId());
				dto.setStatus(WebTicketDTO.PrintStatus.PRINTED);
				return true;
			}
			return false;
		}

		public List<Integer> getPrintedTicketIds() {
			return printedTicketIds;
		}
	}

	@Test
	public void testEvaluateAutoPrint_WaiterTicket_PrintsImmediately_EvenWhenAutoPrintDisabled() {
		MockWebPrintOrchestrator orchestrator = new MockWebPrintOrchestrator();
		orchestrator.setAutoPrintEnabled(false); // Auto-print toggled OFF

		User waiter = new User();
		waiter.setAutoId(14); // ownerId > 1

		Ticket ticket = new Ticket();
		ticket.setId(501);
		ticket.setOwner(waiter);

		WebTicketDTO dto = new WebTicketDTO();
		dto.setTicketId(501);
		dto.setTicket(ticket);
		dto.setOrderTime(new Date()); // 0 seconds elapsed

		List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		tickets.add(dto);

		orchestrator.evaluateAutoPrint(tickets);

		Assert.assertTrue("Waiter ticket should print immediately even when autoPrintEnabled is false",
				orchestrator.getPrintedTicketIds().contains(501));
		Assert.assertEquals(WebTicketDTO.PrintStatus.PRINTED, dto.getStatus());
	}

	@Test
	public void testEvaluateAutoPrint_CustomerTicket_DoesNotPrintBeforeCountdown() {
		MockWebPrintOrchestrator orchestrator = new MockWebPrintOrchestrator();
		orchestrator.setAutoPrintEnabled(true);

		User systemUser = new User();
		systemUser.setAutoId(1); // Default customer owner

		Ticket ticket = new Ticket();
		ticket.setId(502);
		ticket.setOwner(systemUser);

		WebTicketDTO dto = new WebTicketDTO();
		dto.setTicketId(502);
		dto.setTicket(ticket);
		dto.setOrderTime(new Date()); // 0 seconds elapsed

		List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		tickets.add(dto);

		orchestrator.evaluateAutoPrint(tickets);

		Assert.assertFalse("Customer ticket should not print before 120s countdown",
				orchestrator.getPrintedTicketIds().contains(502));
		Assert.assertEquals(WebTicketDTO.PrintStatus.PENDING, dto.getStatus());
	}

	@Test
	public void testEvaluateAutoPrint_SuspendsAutoRetry_WhenStatusIsPrintError() {
		MockWebPrintOrchestrator orchestrator = new MockWebPrintOrchestrator();
		orchestrator.setAutoPrintEnabled(true);

		User waiter = new User();
		waiter.setAutoId(14);

		Ticket ticket = new Ticket();
		ticket.setId(503);
		ticket.setOwner(waiter);

		WebTicketDTO dto = new WebTicketDTO();
		dto.setTicketId(503);
		dto.setTicket(ticket);
		dto.setOrderTime(new Date());
		dto.setStatus(WebTicketDTO.PrintStatus.PRINT_ERROR);

		List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		tickets.add(dto);

		orchestrator.evaluateAutoPrint(tickets);

		Assert.assertFalse("Tickets in PRINT_ERROR status should suspend auto-print retries",
				orchestrator.getPrintedTicketIds().contains(503));
	}
}
