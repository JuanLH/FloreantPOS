package com.floreantpos.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class MenuItemIngredientAssociationTest {

	private Ingredient ingGarlic;
	private Ingredient ingOliveOil;
	private Ingredient ingParmesan;
	private MenuItem menuItem;

	@Before
	public void setUp() {
		ingGarlic = new Ingredient(1);
		ingGarlic.setName("Garlic");
		ingGarlic.setDescription("Fresh garlic cloves");

		ingOliveOil = new Ingredient(2);
		ingOliveOil.setName("Extra Virgin Olive Oil");
		ingOliveOil.setDescription("Cold-pressed olive oil");

		ingParmesan = new Ingredient(3);
		ingParmesan.setName("Parmesan");
		ingParmesan.setDescription("Aged parmesan cheese");

		menuItem = new MenuItem(100);
		menuItem.setName("Pasta Aglio e Olio");
		menuItem.setMenuItemIngredients(new ArrayList<MenuItemIngredient>());
	}

	/**
	 * Helper function mimicking MenuItemForm duplicate check logic (RF-14)
	 */
	private boolean tryAddIngredient(MenuItem item, Ingredient ingredient) {
		List<MenuItemIngredient> list = item.getMenuItemIngredients();
		if (list != null) {
			for (MenuItemIngredient existing : list) {
				if (existing.getIngredient() != null &&
					existing.getIngredient().getId() != null &&
					existing.getIngredient().getId().equals(ingredient.getId())) {
					return false; // Duplicate detected (RF-14)
				}
			}
		}
		MenuItemIngredient association = new MenuItemIngredient();
		association.setIngredient(ingredient);
		association.setMenuItem(item);
		item.addTomenuItemIngredients(association);
		return true;
	}

	@Test
	public void testAddUniqueIngredientsToMenuItem() {
		assertTrue("Garlic should be added", tryAddIngredient(menuItem, ingGarlic));
		assertTrue("Olive oil should be added", tryAddIngredient(menuItem, ingOliveOil));

		assertEquals(2, menuItem.getMenuItemIngredients().size());
		assertEquals("Garlic", menuItem.getMenuItemIngredients().get(0).getIngredient().getName());
		assertEquals("Extra Virgin Olive Oil", menuItem.getMenuItemIngredients().get(1).getIngredient().getName());
	}

	@Test
	public void testPreventDuplicateIngredientOnSameMenuItem() {
		assertTrue("First addition of Garlic should succeed", tryAddIngredient(menuItem, ingGarlic));
		assertFalse("Duplicate addition of Garlic must be rejected (RF-14)", tryAddIngredient(menuItem, ingGarlic));

		// Verify count did not increase
		assertEquals(1, menuItem.getMenuItemIngredients().size());
	}

	@Test
	public void testRemoveIngredientFromMenuItem() {
		tryAddIngredient(menuItem, ingGarlic);
		tryAddIngredient(menuItem, ingOliveOil);
		tryAddIngredient(menuItem, ingParmesan);
		assertEquals(3, menuItem.getMenuItemIngredients().size());

		// Remove Olive Oil (RF-15)
		menuItem.getMenuItemIngredients().remove(1);
		assertEquals(2, menuItem.getMenuItemIngredients().size());
		assertEquals("Garlic", menuItem.getMenuItemIngredients().get(0).getIngredient().getName());
		assertEquals("Parmesan", menuItem.getMenuItemIngredients().get(1).getIngredient().getName());
	}

	@Test
	public void testCascadeDisassociationLogic() {
		MenuItem item1 = new MenuItem(101);
		item1.setName("Pizza Margherita");
		item1.setMenuItemIngredients(new ArrayList<MenuItemIngredient>());

		MenuItem item2 = new MenuItem(102);
		item2.setName("Garlic Bread");
		item2.setMenuItemIngredients(new ArrayList<MenuItemIngredient>());

		tryAddIngredient(item1, ingGarlic);
		tryAddIngredient(item1, ingParmesan);

		tryAddIngredient(item2, ingGarlic);
		tryAddIngredient(item2, ingOliveOil);

		assertEquals(2, item1.getMenuItemIngredients().size());
		assertEquals(2, item2.getMenuItemIngredients().size());

		// Simulate deleting Garlic from master catalog (RF-9 cascade behavior)
		Integer deletedIngredientId = ingGarlic.getId();

		List<MenuItem> allMenuItems = new ArrayList<MenuItem>();
		allMenuItems.add(item1);
		allMenuItems.add(item2);

		for (MenuItem mi : allMenuItems) {
			Iterator<MenuItemIngredient> it = mi.getMenuItemIngredients().iterator();
			while (it.hasNext()) {
				MenuItemIngredient mii = it.next();
				if (mii.getIngredient() != null && deletedIngredientId.equals(mii.getIngredient().getId())) {
					it.remove();
				}
			}
		}

		// Verify Garlic is removed from both menu items
		assertEquals(1, item1.getMenuItemIngredients().size());
		assertEquals("Parmesan", item1.getMenuItemIngredients().get(0).getIngredient().getName());

		assertEquals(1, item2.getMenuItemIngredients().size());
		assertEquals("Extra Virgin Olive Oil", item2.getMenuItemIngredients().get(0).getIngredient().getName());
	}

	@Test
	public void testCanBeRemovedDefaultIsFalse() {
		MenuItemIngredient mii = new MenuItemIngredient();
		assertNotNull(mii.isCanBeRemoved());
		assertFalse("Default canBeRemoved must be false", mii.isCanBeRemoved());
		assertFalse("Default getCanBeRemoved must be false", mii.getCanBeRemoved());
	}

	@Test
	public void testCanBeRemovedToggle() {
		MenuItemIngredient mii = new MenuItemIngredient();
		mii.setIngredient(ingGarlic);
		mii.setCanBeRemoved(Boolean.TRUE);
		assertTrue("canBeRemoved should be true after set", mii.isCanBeRemoved());
		assertTrue("getCanBeRemoved should be true after set", mii.getCanBeRemoved());

		mii.setCanBeRemoved(Boolean.FALSE);
		assertFalse("canBeRemoved should be false after reset", mii.isCanBeRemoved());
	}

	@Test
	public void testCanBeRemovedNullSafety() {
		MenuItemIngredient mii = new MenuItemIngredient();
		mii.setCanBeRemoved(null);
		assertNotNull(mii.isCanBeRemoved());
		assertFalse("isCanBeRemoved must guard against null and return false", mii.isCanBeRemoved());
		assertFalse("getCanBeRemoved must guard against null and return false", mii.getCanBeRemoved());
	}

	@Test
	public void testIsAddedByDefaultDefaultIsTrue() {
		MenuItemIngredient mii = new MenuItemIngredient();
		assertNotNull(mii.isIsAddedByDefault());
		assertTrue("Default isAddedByDefault must be true", mii.isIsAddedByDefault());
		assertTrue("Default isAddedByDefault must be true via alias", mii.isAddedByDefault());
		assertTrue("Default getIsAddedByDefault must be true", mii.getIsAddedByDefault());
		assertTrue("Default getAddedByDefault must be true", mii.getAddedByDefault());
	}

	@Test
	public void testIsAddedByDefaultToggle() {
		MenuItemIngredient mii = new MenuItemIngredient();
		mii.setIngredient(ingGarlic);
		mii.setIsAddedByDefault(Boolean.FALSE);
		assertFalse("isAddedByDefault should be false after set", mii.isIsAddedByDefault());
		assertFalse("getIsAddedByDefault should be false after set", mii.getIsAddedByDefault());

		mii.setAddedByDefault(Boolean.TRUE);
		assertTrue("isAddedByDefault should be true after reset", mii.isAddedByDefault());
	}

	@Test
	public void testIsAddedByDefaultNullSafety() {
		MenuItemIngredient mii = new MenuItemIngredient();
		mii.setIsAddedByDefault(null);
		assertNotNull(mii.isIsAddedByDefault());
		assertTrue("isIsAddedByDefault must guard against null and return true", mii.isIsAddedByDefault());
		assertTrue("getIsAddedByDefault must guard against null and return true", mii.getIsAddedByDefault());
	}
}
