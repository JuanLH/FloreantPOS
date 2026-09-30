package com.floreantpos.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TicketDeliveryChargeTest {

	private Ticket ticket;

	@Before
	public void setUp() {
		ticket = new Ticket();
	}

	@Test
	public void testWebOrderWithDeliveryEnabled() {
		ticket.setIsWeb(true);
		ticket.setIsDelivery(true);
		ticket.setDeliveryCharge(5.0);

		assertEquals(Double.valueOf(5.0), ticket.getDeliveryCharge());
	}

	@Test
	public void testWebOrderWithDeliveryDisabled() {
		ticket.setIsWeb(true);
		ticket.setIsDelivery(false);
		ticket.setDeliveryCharge(5.0);

		assertEquals(Double.valueOf(0.0), ticket.getDeliveryCharge());
	}

	@Test
	public void testPosOrderWithDeliveryOrderType() {
		ticket.setIsWeb(false);
		OrderType deliveryType = new OrderType();
		deliveryType.setDelivery(true);
		ticket.setOrderType(deliveryType);
		ticket.setDeliveryCharge(4.50);

		assertEquals(Double.valueOf(4.50), ticket.getDeliveryCharge());
	}

	@Test
	public void testPosOrderWithTakeOutOrderType() {
		ticket.setIsWeb(false);
		OrderType takeoutType = new OrderType();
		takeoutType.setDelivery(false);
		ticket.setOrderType(takeoutType);
		ticket.setDeliveryCharge(4.50);

		assertEquals(Double.valueOf(0.0), ticket.getDeliveryCharge());
	}

	@Test
	public void testWebOrderFallsBackToCustomerDeliveryCharge() {
		ticket.setIsWeb(true);
		ticket.setIsDelivery(true);
		ticket.setDeliveryCharge(0.0);

		Customer customer = new Customer();
		customer.setDeliveryCharge(3.75);
		ticket.setCustomer(customer);

		assertEquals(Double.valueOf(3.75), ticket.getDeliveryCharge());
	}
}
