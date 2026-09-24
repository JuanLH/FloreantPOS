package com.floreantpos.model.dao;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public abstract class BasePackagingUnitConversionDAO extends com.floreantpos.model.dao._RootDAO {

	public static PackagingUnitConversionDAO instance;

	public static PackagingUnitConversionDAO getInstance () {
		if (null == instance) instance = new PackagingUnitConversionDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return com.floreantpos.model.PackagingUnitConversion.class;
	}

	public Order getDefaultOrder () {
		return Order.asc("id"); //$NON-NLS-1$
	}

	public com.floreantpos.model.PackagingUnitConversion cast (Object object) {
		return (com.floreantpos.model.PackagingUnitConversion) object;
	}

	public com.floreantpos.model.PackagingUnitConversion get(java.lang.Integer key)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.PackagingUnitConversion) get(getReferenceClass(), key);
	}

	public com.floreantpos.model.PackagingUnitConversion get(java.lang.Integer key, Session s)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.PackagingUnitConversion) get(getReferenceClass(), key, s);
	}

	public com.floreantpos.model.PackagingUnitConversion load(java.lang.Integer key)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.PackagingUnitConversion) load(getReferenceClass(), key);
	}

	public com.floreantpos.model.PackagingUnitConversion load(java.lang.Integer key, Session s)
		throws org.hibernate.HibernateException {
		return (com.floreantpos.model.PackagingUnitConversion) load(getReferenceClass(), key, s);
	}

	public java.util.List<com.floreantpos.model.PackagingUnitConversion> findAll () {
		return super.findAll();
	}

	public java.util.List<com.floreantpos.model.PackagingUnitConversion> findAll (Order defaultOrder) {
		return super.findAll(defaultOrder);
	}

	public java.lang.Integer save(com.floreantpos.model.PackagingUnitConversion conversion)
		throws org.hibernate.HibernateException {
		return (java.lang.Integer) super.save(conversion);
	}

	public void saveOrUpdate(com.floreantpos.model.PackagingUnitConversion conversion)
		throws org.hibernate.HibernateException {
		saveOrUpdate((Object) conversion);
	}

	public void update(com.floreantpos.model.PackagingUnitConversion conversion) 
		throws org.hibernate.HibernateException {
		update((Object) conversion);
	}

	public void delete(java.lang.Integer id)
		throws org.hibernate.HibernateException {
		delete((Object) load(id));
	}

	public void delete(com.floreantpos.model.PackagingUnitConversion conversion)
		throws org.hibernate.HibernateException {
		delete((Object) conversion);
	}
}
