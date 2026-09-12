package com.floreantpos.model.dao;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

/**
 * Base DAO class for Ingredient.
 */
public abstract class BaseIngredientDAO extends com.floreantpos.model.dao._RootDAO {

	public static IngredientDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static IngredientDAO getInstance () {
		if (null == instance) instance = new IngredientDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return com.floreantpos.model.Ingredient.class;
	}

	public Order getDefaultOrder () {
		return Order.asc("name");
	}

	public com.floreantpos.model.Ingredient cast (Object object) {
		return (com.floreantpos.model.Ingredient) object;
	}

	public com.floreantpos.model.Ingredient get(java.lang.Integer key) {
		return (com.floreantpos.model.Ingredient) get(getReferenceClass(), key);
	}

	public com.floreantpos.model.Ingredient get(java.lang.Integer key, Session s) {
		return (com.floreantpos.model.Ingredient) get(getReferenceClass(), key, s);
	}

	public com.floreantpos.model.Ingredient load(java.lang.Integer key) {
		return (com.floreantpos.model.Ingredient) load(getReferenceClass(), key);
	}

	public com.floreantpos.model.Ingredient load(java.lang.Integer key, Session s) {
		return (com.floreantpos.model.Ingredient) load(getReferenceClass(), key, s);
	}

	public com.floreantpos.model.Ingredient loadInitialize(java.lang.Integer key, Session s) {
		com.floreantpos.model.Ingredient obj = load(key, s);
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		}
		return obj;
	}

	public java.util.List<com.floreantpos.model.Ingredient> findAll () {
		return super.findAll();
	}

	public java.util.List<com.floreantpos.model.Ingredient> findAll (Order defaultOrder) {
		return super.findAll(defaultOrder);
	}

	public java.util.List<com.floreantpos.model.Ingredient> findAll (Session s, Order defaultOrder) {
		return super.findAll(s, defaultOrder);
	}

	public java.lang.Integer save(com.floreantpos.model.Ingredient ingredient) {
		return (java.lang.Integer) super.save(ingredient);
	}

	public java.lang.Integer save(com.floreantpos.model.Ingredient ingredient, Session s) {
		return (java.lang.Integer) save((Object) ingredient, s);
	}

	public void saveOrUpdate(com.floreantpos.model.Ingredient ingredient) {
		saveOrUpdate((Object) ingredient);
	}

	public void saveOrUpdate(com.floreantpos.model.Ingredient ingredient, Session s) {
		saveOrUpdate((Object) ingredient, s);
	}

	public void delete(java.lang.Integer id) {
		delete((Object) load(id));
	}

	public void delete(java.lang.Integer id, Session s) {
		delete((Object) load(id, s), s);
	}

	public void delete(com.floreantpos.model.Ingredient ingredient) {
		delete((Object) ingredient);
	}

	public void delete(com.floreantpos.model.Ingredient ingredient, Session s) {
		delete((Object) ingredient, s);
	}

	public void refresh (com.floreantpos.model.Ingredient ingredient, Session s) {
		super.refresh(ingredient, s);
	}
}
