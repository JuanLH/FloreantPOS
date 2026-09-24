package com.floreantpos.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import com.floreantpos.POSConstants;
import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.PackagingUnitConversion;
import com.floreantpos.model.dao.PackagingUnitConversionDAO;

public class UnitConversionService {

	private static final UnitConversionService instance = new UnitConversionService();

	public static UnitConversionService getInstance() {
		return instance;
	}

	public UnitConversionService() {}

	/**
	 * Calculates the recipe unit cost given packaging purchase price and conversion factor (unitPerPackage).
	 * Protects against division by zero and negative prices.
	 * Rounds to 6 decimal places using HALF_UP.
	 */
	public Double calculateRecipeUnitCost(Double packagingPrice, Double unitPerPackage) {
		if (packagingPrice == null || packagingPrice < 0.0) {
			return Double.valueOf(0.0);
		}
		if (unitPerPackage == null || unitPerPackage <= 0.0) {
			return Double.valueOf(0.0);
		}

		double rawCost = packagingPrice / unitPerPackage;
		BigDecimal bd = new BigDecimal(Double.toString(rawCost)).setScale(6, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Formats the recipe unit cost with 6 decimal places (US format).
	 */
	public String formatRecipeUnitCost(Double cost) {
		if (cost == null || cost < 0.0) {
			return "0.000000"; //$NON-NLS-1$
		}
		return String.format(Locale.US, "%.6f", cost); //$NON-NLS-1$
	}

	/**
	 * Resolves the conversion factor between source packaging unit and target recipe unit.
	 * If both units are identical, returns 1.0.
	 * If an active conversion is registered in the database, returns its factor.
	 * Otherwise returns null to allow manual user entry.
	 */
	public Double resolveConversionFactor(PackagingUnit source, PackagingUnit target) {
		if (source == null || target == null) {
			return null;
		}

		if (isIdentical(source, target)) {
			return Double.valueOf(1.0);
		}

		try {
			PackagingUnitConversion conv = PackagingUnitConversionDAO.getInstance().findActiveConversion(source, target);
			if (conv != null && conv.getFactor() != null && conv.getFactor() > 0.0) {
				return conv.getFactor();
			}
		} catch (Exception e) {
			// DAO exception or uninitialized context in unit tests
		}

		return null;
	}

	/**
	 * Validates a global unit conversion entry.
	 * Returns null if valid, or an error message string if invalid.
	 */
	public String validateConversion(PackagingUnit source, PackagingUnit target, Double factor, Integer currentId) {
		if (source == null) {
			return POSConstants.SOURCE_UNIT + " is required."; //$NON-NLS-1$
		}
		if (target == null) {
			return POSConstants.TARGET_UNIT + " is required."; //$NON-NLS-1$
		}
		if (isIdentical(source, target)) {
			return POSConstants.IDENTICAL_CONVERSION_UNITS_ERROR;
		}
		if (factor == null || factor <= 0.0) {
			return POSConstants.CONVERSION_FACTOR_REQUIRED;
		}

		try {
			PackagingUnitConversion existing = PackagingUnitConversionDAO.getInstance().findBySourceAndTarget(source, target);
			if (existing != null) {
				if (currentId == null || !existing.getId().equals(currentId)) {
					return POSConstants.DUPLICATE_CONVERSION_ERROR;
				}
			}
		} catch (Exception e) {
			// DAO exception or uninitialized context in mock tests
		}

		return null;
	}

	private boolean isIdentical(PackagingUnit u1, PackagingUnit u2) {
		if (u1 == u2) return true;
		if (u1.getId() != null && u2.getId() != null && u1.getId().equals(u2.getId())) {
			return true;
		}
		if (u1.getName() != null && u2.getName() != null && u1.getName().trim().equalsIgnoreCase(u2.getName().trim())) {
			return true;
		}
		return false;
	}
}
