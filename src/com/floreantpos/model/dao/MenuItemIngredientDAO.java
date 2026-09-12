package com.floreantpos.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.floreantpos.model.MenuItemIngredient;

public class MenuItemIngredientDAO extends BaseMenuItemIngredientDAO {

	public MenuItemIngredientDAO () {}

	public List<MenuItemIngredient> findByMenuItem(Integer menuItemId) {
		if (menuItemId == null) {
			return null;
		}
		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(getReferenceClass());
			criteria.add(Restrictions.eq("menuItem.id", menuItemId)); //$NON-NLS-1$
			return criteria.list();
		} finally {
			closeSession(session);
		}
	}
}
