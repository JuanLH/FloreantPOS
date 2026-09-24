/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
package com.floreantpos.model;

import com.floreantpos.model.base.BaseRecepieItem;



public class RecepieItem extends BaseRecepieItem {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RecepieItem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RecepieItem (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public RecepieItem (
		java.lang.Integer id,
		com.floreantpos.model.Recepie recepie) {

		super (
			id,
			recepie);
	}

/*[CONSTRUCTOR MARKER END]*/

	public double getPurchasePrice() {
		if (getInventoryItem() != null && getQuantity() != null) {
			return getQuantity() * getInventoryItem().getUnitPurchasePrice();
		}
		return 0.0;
	}

	public double getSellingPrice() {
		if (getInventoryItem() != null && getQuantity() != null) {
			return getQuantity() * getInventoryItem().getUnitSellingPrice();
		}
		return 0.0;
	}

	public double getProfitPercentage() {
		double cost = getPurchasePrice();
		double selling = getSellingPrice();
		if (cost == 0.0) {
			return selling > 0.0 ? 100.0 : 0.0;
		}
		return ((selling - cost) / cost) * 100.0;
	}

}