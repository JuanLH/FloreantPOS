package com.floreantpos.model.base;

import java.io.Serializable;

/**
 * This is an object that contains data related to the PACKAGING_UNIT_CONVERSION table.
 *
 * @hibernate.class
 *  table="PACKAGING_UNIT_CONVERSION"
 */
public abstract class BasePackagingUnitConversion implements Comparable, Serializable {

	public static String REF = "PackagingUnitConversion"; //$NON-NLS-1$
	public static String PROP_SOURCE_UNIT = "sourceUnit"; //$NON-NLS-1$
	public static String PROP_TARGET_UNIT = "targetUnit"; //$NON-NLS-1$
	public static String PROP_FACTOR = "factor"; //$NON-NLS-1$
	public static String PROP_ACTIVE = "active"; //$NON-NLS-1$
	public static String PROP_ID = "id"; //$NON-NLS-1$

	// constructors
	public BasePackagingUnitConversion () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BasePackagingUnitConversion (java.lang.Integer id) {
		this.setId(id);
		initialize();
	}

	protected void initialize () {}

	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer id;

	// fields
	protected java.lang.Double factor;
	protected java.lang.Boolean active;

	// many to one
	private com.floreantpos.model.PackagingUnit sourceUnit;
	private com.floreantpos.model.PackagingUnit targetUnit;

	/**
	 * Return the unique identifier of this class
	 * @hibernate.id
	 *  generator-class="identity"
	 *  column="ID"
	 */
	public java.lang.Integer getId () {
		return id;
	}

	/**
	 * Set the unique identifier of this class
	 * @param id the new ID
	 */
	public void setId (java.lang.Integer id) {
		this.id = id;
		this.hashCode = Integer.MIN_VALUE;
	}

	/**
	 * Return the value associated with the column: CONVERSION_FACTOR
	 */
	public java.lang.Double getFactor () {
		return factor == null ? Double.valueOf(0) : factor;
	}

	/**
	 * Set the value related to the column: CONVERSION_FACTOR
	 * @param factor the CONVERSION_FACTOR value
	 */
	public void setFactor (java.lang.Double factor) {
		this.factor = factor;
	}

	/**
	 * Return the value associated with the column: ACTIVE
	 */
	public java.lang.Boolean isActive () {
		return active == null ? Boolean.FALSE : active;
	}

	/**
	 * Set the value related to the column: ACTIVE
	 * @param active the ACTIVE value
	 */
	public void setActive (java.lang.Boolean active) {
		this.active = active;
	}

	/**
	 * Return the value associated with the column: SOURCE_UNIT_ID
	 */
	public com.floreantpos.model.PackagingUnit getSourceUnit () {
		return sourceUnit;
	}

	/**
	 * Set the value related to the column: SOURCE_UNIT_ID
	 * @param sourceUnit the SOURCE_UNIT_ID value
	 */
	public void setSourceUnit (com.floreantpos.model.PackagingUnit sourceUnit) {
		this.sourceUnit = sourceUnit;
	}

	/**
	 * Return the value associated with the column: TARGET_UNIT_ID
	 */
	public com.floreantpos.model.PackagingUnit getTargetUnit () {
		return targetUnit;
	}

	/**
	 * Set the value related to the column: TARGET_UNIT_ID
	 * @param targetUnit the TARGET_UNIT_ID value
	 */
	public void setTargetUnit (com.floreantpos.model.PackagingUnit targetUnit) {
		this.targetUnit = targetUnit;
	}

	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof com.floreantpos.model.PackagingUnitConversion)) return false;
		else {
			com.floreantpos.model.PackagingUnitConversion packagingUnitConversion = (com.floreantpos.model.PackagingUnitConversion) obj;
			if (null == this.getId() || null == packagingUnitConversion.getId()) return false;
			else return (this.getId().equals(packagingUnitConversion.getId()));
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
