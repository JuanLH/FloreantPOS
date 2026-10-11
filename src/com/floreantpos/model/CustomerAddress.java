package com.floreantpos.model;

import com.floreantpos.model.base.BaseCustomerAddress;

public class CustomerAddress extends BaseCustomerAddress {
	private static final long serialVersionUID = 1L;

	public CustomerAddress() {
		super();
	}

	public CustomerAddress(java.lang.Integer id) {
		super(id);
	}

	@Override
	public String toString() {
		if (getAddressLabel() != null && !getAddressLabel().trim().isEmpty()) {
			return getAddressLabel() + ": " + getAddressText();
		}
		return getAddressText();
	}
}
