package com.floreantpos.model;

import javax.xml.bind.annotation.XmlRootElement;
import com.floreantpos.model.base.BaseMenuItemIngredient;

@XmlRootElement(name = "menuItemIngredient")
public class MenuItemIngredient extends BaseMenuItemIngredient {
	private static final long serialVersionUID = 1L;

	public MenuItemIngredient () {
		super();
	}

	public MenuItemIngredient (java.lang.Integer id) {
		super(id);
	}

	@Override
	public String toString() {
		if (getIngredient() != null) {
			return getIngredient().getName();
		}
		return ""; //$NON-NLS-1$
	}
}
