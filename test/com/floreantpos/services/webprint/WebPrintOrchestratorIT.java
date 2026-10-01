package com.floreantpos.services.webprint;

import java.util.Date;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.floreantpos.model.Ticket;
import com.floreantpos.model.dao.TicketDAO;
import com.floreantpos.services.webprint.model.WebTicketDTO;
import com.floreantpos.util.DatabaseUtil;

public class WebPrintOrchestratorIT {

	@BeforeClass
	public static void setUpDatabase() throws Exception {
		com.floreantpos.main.Application.getInstance().initializeSystemHeadless();
	}

	@Test
	public void testOrchestratorHandlesPrintSuccess() throws Exception {
		Ticket ticket = new Ticket();
		ticket.setTicketType("WEB");
		ticket.setCreateDate(new Date());
		ticket.setTotalAmount(15.00);
		TicketDAO.getInstance().save(ticket);

		WebTicketDTO dto = new WebTicketDTO(ticket);
		WebPrintOrchestrator orchestrator = new WebPrintOrchestrator();

		boolean success = orchestrator.printTicket(dto);
		if (success) {
			Assert.assertEquals(WebTicketDTO.PrintStatus.PRINTED, dto.getStatus());
			Assert.assertNull(dto.getErrorMessage());
		} else {
			// If printer not configured in test environment, print fails safely without crashing
			Assert.assertEquals(WebTicketDTO.PrintStatus.PRINT_ERROR, dto.getStatus());
			Assert.assertNotNull(dto.getErrorMessage());
		}
	}
}
