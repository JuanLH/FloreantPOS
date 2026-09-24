package com.floreantpos.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.floreantpos.model.PackagingUnit;

public class UnitConversionServiceTest {

	private UnitConversionService service;

	@Before
	public void setUp() {
		service = UnitConversionService.getInstance();
	}

	@Test
	public void testCalculateRecipeUnitCostStandard() {
		// e.g. Sack of flour: $50.00 for 50 Pounds -> $1.000000 per pound (RF-11, RF-13)
		Double cost = service.calculateRecipeUnitCost(50.0, 50.0);
		assertEquals(Double.valueOf(1.0), cost);
		assertEquals("1.000000", service.formatRecipeUnitCost(cost));
	}

	@Test
	public void testCalculateRecipeUnitCostHighPrecisionFraction() {
		// e.g. Can of sauce: $4.00 for 1000 Grams -> $0.004000 per gram (BC-1, RF-13)
		Double cost = service.calculateRecipeUnitCost(4.0, 1000.0);
		assertEquals(Double.valueOf(0.004), cost);
		assertEquals("0.004000", service.formatRecipeUnitCost(cost));
	}

	@Test
	public void testCalculateRecipeUnitCostDivisionByZeroProtection() {
		// Factor = 0 or null must return 0.0 without throwing exceptions (BC-3)
		Double costZero = service.calculateRecipeUnitCost(20.0, 0.0);
		assertEquals(Double.valueOf(0.0), costZero);
		assertEquals("0.000000", service.formatRecipeUnitCost(costZero));

		Double costNegative = service.calculateRecipeUnitCost(20.0, -5.0);
		assertEquals(Double.valueOf(0.0), costNegative);

		Double costNullFactor = service.calculateRecipeUnitCost(20.0, null);
		assertEquals(Double.valueOf(0.0), costNullFactor);
	}

	@Test
	public void testCalculateRecipeUnitCostNegativeOrNullPriceProtection() {
		// Negative or null purchase price must return 0.0 (RF-16)
		Double costNegativePrice = service.calculateRecipeUnitCost(-10.0, 5.0);
		assertEquals(Double.valueOf(0.0), costNegativePrice);

		Double costNullPrice = service.calculateRecipeUnitCost(null, 5.0);
		assertEquals(Double.valueOf(0.0), costNullPrice);
	}

	@Test
	public void testIdenticalUnitFactorResolution() {
		// When packaging unit and recipe unit are identical, factor is 1.0 (RF-9, BC-5)
		PackagingUnit u1 = new PackagingUnit(1);
		u1.setName("Unit");

		PackagingUnit u2 = new PackagingUnit(1);
		u2.setName("Unit");

		Double factor = service.resolveConversionFactor(u1, u2);
		assertEquals(Double.valueOf(1.0), factor);

		// Different ID but same name
		PackagingUnit u3 = new PackagingUnit(2);
		u3.setName("unit");
		Double factorSameName = service.resolveConversionFactor(u1, u3);
		assertEquals(Double.valueOf(1.0), factorSameName);

		// Null units
		assertNull(service.resolveConversionFactor(null, u1));
		assertNull(service.resolveConversionFactor(u1, null));
	}

	@Test
	public void testValidateConversionIdenticalUnits() {
		// Identical units rejected in global conversion table (RF-4)
		PackagingUnit u1 = new PackagingUnit(1);
		u1.setName("Box");
		PackagingUnit u2 = new PackagingUnit(1);
		u2.setName("Box");

		String error = service.validateConversion(u1, u2, 24.0, null);
		assertNotNull("Identical units must produce validation error", error);
	}

	@Test
	public void testValidateConversionNonPositiveFactor() {
		// Factor <= 0 rejected (RF-3)
		PackagingUnit u1 = new PackagingUnit(1);
		u1.setName("Box");
		PackagingUnit u2 = new PackagingUnit(2);
		u2.setName("Unit");

		String errorZero = service.validateConversion(u1, u2, 0.0, null);
		assertNotNull("Zero factor must produce validation error", errorZero);

		String errorNegative = service.validateConversion(u1, u2, -1.0, null);
		assertNotNull("Negative factor must produce validation error", errorNegative);

		String errorNull = service.validateConversion(u1, u2, null, null);
		assertNotNull("Null factor must produce validation error", errorNull);
	}

	@Test
	public void testValidateConversionNullUnits() {
		PackagingUnit u = new PackagingUnit(1);
		u.setName("Pound");

		assertNotNull("Null source unit must fail", service.validateConversion(null, u, 10.0, null));
		assertNotNull("Null target unit must fail", service.validateConversion(u, null, 10.0, null));
	}
}
