package com.floreantpos.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.floreantpos.PosException;
import com.floreantpos.model.Ingredient;

public class IngredientDAO extends BaseIngredientDAO {

	public IngredientDAO () {}

	/**
	 * Find an ingredient by name ignoring case (RF-6, RF-7).
	 */
	public Ingredient findByNameIgnoreCase(String name) {
		if (name == null || name.trim().isEmpty()) {
			return null;
		}
		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(getReferenceClass());
			criteria.add(Restrictions.eq("name", name.trim()).ignoreCase());
			List list = criteria.list();
			if (list != null && !list.isEmpty()) {
				return (Ingredient) list.get(0);
			}
			return null;
		} finally {
			closeSession(session);
		}
	}

	/**
	 * Deletes an ingredient and cascades removal of all MenuItem-Ingredient associations (RF-8, RF-9).
	 */
	@Override
	public void delete(Ingredient ingredient) {
		if (ingredient == null || ingredient.getId() == null) {
			return;
		}
		Session session = null;
		Transaction tx = null;
		try {
			session = createNewSession();
			tx = session.beginTransaction();
			delete(ingredient, session);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new PosException("Failed to delete ingredient: " + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			closeSession(session);
		}
	}

	@Override
	public void delete(Ingredient ingredient, Session s) {
		if (ingredient == null || ingredient.getId() == null) {
			return;
		}
		// First remove all associations in MenuItemIngredient referencing this ingredient (RF-9)
		s.createQuery("DELETE FROM MenuItemIngredient mii WHERE mii.ingredient.id = :ingId") //$NON-NLS-1$
		 .setInteger("ingId", ingredient.getId()) //$NON-NLS-1$
		 .executeUpdate();

		// Then delete the master ingredient (RF-8)
		s.delete(ingredient);
	}
}
