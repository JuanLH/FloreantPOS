package com.floreantpos.model.dao;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

/**
 * Base DAO class for MenuItemIngredient.
 */
public abstract class BaseMenuItemIngredientDAO extends com.floreantpos.model.dao._RootDAO {

	public static MenuItemIngredientDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static MenuItemIngredientDAO getInstance () {
		if (null == instance) instance = new MenuItemIngredientDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return com.floreantpos.model.MenuItemIngredient.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public com.floreantpos.model.MenuItemIngredient cast (Object object) {
		return (com.floreantpos.model.MenuItemIngredient) object;
	}

	public com.floreantpos.model.MenuItemIngredient get(java.lang.Integer key) {
		return (com.floreantpos.model.MenuItemIngredient) get(getReferenceClass(), key);
	}

	public com.floreantpos.model.MenuItemIngredient get(java.lang.Integer key, Session s) {
		return (com.floreantpos.model.MenuItemIngredient) get(getReferenceClass(), key, s);
	}

	public com.floreantpos.model.MenuItemIngredient load(java.lang.Integer key) {
		return (com.floreantpos.model.MenuItemIngredient) load(getReferenceClass(), key);
	}

	public com.floreantpos.model.MenuItemIngredient load(java.lang.Integer key, Session s) {
		return (com.floreantpos.model.MenuItemIngredient) load(getReferenceClass(), key, s);
	}

	public com.floreantpos.model.MenuItemIngredient loadInitialize(java.lang.Integer key, Session s) {
		com.floreantpos.model.MenuItemIngredient obj = load(key, s);
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		}
		return obj;
	}

	public java.util.List<com.floreantpos.model.MenuItemIngredient> findAll () {
		return super.findAll();
	}

	public java.util.List<com.floreantpos.model.MenuItemIngredient> findAll (Order defaultOrder) {
		return super.findAll(defaultOrder);
	}

	public java.util.List<com.floreantpos.model.MenuItemIngredient> findAll (Session s, Order defaultOrder) {
		return super.findAll(s, defaultOrder);
	}

	public java.lang.Integer save(com.floreantpos.model.MenuItemIngredient menuItemIngredient) {
		return (java.lang.Integer) super.save(menuItemIngredient);
	}

	public java.lang.Integer save(com.floreantpos.model.MenuItemIngredient menuItemIngredient, Session s) {
		return (java.lang.Integer) save((Object) menuItemIngredient, s);
	}

	public void saveOrUpdate(com.floreantpos.model.MenuItemIngredient menuItemIngredient) {
		saveOrUpdate((Object) menuItemIngredient);
	}

	public void saveOrUpdate(com.floreantpos.model.MenuItemIngredient menuItemIngredient, Session s) {
		saveOrUpdate((Object) menuItemIngredient, s);
	}

	public void delete(java.lang.Integer id) {
		delete((Object) load(id));
	}

	public void delete(java.lang.Integer id, Session s) {
		delete((Object) load(id, s), s);
	}

	public void delete(com.floreantpos.model.MenuItemIngredient menuItemIngredient) {
		delete((Object) menuItemIngredient);
	}

	public void delete(com.floreantpos.model.MenuItemIngredient menuItemIngredient, Session s) {
		delete((Object) menuItemIngredient, s);
	}

	public void refresh (com.floreantpos.model.MenuItemIngredient menuItemIngredient, Session s) {
		super.refresh(menuItemIngredient, s);
	}
}
