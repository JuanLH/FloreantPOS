package com.floreantpos.services.webprint;

import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.floreantpos.model.Ticket;
import com.floreantpos.model.dao.TicketDAO;
import com.floreantpos.services.webprint.model.WebTicketDTO;
import com.floreantpos.util.DatabaseUtil;

public class WebTicketPollingServiceIT {

	@BeforeClass
	public static void setUpDatabase() throws Exception {
		com.floreantpos.main.Application.getInstance().initializeSystemHeadless();
	}

	@Test
	public void testPollPendingWebTickets() throws Exception {
		// Create a test WEB ticket
		Ticket ticket = new Ticket();
		ticket.setTicketType("WEB");
		ticket.setCreateDate(new Date());
		ticket.setTotalAmount(25.50);
		TicketDAO.getInstance().save(ticket);

		WebTicketPollingService pollingService = new WebTicketPollingService();
		final List<WebTicketDTO>[] resultHolder = new List[1];

		pollingService.setListener(new WebTicketPollingService.PollingListener() {
			@Override
			public void onTicketsPolled(List<WebTicketDTO> tickets) {
				resultHolder[0] = tickets;
			}

			@Override
			public void onPollingFailure(Exception e) {
				Assert.fail("Polling failed: " + e.getMessage());
			}
		});

		pollingService.pollPendingWebTickets();

		Assert.assertNotNull(resultHolder[0]);
		boolean foundTestTicket = false;
		for (WebTicketDTO dto : resultHolder[0]) {
			if (ticket.getId().equals(dto.getTicketId())) {
				foundTestTicket = true;
				Assert.assertEquals("WEB", dto.getTicket().getTicketType());
				break;
			}
		}
		Assert.assertTrue("Polled tickets should contain newly created WEB ticket", foundTestTicket);
	}
}
