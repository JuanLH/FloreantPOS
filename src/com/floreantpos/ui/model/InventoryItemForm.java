package com.floreantpos.ui.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
import com.floreantpos.services.UnitConversionService;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.IntegerTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

public class InventoryItemForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private FixedLengthTextField tfDescription;
	private DoubleTextField tfPackagePurchasePrice;
	private DoubleTextField tfUnitPerPackage;
	private DoubleTextField tfUnitPurchasePrice; // Recipe Unit Cost (read-only)
	private DoubleTextField tfUnitSellingPrice;
	private IntegerTextField tfSortOrder;
	private JComboBox<PackagingUnit> cbPackagingUnit;
	private JComboBox<PackagingUnit> cbRecipeUnit;
	private JComboBox<InventoryGroup> cbItemGroup;
	private JComboBox<InventoryLocation> cbItemLocation;
	private JComboBox<InventoryVendor> cbItemVendor;
	private static final Color READ_ONLY_BG = new Color(240, 240, 240);
	private Color defaultUnitPerPackageBg;
	private JCheckBox chkVisible;

	private boolean isUpdatingView = false;

	public InventoryItemForm() {
		this(new InventoryItem());
	}

	public InventoryItemForm(InventoryItem item) {
		initComponents();
		setBean(item);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,160::]15[grow,fill,260::]")); //$NON-NLS-1$ //$NON-NLS-2$
		setPreferredSize(new Dimension(540, 480));

		tfName = new FixedLengthTextField(120);
		tfName.setColumns(20);

		tfDescription = new FixedLengthTextField(255);
		tfDescription.setColumns(20);

		tfPackagePurchasePrice = new DoubleTextField(10);
		tfUnitPerPackage = new DoubleTextField(10);
		defaultUnitPerPackageBg = tfUnitPerPackage.getBackground();

		tfUnitPurchasePrice = new DoubleTextField(10);
		tfUnitPurchasePrice.setEditable(false);
		tfUnitPurchasePrice.setFocusable(false);
		tfUnitPurchasePrice.setBackground(READ_ONLY_BG);

		tfUnitSellingPrice = new DoubleTextField(10);
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

		// Event listeners for automatic factor resolution & real-time cost calculation
		ItemListener unitChangeListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					onUnitSelectionChanged();
				}
			}
		};
		cbPackagingUnit.addItemListener(unitChangeListener);
		cbRecipeUnit.addItemListener(unitChangeListener);

		DocumentListener calculationDocListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { recalculateRecipeUnitCost(); }
			@Override
			public void removeUpdate(DocumentEvent e) { recalculateRecipeUnitCost(); }
			@Override
			public void changedUpdate(DocumentEvent e) { recalculateRecipeUnitCost(); }
		};
		tfPackagePurchasePrice.getDocument().addDocumentListener(calculationDocListener);
		tfUnitPerPackage.getDocument().addDocumentListener(calculationDocListener);

		add(new JLabel(Messages.getString("InventoryItemForm.name")));                  add(tfName); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.description")));           add(tfDescription); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.packagingUnit")));         add(cbPackagingUnit); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.recipeUnit")));            add(cbRecipeUnit); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.packagePurchasePrice")));  add(tfPackagePurchasePrice); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.unitPerPackage")));        add(tfUnitPerPackage); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.recipeUnitCost")));        add(tfUnitPurchasePrice); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.unitSellingPrice")));      add(tfUnitSellingPrice); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.group")));                 add(cbItemGroup); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.location")));              add(cbItemLocation); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.vendor")));                add(cbItemVendor); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryItemForm.sortOrder")));             add(tfSortOrder); //$NON-NLS-1$
		add(new JLabel());
		add(chkVisible);
	}

	private void setUnitPerPackageEditable(boolean editable) {
		tfUnitPerPackage.setEditable(editable);
		tfUnitPerPackage.setFocusable(editable);
		tfUnitPerPackage.setBackground(editable ? defaultUnitPerPackageBg : READ_ONLY_BG);
	}

	private void updateUnitPerPackageState(PackagingUnit pkg, PackagingUnit rec) {
		if (pkg != null && rec != null) {
			Double factor = UnitConversionService.getInstance().resolveConversionFactor(pkg, rec);
			if (factor != null) {
				tfUnitPerPackage.setText(String.valueOf(factor));
				setUnitPerPackageEditable(false);
				return;
			}
		}
		if (!tfUnitPerPackage.isEditable()) {
			tfUnitPerPackage.setText(""); //$NON-NLS-1$
		}
		setUnitPerPackageEditable(true);
	}

	private void onUnitSelectionChanged() {
		if (isUpdatingView) return;

		PackagingUnit pkg = (PackagingUnit) cbPackagingUnit.getSelectedItem();
		PackagingUnit rec = (PackagingUnit) cbRecipeUnit.getSelectedItem();

		updateUnitPerPackageState(pkg, rec);
		recalculateRecipeUnitCost();
	}

	private void recalculateRecipeUnitCost() {
		if (isUpdatingView) return;

		double pkgPrice = tfPackagePurchasePrice.getDoubleOrZero();
		double factor = tfUnitPerPackage.getDoubleOrZero();

		Double cost = UnitConversionService.getInstance().calculateRecipeUnitCost(pkgPrice, factor);
		tfUnitPurchasePrice.setText(UnitConversionService.getInstance().formatRecipeUnitCost(cost));
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

		isUpdatingView = true;
		try {
			tfName.setText(item.getName() == null ? "" : item.getName()); //$NON-NLS-1$
			tfDescription.setText(item.getDescription() == null ? "" : item.getDescription()); //$NON-NLS-1$

			Double pkgPrice = item.getPackagePurchasePrice();
			tfPackagePurchasePrice.setText(pkgPrice == null || pkgPrice == 0.0 ? "" : String.valueOf(pkgPrice)); //$NON-NLS-1$

			Double factor = item.getUnitPerPackage();
			tfUnitPerPackage.setText(factor == null || factor == 0.0 ? "" : String.valueOf(factor)); //$NON-NLS-1$

			Double recipeCost = item.getUnitPurchasePrice();
			tfUnitPurchasePrice.setText(UnitConversionService.getInstance().formatRecipeUnitCost(recipeCost));

			tfUnitSellingPrice.setText(item.getUnitSellingPrice() == null ? "" : String.valueOf(item.getUnitSellingPrice())); //$NON-NLS-1$
			tfSortOrder.setText(item.getSortOrder() == null ? "0" : String.valueOf(item.getSortOrder())); //$NON-NLS-1$
			chkVisible.setSelected(item.isVisible());

			cbPackagingUnit.setSelectedItem(item.getPackagingUnit());
			cbRecipeUnit.setSelectedItem(item.getRecipeUnit());
			cbItemGroup.setSelectedItem(item.getItemGroup());
			cbItemLocation.setSelectedItem(item.getItemLocation());
			cbItemVendor.setSelectedItem(item.getItemVendor());

			updateUnitPerPackageState(item.getPackagingUnit(), item.getRecipeUnit());
		} finally {
			isUpdatingView = false;
		}
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			MessageDialog.showError(Messages.getString("InventoryItemForm.nameRequired")); //$NON-NLS-1$
			return false;
		}

		PackagingUnit packagingUnit = (PackagingUnit) cbPackagingUnit.getSelectedItem();
		PackagingUnit recipeUnit = (PackagingUnit) cbRecipeUnit.getSelectedItem();
		if (packagingUnit == null || recipeUnit == null) {
			MessageDialog.showError(Messages.getString("InventoryItemForm.unitsRequired")); //$NON-NLS-1$
			return false;
		}

		double pkgPrice = tfPackagePurchasePrice.getDoubleOrZero();
		if (pkgPrice < 0.0) {
			MessageDialog.showError(Messages.getString("InventoryItemForm.negativePriceError")); //$NON-NLS-1$
			return false;
		}

		double factor = tfUnitPerPackage.getDoubleOrZero();
		if (factor <= 0.0) {
			MessageDialog.showError(Messages.getString("InventoryItemForm.conversionFactorRequired")); //$NON-NLS-1$
			return false;
		}

		double recipeCost = UnitConversionService.getInstance().calculateRecipeUnitCost(pkgPrice, factor);

		InventoryItem item = (InventoryItem) getBean();
		item.setName(name);
		item.setDescription(tfDescription.getText());
		item.setVisible(chkVisible.isSelected());
		item.setPackagingUnit(packagingUnit);
		item.setRecipeUnit(recipeUnit);
		item.setItemGroup((InventoryGroup) cbItemGroup.getSelectedItem());
		item.setItemLocation((InventoryLocation) cbItemLocation.getSelectedItem());
		item.setItemVendor((InventoryVendor) cbItemVendor.getSelectedItem());

		item.setPackagePurchasePrice(pkgPrice);
		item.setUnitPerPackage(factor);
		item.setUnitPurchasePrice(recipeCost);
		item.setUnitSellingPrice(tfUnitSellingPrice.getDoubleOrZero());
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
