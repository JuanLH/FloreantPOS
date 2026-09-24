package com.floreantpos.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.PackagingUnitConversion;

public class PackagingUnitConversionDAO extends BasePackagingUnitConversionDAO {

	public PackagingUnitConversionDAO() {}

	public PackagingUnitConversion findBySourceAndTarget(PackagingUnit source, PackagingUnit target) {
		if (source == null || target == null) return null;

		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(getReferenceClass());
			criteria.add(Restrictions.eq("sourceUnit", source)); //$NON-NLS-1$
			criteria.add(Restrictions.eq("targetUnit", target)); //$NON-NLS-1$
			List list = criteria.list();
			if (list != null && !list.isEmpty()) {
				return (PackagingUnitConversion) list.get(0);
			}
			return null;
		} finally {
			if (session != null) {
				closeSession(session);
			}
		}
	}

	public PackagingUnitConversion findActiveConversion(PackagingUnit source, PackagingUnit target) {
		if (source == null || target == null) return null;

		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(getReferenceClass());
			criteria.add(Restrictions.eq("sourceUnit", source)); //$NON-NLS-1$
			criteria.add(Restrictions.eq("targetUnit", target)); //$NON-NLS-1$
			criteria.add(Restrictions.eq("active", Boolean.TRUE)); //$NON-NLS-1$
			List list = criteria.list();
			if (list != null && !list.isEmpty()) {
				return (PackagingUnitConversion) list.get(0);
			}
			return null;
		} finally {
			if (session != null) {
				closeSession(session);
			}
		}
	}
}
