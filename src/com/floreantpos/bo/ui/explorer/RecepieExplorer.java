package com.floreantpos.bo.ui.explorer;

import java.awt.BorderLayout;
import java.awt.FontMetrics;

import javax.swing.JTabbedPane;

import com.floreantpos.Messages;
import com.floreantpos.swing.TransparentPanel;

/**
 * Multi-tab container for the Recipe (Recepie) maintenance module.
 * Follows the same pattern as {@link PizzaExplorer}.
 *
 * Tabs:
 *   1. Recepie          – linked recipes per menu item + recipe items
 *   2. Inventory Group  – maintenance for inventory groups
 *   3. Inventory Location – maintenance for storage locations
 *   4. Inventory Vendor – maintenance for suppliers/vendors
 *   5. Packaging Unit   – maintenance for measurement units
 *   6. Inventory Item   – maintenance for inventory items (raw ingredients)
 */
public class RecepieExplorer extends TransparentPanel {

	private JTabbedPane mainTab;
	private RecepieExplorerPanel recepieExplorerPanel;

	public RecepieExplorer() {
		initComponents();
	}

	private void initComponents() {
		mainTab = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		setLayout(new BorderLayout());

		mainTab.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
			@Override
			protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 30;
			}

			@Override
			protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
				return 150;
			}
		});

		recepieExplorerPanel = new RecepieExplorerPanel();
		mainTab.addTab(Messages.getString("RecepieExplorer.tab.recepie"),         recepieExplorerPanel); //$NON-NLS-1$
		mainTab.addTab(Messages.getString("RecepieExplorer.tab.inventoryGroup"),   new InventoryGroupExplorer()); //$NON-NLS-1$
		mainTab.addTab(Messages.getString("RecepieExplorer.tab.inventoryLocation"), new InventoryLocationExplorer()); //$NON-NLS-1$
		mainTab.addTab(Messages.getString("RecepieExplorer.tab.inventoryVendor"),  new InventoryVendorExplorer()); //$NON-NLS-1$
		mainTab.addTab(Messages.getString("RecepieExplorer.tab.packagingUnit"),    new PackagingUnitExplorer()); //$NON-NLS-1$
		mainTab.addTab(Messages.getString("RecepieExplorer.tab.inventoryItem"),    new InventoryItemExplorer()); //$NON-NLS-1$

		add(mainTab);
	}

	public void selectOrCreateRecipeFor(com.floreantpos.model.MenuItem menuItem) {
		mainTab.setSelectedIndex(0);
		if (recepieExplorerPanel != null) {
			recepieExplorerPanel.selectOrCreateRecipeFor(menuItem);
		}
	}
}
