package com.floreantpos.model.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.PackagingUnitConversion;

public class PackagingUnitConversionTest {

	@Test
	public void testConversionEntityProperties() {
		PackagingUnit sack = new PackagingUnit(1);
		sack.setName("Sack");

		PackagingUnit pound = new PackagingUnit(2);
		pound.setName("Pound");

		PackagingUnitConversion conv = new PackagingUnitConversion();
		conv.setId(10);
		conv.setSourceUnit(sack);
		conv.setTargetUnit(pound);
		conv.setFactor(50.0);
		conv.setActive(true);

		assertEquals(Integer.valueOf(10), conv.getId());
		assertEquals("Sack", conv.getSourceUnit().getName());
		assertEquals("Pound", conv.getTargetUnit().getName());
		assertEquals(Double.valueOf(50.0), conv.getFactor());
		assertTrue(conv.isActive());
		assertEquals("Sack -> Pound (50.0)", conv.toString());
	}

	@Test
	public void testEqualityAndHashCode() {
		PackagingUnitConversion c1 = new PackagingUnitConversion(1);
		PackagingUnitConversion c2 = new PackagingUnitConversion(1);
		PackagingUnitConversion c3 = new PackagingUnitConversion(2);

		assertEquals(c1, c2);
		assertFalse(c1.equals(c3));
		assertEquals(c1.hashCode(), c2.hashCode());
	}

	@Test
	public void testActiveToggle() {
		PackagingUnitConversion conv = new PackagingUnitConversion();
		conv.setActive(false);
		assertFalse(conv.isActive());

		conv.setActive(true);
		assertTrue(conv.isActive());
	}
}
