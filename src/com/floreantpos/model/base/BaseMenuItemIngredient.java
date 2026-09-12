package com.floreantpos.model.base;

import java.io.Serializable;

/**
 * This is an object that contains data related to the MENUITEM_INGREDIENT table.
 */
public abstract class BaseMenuItemIngredient implements Comparable, Serializable {

	public static String REF = "MenuItemIngredient"; //$NON-NLS-1$
	public static String PROP_ID = "id"; //$NON-NLS-1$
	public static String PROP_CAN_BE_REMOVED = "canBeRemoved"; //$NON-NLS-1$
	public static String PROP_INGREDIENT = "ingredient"; //$NON-NLS-1$
	public static String PROP_MENU_ITEM = "menuItem"; //$NON-NLS-1$

	// constructors
	public BaseMenuItemIngredient () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseMenuItemIngredient (java.lang.Integer id) {
		this.setId(id);
		initialize();
	}

	protected void initialize () {}

	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer id;

	// fields
	private java.lang.Boolean canBeRemoved = Boolean.FALSE;

	// many to one
	private com.floreantpos.model.Ingredient ingredient;
	private com.floreantpos.model.MenuItem menuItem;

	public java.lang.Integer getId () {
		return id;
	}

	public void setId (java.lang.Integer id) {
		this.id = id;
		this.hashCode = Integer.MIN_VALUE;
	}

	public java.lang.Boolean isCanBeRemoved () {
		return canBeRemoved == null ? Boolean.FALSE : canBeRemoved;
	}

	public java.lang.Boolean getCanBeRemoved () {
		return canBeRemoved == null ? Boolean.FALSE : canBeRemoved;
	}

	public void setCanBeRemoved (java.lang.Boolean canBeRemoved) {
		this.canBeRemoved = canBeRemoved;
	}

	public com.floreantpos.model.Ingredient getIngredient () {
		return ingredient;
	}

	public void setIngredient (com.floreantpos.model.Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public com.floreantpos.model.MenuItem getMenuItem () {
		return menuItem;
	}

	public void setMenuItem (com.floreantpos.model.MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof com.floreantpos.model.MenuItemIngredient)) return false;
		else {
			com.floreantpos.model.MenuItemIngredient menuItemIngredient = (com.floreantpos.model.MenuItemIngredient) obj;
			if (null == this.getId() || null == menuItemIngredient.getId()) return false;
			else return (this.getId().equals(menuItemIngredient.getId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getId().hashCode(); //$NON-NLS-1$
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}

	public int compareTo (Object obj) {
		if (obj.hashCode() > hashCode()) return 1;
		else if (obj.hashCode() < hashCode()) return -1;
		else return 0;
	}

	public String toString () {
		return super.toString();
	}
}
