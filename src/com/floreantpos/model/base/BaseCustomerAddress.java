package com.floreantpos.model.base;

import java.io.Serializable;

/**
 * This is an object that contains data related to the CUSTOMER_ADDRESS table.
 *
 * @hibernate.class
 *  table="CUSTOMER_ADDRESS"
 */
public abstract class BaseCustomerAddress implements Comparable, Serializable {

	public static String REF = "CustomerAddress";
	public static String PROP_ID = "id";
	public static String PROP_CUSTOMER = "customer";
	public static String PROP_ADDRESS_LABEL = "addressLabel";
	public static String PROP_ADDRESS_TEXT = "addressText";
	public static String PROP_IS_DEFAULT = "isDefault";

	public BaseCustomerAddress() {
		initialize();
	}

	public BaseCustomerAddress(java.lang.Integer id) {
		this.setId(id);
		initialize();
	}

	protected void initialize() {}

	private int hashCode = Integer.MIN_VALUE;

	private java.lang.Integer id;
	protected java.lang.String addressLabel;
	protected java.lang.String addressText;
	protected java.lang.Boolean isDefault;
	private com.floreantpos.model.Customer customer;

	public java.lang.Integer getId() {
		return id;
	}

	public void setId(java.lang.Integer id) {
		this.id = id;
		this.hashCode = Integer.MIN_VALUE;
	}

	public java.lang.String getAddressLabel() {
		return addressLabel;
	}

	public void setAddressLabel(java.lang.String addressLabel) {
		this.addressLabel = addressLabel;
	}

	public java.lang.String getAddressText() {
		return addressText;
	}

	public void setAddressText(java.lang.String addressText) {
		this.addressText = addressText;
	}

	public java.lang.Boolean getIsDefault() {
		return isDefault == null ? Boolean.FALSE : isDefault;
	}

	public java.lang.Boolean isDefault() {
		return getIsDefault();
	}

	public void setIsDefault(java.lang.Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public com.floreantpos.model.Customer getCustomer() {
		return customer;
	}

	public void setCustomer(com.floreantpos.model.Customer customer) {
		this.customer = customer;
	}

	public boolean equals(Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof com.floreantpos.model.CustomerAddress)) return false;
		else {
			com.floreantpos.model.CustomerAddress customerAddress = (com.floreantpos.model.CustomerAddress) obj;
			if (null == this.getId() || null == customerAddress.getId()) {
				return super.equals(obj);
			}
			else return (this.getId().equals(customerAddress.getId()));
		}
	}

	public int hashCode() {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}

	public int compareTo(Object obj) {
		if (obj.hashCode() > hashCode()) return 1;
		else if (obj.hashCode() < hashCode()) return -1;
		else return 0;
	}

	public String toString() {
		return super.toString();
	}
}
