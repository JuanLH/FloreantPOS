package com.floreantpos.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.floreantpos.model.Customer;

public class CustomerSelectorTest {

	@Test
	public void testSingleCustomerSelection() {
		Customer c1 = new Customer();
		c1.setAutoId(1);
		c1.setFirstName("Solo");

		List<Customer> list = new ArrayList<Customer>();
		list.add(c1);

		CustomerTable table = new CustomerTable();
		CustomerListTableModel model = new CustomerListTableModel(list);
		table.setModel(model);

		table.setRowSelectionInterval(0, 0);
		Customer selected = table.getSelectedCustomer();
		assertNotNull(selected);
		assertEquals("Solo", selected.getFirstName());
		assertEquals(Integer.valueOf(1), selected.getAutoId());
	}

	@Test
	public void testMultipleCustomersUnsorted() {
		Customer c1 = new Customer();
		c1.setAutoId(1);
		c1.setFirstName("Charlie");

		Customer c2 = new Customer();
		c2.setAutoId(2);
		c2.setFirstName("Alice");

		Customer c3 = new Customer();
		c3.setAutoId(3);
		c3.setFirstName("Bob");

		List<Customer> list = new ArrayList<Customer>();
		list.add(c1);
		list.add(c2);
		list.add(c3);

		CustomerTable table = new CustomerTable();
		CustomerListTableModel model = new CustomerListTableModel(list);
		table.setModel(model);

		table.setRowSelectionInterval(0, 0);
		assertEquals("Charlie", table.getSelectedCustomer().getFirstName());

		table.setRowSelectionInterval(1, 1);
		assertEquals("Alice", table.getSelectedCustomer().getFirstName());

		table.setRowSelectionInterval(2, 2);
		assertEquals("Bob", table.getSelectedCustomer().getFirstName());
	}

	@Test
	public void testMultipleCustomersSortedAscending() {
		Customer c1 = new Customer();
		c1.setAutoId(1);
		c1.setFirstName("Charlie");

		Customer c2 = new Customer();
		c2.setAutoId(2);
		c2.setFirstName("Alice");

		Customer c3 = new Customer();
		c3.setAutoId(3);
		c3.setFirstName("Bob");

		List<Customer> list = new ArrayList<Customer>();
		list.add(c1);
		list.add(c2);
		list.add(c3);

		CustomerTable table = new CustomerTable();
		CustomerListTableModel model = new CustomerListTableModel(list);
		table.setModel(model);

		// Sort ascending by first name (column 0) -> Alice (view 0), Bob (view 1), Charlie (view 2)
		table.toggleSortOrder(0);

		// Select view row 0 (Alice)
		table.setRowSelectionInterval(0, 0);
		Customer selected0 = table.getSelectedCustomer();
		assertNotNull(selected0);
		assertEquals("Alice", selected0.getFirstName());
		assertEquals(Integer.valueOf(2), selected0.getAutoId());

		// Select view row 1 (Bob)
		table.setRowSelectionInterval(1, 1);
		Customer selected1 = table.getSelectedCustomer();
		assertNotNull(selected1);
		assertEquals("Bob", selected1.getFirstName());
		assertEquals(Integer.valueOf(3), selected1.getAutoId());

		// Select view row 2 (Charlie)
		table.setRowSelectionInterval(2, 2);
		Customer selected2 = table.getSelectedCustomer();
		assertNotNull(selected2);
		assertEquals("Charlie", selected2.getFirstName());
		assertEquals(Integer.valueOf(1), selected2.getAutoId());
	}

	@Test
	public void testMultipleCustomersSortedDescending() {
		Customer c1 = new Customer();
		c1.setAutoId(1);
		c1.setFirstName("Charlie");

		Customer c2 = new Customer();
		c2.setAutoId(2);
		c2.setFirstName("Alice");

		Customer c3 = new Customer();
		c3.setAutoId(3);
		c3.setFirstName("Bob");

		List<Customer> list = new ArrayList<Customer>();
		list.add(c1);
		list.add(c2);
		list.add(c3);

		CustomerTable table = new CustomerTable();
		CustomerListTableModel model = new CustomerListTableModel(list);
		table.setModel(model);

		// First toggle: ascending; second toggle: descending -> Charlie (0), Bob (1), Alice (2)
		table.toggleSortOrder(0);
		table.toggleSortOrder(0);

		table.setRowSelectionInterval(0, 0);
		assertEquals("Charlie", table.getSelectedCustomer().getFirstName());

		table.setRowSelectionInterval(1, 1);
		assertEquals("Bob", table.getSelectedCustomer().getFirstName());

		table.setRowSelectionInterval(2, 2);
		assertEquals("Alice", table.getSelectedCustomer().getFirstName());
	}

	@Test
	public void testNoSelectionReturnsNull() {
		Customer c1 = new Customer();
		c1.setAutoId(1);
		c1.setFirstName("Charlie");

		List<Customer> list = new ArrayList<Customer>();
		list.add(c1);

		CustomerTable table = new CustomerTable();
		CustomerListTableModel model = new CustomerListTableModel(list);
		table.setModel(model);

		table.clearSelection();
		assertNull(table.getSelectedCustomer());
	}
}
