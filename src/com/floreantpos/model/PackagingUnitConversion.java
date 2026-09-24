package com.floreantpos.model;

import com.floreantpos.model.base.BasePackagingUnitConversion;

public class PackagingUnitConversion extends BasePackagingUnitConversion {
	private static final long serialVersionUID = 1L;

	public PackagingUnitConversion () {
		super();
	}

	public PackagingUnitConversion (java.lang.Integer id) {
		super(id);
	}

	@Override
	public String toString() {
		String src = getSourceUnit() == null ? "" : getSourceUnit().getName(); //$NON-NLS-1$
		String tgt = getTargetUnit() == null ? "" : getTargetUnit().getName(); //$NON-NLS-1$
		return src + " -> " + tgt + " (" + getFactor() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
