package com.floreantpos.ui.model;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.InventoryGroup;
import com.floreantpos.model.InventoryItem;
import com.floreantpos.model.InventoryLocation;
import com.floreantpos.model.InventoryVendor;
import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.dao.InventoryGroupDAO;
import com.floreantpos.model.dao.InventoryItemDAO;
import com.floreantpos.model.dao.InventoryLocationDAO;
import com.floreantpos.model.dao.InventoryVendorDAO;
import com.floreantpos.model.dao.PackagingUnitDAO;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.IntegerTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

public class InventoryItemForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private FixedLengthTextField tfDescription;
	private DoubleTextField tfUnitPurchasePrice;
	private DoubleTextField tfUnitSellingPrice;
	private DoubleTextField tfUnitPerPackage;
	private IntegerTextField tfSortOrder;
	private JComboBox<PackagingUnit> cbPackagingUnit;
	private JComboBox<PackagingUnit> cbRecipeUnit;
	private JComboBox<InventoryGroup> cbItemGroup;
	private JComboBox<InventoryLocation> cbItemLocation;
	private JComboBox<InventoryVendor> cbItemVendor;
	private JCheckBox chkVisible;

	public InventoryItemForm() {
		this(new InventoryItem());
	}

	public InventoryItemForm(InventoryItem item) {
		initComponents();
		setBean(item);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,140::]15[grow,fill,260::]"));
		setPreferredSize(new Dimension(520, 440));

		tfName = new FixedLengthTextField(120);
		tfName.setColumns(20);

		tfDescription = new FixedLengthTextField(255);
		tfDescription.setColumns(20);

		tfUnitPurchasePrice = new DoubleTextField(10);
		tfUnitSellingPrice = new DoubleTextField(10);
		tfUnitPerPackage = new DoubleTextField(10);
		tfSortOrder = new IntegerTextField(10);
		chkVisible = new JCheckBox(Messages.getString("InventoryItemForm.visible")); //$NON-NLS-1$

		// Packaging Unit combo
		List<PackagingUnit> packagingUnits = new PackagingUnitDAO().findAll();
		cbPackagingUnit = new JComboBox<PackagingUnit>();
		cbPackagingUnit.addItem(null);
		for (PackagingUnit p : packagingUnits) {
			cbPackagingUnit.addItem(p);
		}

		// Recipe Unit combo
		cbRecipeUnit = new JComboBox<PackagingUnit>();
		cbRecipeUnit.addItem(null);
		for (PackagingUnit p : packagingUnits) {
			cbRecipeUnit.addItem(p);
		}

		// Inventory Group combo
		List<InventoryGroup> groups = new InventoryGroupDAO().findAll();
		cbItemGroup = new JComboBox<InventoryGroup>();
		cbItemGroup.addItem(null);
		for (InventoryGroup g : groups) {
			cbItemGroup.addItem(g);
		}

		// Inventory Location combo
		List<InventoryLocation> locations = new InventoryLocationDAO().findAll();
		cbItemLocation = new JComboBox<InventoryLocation>();
		cbItemLocation.addItem(null);
		for (InventoryLocation l : locations) {
			cbItemLocation.addItem(l);
		}

		// Inventory Vendor combo
		List<InventoryVendor> vendors = new InventoryVendorDAO().findAll();
		cbItemVendor = new JComboBox<InventoryVendor>();
		cbItemVendor.addItem(null);
		for (InventoryVendor v : vendors) {
			cbItemVendor.addItem(v);
		}

		add(new JLabel(Messages.getString("InventoryItemForm.name")));              add(tfName); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.description")));       add(tfDescription); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.packagingUnit")));     add(cbPackagingUnit); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.recipeUnit")));        add(cbRecipeUnit); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.group")));             add(cbItemGroup); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.location")));          add(cbItemLocation); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.vendor")));            add(cbItemVendor); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.unitPurchasePrice"))); add(tfUnitPurchasePrice); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.unitSellingPrice")));  add(tfUnitSellingPrice); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.unitPerPackage")));    add(tfUnitPerPackage); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.sortOrder")));         add(tfSortOrder); //$NON-NLS-1$
		add(new JLabel());
		add(chkVisible);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) return false;
			InventoryItem item = (InventoryItem) getBean();
			new InventoryItemDAO().saveOrUpdate(item);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		InventoryItem item = (InventoryItem) getBean();
		if (item == null) return;
		tfName.setText(item.getName() == null ? "" : item.getName()); //$NON-NLS-1$
		tfDescription.setText(item.getDescription() == null ? "" : item.getDescription()); //$NON-NLS-1$
		tfUnitPurchasePrice.setText(String.valueOf(item.getUnitPurchasePrice()));
		tfUnitSellingPrice.setText(String.valueOf(item.getUnitSellingPrice()));
		tfUnitPerPackage.setText(String.valueOf(item.getUnitPerPackage()));
		tfSortOrder.setText(String.valueOf(item.getSortOrder()));
		chkVisible.setSelected(item.isVisible());
		cbPackagingUnit.setSelectedItem(item.getPackagingUnit());
		cbRecipeUnit.setSelectedItem(item.getRecipeUnit());
		cbItemGroup.setSelectedItem(item.getItemGroup());
		cbItemLocation.setSelectedItem(item.getItemLocation());
		cbItemVendor.setSelectedItem(item.getItemVendor());
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			MessageDialog.showError(Messages.getString("InventoryItemForm.nameRequired")); //$NON-NLS-1$
			return false;
		}
		InventoryItem item = (InventoryItem) getBean();
		item.setName(name);
		item.setDescription(tfDescription.getText());
		item.setVisible(chkVisible.isSelected());
		item.setPackagingUnit((PackagingUnit) cbPackagingUnit.getSelectedItem());
		item.setRecipeUnit((PackagingUnit) cbRecipeUnit.getSelectedItem());
		item.setItemGroup((InventoryGroup) cbItemGroup.getSelectedItem());
		item.setItemLocation((InventoryLocation) cbItemLocation.getSelectedItem());
		item.setItemVendor((InventoryVendor) cbItemVendor.getSelectedItem());

		item.setUnitPurchasePrice(tfUnitPurchasePrice.getDoubleOrZero());
		item.setUnitSellingPrice(tfUnitSellingPrice.getDoubleOrZero());
		item.setUnitPerPackage(tfUnitPerPackage.getDoubleOrZero());
		item.setSortOrder(tfSortOrder.getInteger());
		return true;
	}

	@Override
	public String getDisplayText() {
		InventoryItem item = (InventoryItem) getBean();
		if (item.getId() == null) return Messages.getString("InventoryItemForm.newTitle"); //$NON-NLS-1$
		return Messages.getString("InventoryItemForm.editTitle"); //$NON-NLS-1$
	}
}
