package com.floreantpos.services.webprint;

import org.junit.Assert;
import org.junit.Test;

import com.floreantpos.model.Customer;
import com.floreantpos.model.Ticket;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebTicketDTOTest {

	@Test
	public void testCustomerNameWithIdAndCustomerEntity() {
		Ticket ticket = new Ticket() {
			private Customer customer;

			@Override
			public Integer getCustomerId() {
				return 105;
			}

			@Override
			public Customer getCustomer() {
				if (customer == null) {
					customer = new Customer();
					customer.setFirstName("John");
					customer.setLastName("Doe");
				}
				return customer;
			}
		};

		WebTicketDTO dto = new WebTicketDTO(ticket);
		Assert.assertEquals("105 - John Doe", dto.getCustomerName());
	}

	@Test
	public void testCustomerNameWithIdAndCustomerProperty() {
		Ticket ticket = new Ticket() {
			@Override
			public Integer getCustomerId() {
				return 200;
			}

			@Override
			public Customer getCustomer() {
				return null;
			}
		};
		ticket.addProperty(Ticket.CUSTOMER_NAME, "Jane Smith");

		WebTicketDTO dto = new WebTicketDTO(ticket);
		Assert.assertEquals("200 - Jane Smith", dto.getCustomerName());
	}

	@Test
	public void testCustomerNameWithIdOnly() {
		Ticket ticket = new Ticket() {
			@Override
			public Integer getCustomerId() {
				return 300;
			}

			@Override
			public Customer getCustomer() {
				return null;
			}
		};

		WebTicketDTO dto = new WebTicketDTO(ticket);
		Assert.assertEquals("Customer #300", dto.getCustomerName());
	}

	@Test
	public void testCustomerNameWithPropertyOnly() {
		Ticket ticket = new Ticket() {
			@Override
			public Integer getCustomerId() {
				return null;
			}

			@Override
			public Customer getCustomer() {
				return null;
			}
		};
		ticket.addProperty(Ticket.CUSTOMER_NAME, "Guest User");

		WebTicketDTO dto = new WebTicketDTO(ticket);
		Assert.assertEquals("Guest User", dto.getCustomerName());
	}

	@Test
	public void testCustomerNameWithNoCustomerInfo() {
		Ticket ticket = new Ticket() {
			@Override
			public Integer getCustomerId() {
				return null;
			}

			@Override
			public Customer getCustomer() {
				return null;
			}
		};

		WebTicketDTO dto = new WebTicketDTO(ticket);
		Assert.assertEquals("Web Guest", dto.getCustomerName());
	}
}
