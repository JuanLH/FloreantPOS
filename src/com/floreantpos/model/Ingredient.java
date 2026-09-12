package com.floreantpos.model;

import javax.xml.bind.annotation.XmlRootElement;
import com.floreantpos.model.base.BaseIngredient;

@XmlRootElement(name = "ingredient")
public class Ingredient extends BaseIngredient {
	private static final long serialVersionUID = 1L;

	public Ingredient () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Ingredient (java.lang.Integer id) {
		super(id);
	}

	@Override
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		return ""; //$NON-NLS-1$
	}
}
