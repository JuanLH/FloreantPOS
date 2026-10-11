package com.floreantpos.model.dao;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.floreantpos.model.Customer;
import com.floreantpos.model.CustomerAddress;

public class CustomerAddressDAO extends BaseCustomerAddressDAO {

	public CustomerAddressDAO() {}

	@SuppressWarnings("unchecked")
	public List<CustomerAddress> findByCustomer(Customer customer) {
		if (customer == null || customer.getAutoId() == null) {
			return Collections.emptyList();
		}
		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(getReferenceClass());
			criteria.add(Restrictions.eq("customer", customer));
			criteria.addOrder(Order.desc("isDefault"));
			criteria.addOrder(Order.asc("id"));
			return criteria.list();
		} finally {
			closeSession(session);
		}
	}

	@SuppressWarnings("unchecked")
	public List<CustomerAddress> findByCustomerId(Integer customerId) {
		if (customerId == null) {
			return Collections.emptyList();
		}
		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(getReferenceClass());
			criteria.add(Restrictions.eq("customer.autoId", customerId));
			criteria.addOrder(Order.desc("isDefault"));
			criteria.addOrder(Order.asc("id"));
			return criteria.list();
		} finally {
			closeSession(session);
		}
	}
}
