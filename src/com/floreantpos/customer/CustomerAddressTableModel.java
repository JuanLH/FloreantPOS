package com.floreantpos.customer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.floreantpos.model.CustomerAddress;

public class CustomerAddressTableModel extends AbstractTableModel {

	private static final String[] COLUMN_NAMES = { "Label", "Address", "Default" };
	private static final Class<?>[] COLUMN_CLASSES = { String.class, String.class, Boolean.class };

	private List<CustomerAddress> addresses = new ArrayList<CustomerAddress>();

	public CustomerAddressTableModel() {}

	public CustomerAddressTableModel(List<CustomerAddress> addresses) {
		setAddresses(addresses);
	}

	public void setAddresses(List<CustomerAddress> addresses) {
		if (addresses == null) {
			this.addresses = new ArrayList<CustomerAddress>();
		} else {
			this.addresses = new ArrayList<CustomerAddress>(addresses);
		}
		ensureOneDefault();
		fireTableDataChanged();
	}

	public List<CustomerAddress> getAddresses() {
		return addresses;
	}

	public CustomerAddress getAddress(int row) {
		if (row >= 0 && row < addresses.size()) {
			return addresses.get(row);
		}
		return null;
	}

	public void addAddress(CustomerAddress address) {
		if (address == null) return;
		if (Boolean.TRUE.equals(address.getIsDefault()) || addresses.isEmpty()) {
			address.setIsDefault(true);
			clearOtherDefaults(null);
		}
		addresses.add(address);
		ensureOneDefault();
		fireTableDataChanged();
	}

	public void updateAddress(int row, CustomerAddress address) {
		if (row >= 0 && row < addresses.size()) {
			if (Boolean.TRUE.equals(address.getIsDefault())) {
				clearOtherDefaults(address);
			}
			addresses.set(row, address);
			ensureOneDefault();
			fireTableDataChanged();
		}
	}

	public CustomerAddress removeAddress(int row) {
		if (row >= 0 && row < addresses.size()) {
			CustomerAddress removed = addresses.remove(row);
			if (Boolean.TRUE.equals(removed.getIsDefault()) && !addresses.isEmpty()) {
				addresses.get(0).setIsDefault(true);
			}
			fireTableDataChanged();
			return removed;
		}
		return null;
	}

	public void setDefaultAt(int row) {
		if (row >= 0 && row < addresses.size()) {
			for (int i = 0; i < addresses.size(); i++) {
				addresses.get(i).setIsDefault(i == row);
			}
			fireTableDataChanged();
		}
	}

	public CustomerAddress getDefaultAddress() {
		for (CustomerAddress a : addresses) {
			if (Boolean.TRUE.equals(a.getIsDefault())) {
				return a;
			}
		}
		if (!addresses.isEmpty()) {
			return addresses.get(0);
		}
		return null;
	}

	private void clearOtherDefaults(CustomerAddress current) {
		for (CustomerAddress a : addresses) {
			if (a != current) {
				a.setIsDefault(false);
			}
		}
	}

	private void ensureOneDefault() {
		if (addresses.isEmpty()) return;
		boolean hasDefault = false;
		for (CustomerAddress a : addresses) {
			if (Boolean.TRUE.equals(a.getIsDefault())) {
				if (hasDefault) {
					a.setIsDefault(false);
				} else {
					hasDefault = true;
				}
			}
		}
		if (!hasDefault && !addresses.isEmpty()) {
			addresses.get(0).setIsDefault(true);
		}
	}

	@Override
	public int getRowCount() {
		return addresses.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASSES[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		CustomerAddress addr = addresses.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return addr.getAddressLabel();
		case 1:
			return addr.getAddressText();
		case 2:
			return addr.getIsDefault();
		default:
			return null;
		}
	}
}
