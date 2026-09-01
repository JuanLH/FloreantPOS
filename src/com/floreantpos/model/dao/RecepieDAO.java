package com.floreantpos.model.dao;

import java.util.List;

import com.floreantpos.model.MenuItem;
import com.floreantpos.model.Recepie;

public class RecepieDAO extends BaseRecepieDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public RecepieDAO () {}

	private static RecepieDAO instance;

	public static RecepieDAO getInstance() {
		if (instance == null) {
			instance = new RecepieDAO();
		}
		return instance;
	}

	/**
	 * Finds the Recepie linked to a specific MenuItem.
	 * @param menuItem the MenuItem to search for
	 * @return the Recepie, or null if none exists yet
	 */
	@SuppressWarnings("unchecked")
	public Recepie findByMenuItem(MenuItem menuItem) {
		if (menuItem == null) return null;
		org.hibernate.Session session = null;
		try {
			session = createNewSession();
			java.util.List<Recepie> list = session
				.createQuery("from Recepie where menuItem = :mi") //$NON-NLS-1$
				.setParameter("mi", menuItem) //$NON-NLS-1$
				.list();
			if (list != null && !list.isEmpty()) {
				return list.get(0);
			}
		} finally {
			closeSession(session);
		}
		return null;
	}

}