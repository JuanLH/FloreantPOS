package com.floreantpos.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IngredientValidationTest {

	/**
	 * Helper mimicking the validation logic in IngredientForm.updateModel() (RF-5)
	 */
	public static boolean isValidIngredientName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		if (name.trim().length() > 120) {
			return false;
		}
		return true;
	}

	/**
	 * Helper mimicking duplicate comparison logic (RF-6, RF-7)
	 */
	public static boolean isDuplicate(String newName, Integer newId, Ingredient existing) {
		if (newName == null || existing == null || existing.getName() == null) {
			return false;
		}
		String trimmedNew = newName.trim();
		String trimmedExisting = existing.getName().trim();
		if (trimmedNew.equalsIgnoreCase(trimmedExisting)) {
			// If different IDs or new record, it is duplicate
			if (newId == null || !newId.equals(existing.getId())) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testRejectBlankOrWhitespaceNames() {
		assertFalse("Null name must be rejected", isValidIngredientName(null));
		assertFalse("Empty name must be rejected", isValidIngredientName(""));
		assertFalse("Whitespace name must be rejected", isValidIngredientName("   "));
		assertFalse("Tab and newline name must be rejected", isValidIngredientName("\t \n"));
		assertTrue("Valid name must be accepted", isValidIngredientName("Tomato"));
	}

	@Test
	public void testTrimOuterWhitespace() {
		String rawName = "  Fresh Basil  ";
		String trimmed = rawName.trim();
		assertEquals("Fresh Basil", trimmed);
		assertTrue(isValidIngredientName(rawName));
	}

	@Test
	public void testNameLengthLimit() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 121; i++) {
			sb.append("a");
		}
		assertFalse("Name exceeding 120 chars must be rejected", isValidIngredientName(sb.toString()));

		StringBuilder validSb = new StringBuilder();
		for (int i = 0; i < 120; i++) {
			validSb.append("a");
		}
		assertTrue("Name with 120 chars must be accepted", isValidIngredientName(validSb.toString()));
	}

	@Test
	public void testCaseInsensitiveDuplicateComparison() {
		Ingredient existing = new Ingredient(1);
		existing.setName("Garlic");

		// Different cases
		assertTrue("garlic should collide with Garlic", isDuplicate("garlic", null, existing));
		assertTrue("GARLIC should collide with Garlic", isDuplicate("GARLIC", null, existing));
		assertTrue("  garlic   should collide with Garlic", isDuplicate("  garlic  ", null, existing));
		assertFalse("Onion should not collide with Garlic", isDuplicate("Onion", null, existing));
	}

	@Test
	public void testAllowSameNameWhenEditingSelf() {
		Ingredient existing = new Ingredient(42);
		existing.setName("Oregano");

		// Saving existing entity with same ID and name should NOT be flagged as duplicate (RF-7)
		assertFalse("Editing self with same name should not be duplicate", isDuplicate("Oregano", 42, existing));
		assertFalse("Editing self with different case should not be duplicate", isDuplicate("oregano", 42, existing));

		// Creating a new entity (null id) or another entity (id 99) with same name should be duplicate
		assertTrue("New entity with existing name must be duplicate", isDuplicate("Oregano", null, existing));
		assertTrue("Different entity with existing name must be duplicate", isDuplicate("Oregano", 99, existing));
	}

	@Test
	public void testIngredientI18nConstantsLoaded() {
		assertFalse("INGREDIENTS should be resolved", com.floreantpos.POSConstants.INGREDIENTS.startsWith("!"));
		assertFalse("INGREDIENT should be resolved", com.floreantpos.POSConstants.INGREDIENT.startsWith("!"));
		assertFalse("NEW_INGREDIENT should be resolved", com.floreantpos.POSConstants.NEW_INGREDIENT.startsWith("!"));
		assertFalse("EDIT_INGREDIENT should be resolved", com.floreantpos.POSConstants.EDIT_INGREDIENT.startsWith("!"));
		assertFalse("INGREDIENT_NAME should be resolved", com.floreantpos.POSConstants.INGREDIENT_NAME.startsWith("!"));
		assertFalse("INGREDIENT_NAME_REQUIRED should be resolved", com.floreantpos.POSConstants.INGREDIENT_NAME_REQUIRED.startsWith("!"));
		assertFalse("INGREDIENT_NAME_EXISTS should be resolved", com.floreantpos.POSConstants.INGREDIENT_NAME_EXISTS.startsWith("!"));
		assertFalse("INGREDIENT_ALREADY_ADDED should be resolved", com.floreantpos.POSConstants.INGREDIENT_ALREADY_ADDED.startsWith("!"));
		assertFalse("NO_INGREDIENTS_AVAILABLE should be resolved", com.floreantpos.POSConstants.NO_INGREDIENTS_AVAILABLE.startsWith("!"));
		assertEquals("user can remove the ingredient", com.floreantpos.POSConstants.USER_CAN_REMOVE_INGREDIENT);
		assertEquals("Can be removed", com.floreantpos.POSConstants.CAN_BE_REMOVED);
		assertEquals("added by default", com.floreantpos.POSConstants.IS_ADDED_BY_DEFAULT);
		assertEquals("Added by default", com.floreantpos.POSConstants.ADDED_BY_DEFAULT);
	}
}
