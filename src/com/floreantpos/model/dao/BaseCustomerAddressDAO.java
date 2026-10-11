package com.floreantpos.model.dao;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public abstract class BaseCustomerAddressDAO extends com.floreantpos.model.dao._RootDAO {

	public static CustomerAddressDAO instance;

	public static CustomerAddressDAO getInstance() {
		if (null == instance) instance = new CustomerAddressDAO();
		return instance;
	}

	public Class getReferenceClass() {
		return com.floreantpos.model.CustomerAddress.class;
	}

	public Order getDefaultOrder() {
		return null;
	}

	public com.floreantpos.model.CustomerAddress cast(Object object) {
		return (com.floreantpos.model.CustomerAddress) object;
	}

	public com.floreantpos.model.CustomerAddress get(java.lang.Integer key)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.CustomerAddress) get(getReferenceClass(), key);
	}

	public com.floreantpos.model.CustomerAddress get(java.lang.Integer key, Session s)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.CustomerAddress) get(getReferenceClass(), key, s);
	}

	public com.floreantpos.model.CustomerAddress load(java.lang.Integer key)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.CustomerAddress) load(getReferenceClass(), key);
	}

	public com.floreantpos.model.CustomerAddress load(java.lang.Integer key, Session s)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.CustomerAddress) load(getReferenceClass(), key, s);
	}

	public java.util.List<com.floreantpos.model.CustomerAddress> findAll() {
		return super.findAll();
	}

	public java.util.List<com.floreantpos.model.CustomerAddress> findAll(Order defaultOrder) {
		return super.findAll(defaultOrder);
	}

	public java.util.List<com.floreantpos.model.CustomerAddress> findAll(Session s, Order defaultOrder) {
		return super.findAll(s, defaultOrder);
	}

	public java.lang.Integer save(com.floreantpos.model.CustomerAddress customerAddress) {
		return (java.lang.Integer) super.save(customerAddress);
	}

	public java.lang.Integer save(com.floreantpos.model.CustomerAddress customerAddress, Session s) {
		return (java.lang.Integer) save((Object) customerAddress, s);
	}

	public void saveOrUpdate(com.floreantpos.model.CustomerAddress customerAddress) {
		saveOrUpdate((Object) customerAddress);
	}

	public void saveOrUpdate(com.floreantpos.model.CustomerAddress customerAddress, Session s) {
		saveOrUpdate((Object) customerAddress, s);
	}

	public void update(com.floreantpos.model.CustomerAddress customerAddress) {
		update((Object) customerAddress);
	}

	public void update(com.floreantpos.model.CustomerAddress customerAddress, Session s) {
		update((Object) customerAddress, s);
	}

	public void delete(com.floreantpos.model.CustomerAddress customerAddress) {
		delete((Object) customerAddress);
	}

	public void delete(com.floreantpos.model.CustomerAddress customerAddress, Session s) {
		delete((Object) customerAddress, s);
	}

	public void refresh(com.floreantpos.model.CustomerAddress customerAddress, Session s) {
		refresh((Object) customerAddress, s);
	}
}
