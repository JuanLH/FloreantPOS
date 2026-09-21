package com.floreantpos.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TicketItemModifierCalculationTest {

	private TicketItem ticketItem;
	private TicketItemModifier modifier;

	@Before
	public void setUp() {
		ticketItem = new TicketItem();
		ticketItem.setItemId(101);
		ticketItem.setName("Burger");
		ticketItem.setUnitPrice(10.00);
		ticketItem.setItemCount(2);
		ticketItem.setTicket(new Ticket());
		ticketItem.setTicketItemModifiers(new ArrayList<TicketItemModifier>());

		modifier = new TicketItemModifier();
		modifier.setModifierId(201);
		modifier.setName("Extra Cheese");
		modifier.setUnitPrice(1.50);
		modifier.setItemCount(2); // 1 per item * 2 items = 2 total
		modifier.setModifierType(TicketItemModifier.NORMAL_MODIFIER);
		modifier.setTicketItem(ticketItem);

		ticketItem.addToticketItemModifiers(modifier);
	}

	@Test
	public void testModifierCalculationWithMultipleItemQuantity() {
		ticketItem.calculatePrice();

		// Modifier subtotal: 2 * 1.50 = 3.00
		assertEquals(3.00, modifier.getSubTotalAmount(), 0.001);

		// Item subtotal including modifiers: (2 * 10.00) + 3.00 = 23.00
		assertEquals(23.00, ticketItem.getSubtotalAmount(), 0.001);

		// Item subtotal without modifiers: 2 * 10.00 = 20.00
		assertEquals(20.00, ticketItem.getSubtotalAmountWithoutModifiers(), 0.001);
	}

	@Test
	public void testScaleModifiersIncreaseQuantity() {
		// When increasing item quantity from 2 to 3
		ticketItem.setItemCount(3);
		ticketItem.scaleModifiers(2, 3);

		// Modifier count should scale from 2 to 3
		assertEquals(Integer.valueOf(3), modifier.getItemCount());

		// Recalculated subtotal: (3 * 10.00) + (3 * 1.50) = 34.50
		assertEquals(34.50, ticketItem.getSubtotalAmount(), 0.001);
		assertEquals(4.50, modifier.getSubTotalAmount(), 0.001);
	}

	@Test
	public void testScaleModifiersDecreaseQuantity() {
		// When decreasing item quantity from 2 to 1
		ticketItem.setItemCount(1);
		ticketItem.scaleModifiers(2, 1);

		// Modifier count should scale from 2 to 1
		assertEquals(Integer.valueOf(1), modifier.getItemCount());

		// Recalculated subtotal: (1 * 10.00) + (1 * 1.50) = 11.50
		assertEquals(11.50, ticketItem.getSubtotalAmount(), 0.001);
		assertEquals(1.50, modifier.getSubTotalAmount(), 0.001);
	}

	@Test
	public void testMultipleModifiersPerItemScaleProportionally() {
		// Suppose 2 extra cheeses per burger, so 2 * 2 = 4 total modifiers for 2 burgers
		modifier.setItemCount(4);
		ticketItem.calculatePrice();

		assertEquals(6.00, modifier.getSubTotalAmount(), 0.001); // 4 * 1.50
		assertEquals(26.00, ticketItem.getSubtotalAmount(), 0.001); // 20.00 + 6.00

		// Scale from 2 to 3 burgers -> 2 per burger * 3 burgers = 6 total modifiers
		ticketItem.setItemCount(3);
		ticketItem.scaleModifiers(2, 3);

		assertEquals(Integer.valueOf(6), modifier.getItemCount());
		assertEquals(9.00, modifier.getSubTotalAmount(), 0.001); // 6 * 1.50
		assertEquals(39.00, ticketItem.getSubtotalAmount(), 0.001); // 30.00 + 9.00
	}

	@Test
	public void testModifierNameDisplayWithMultipleItems() {
		// With 2 items and 2 modifiers (1 per item), display shows modifier count + name + unit (ea / c/u) by default
		String display1 = modifier.getNameDisplay();
		assertNotNull(display1);
		assertTrue("Should include modifier count by default", display1.startsWith("1 "));
		assertTrue("Should show modifier name", display1.contains("Extra Cheese"));
		assertTrue("Should include unit (ea / c/u) by default", display1.endsWith("ea") || display1.endsWith("c/u"));

		// With 2 items and 4 modifiers (2 per item), display shows "2 ... ea" (or "2 ... c/u")
		modifier.setItemCount(4);
		String display2 = modifier.getNameDisplay();
		assertNotNull(display2);
		assertTrue("Should indicate multiple per item", display2.startsWith("2 "));
		assertTrue("Should include unit (ea / c/u)", display2.endsWith("ea") || display2.endsWith("c/u"));
	}

	@Test
	public void testScaleAddOnsAndSizeModifier() {
		TicketItemModifier addOn = new TicketItemModifier();
		addOn.setModifierId(301);
		addOn.setName("Bacon AddOn");
		addOn.setUnitPrice(2.00);
		addOn.setItemCount(2); // 1 per item * 2 items
		addOn.setTicketItem(ticketItem);

		ticketItem.setAddOns(new ArrayList<TicketItemModifier>());
		ticketItem.addToaddOns(addOn);

		TicketItemModifier sizeMod = new TicketItemModifier();
		sizeMod.setModifierId(401);
		sizeMod.setName("Large Crust");
		sizeMod.setUnitPrice(3.00);
		sizeMod.setItemCount(2); // 1 per item * 2 items
		sizeMod.setTicketItem(ticketItem);
		ticketItem.setSizeModifier(sizeMod);

		// Scale from 2 to 4 items
		ticketItem.setItemCount(4);
		ticketItem.scaleModifiers(2, 4);

		assertEquals(Integer.valueOf(4), modifier.getItemCount());
		assertEquals(Integer.valueOf(4), addOn.getItemCount());
		assertEquals(Integer.valueOf(4), sizeMod.getItemCount());
	}
}
