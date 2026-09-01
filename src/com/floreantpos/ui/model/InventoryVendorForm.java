package com.floreantpos.ui.model;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.InventoryVendor;
import com.floreantpos.model.dao.InventoryVendorDAO;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

public class InventoryVendorForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private FixedLengthTextField tfAddress;
	private FixedLengthTextField tfCity;
	private FixedLengthTextField tfState;
	private FixedLengthTextField tfZip;
	private FixedLengthTextField tfCountry;
	private FixedLengthTextField tfEmail;
	private FixedLengthTextField tfPhone;
	private JCheckBox chkVisible;

	public InventoryVendorForm() {
		this(new InventoryVendor());
	}

	public InventoryVendorForm(InventoryVendor vendor) {
		initComponents();
		setBean(vendor);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,120::]15[grow,fill,260::]"));
		setPreferredSize(new Dimension(500, 380));

		tfName = new FixedLengthTextField(100);
		tfName.setColumns(20);

		tfAddress = new FixedLengthTextField(200);
		tfAddress.setColumns(20);

		tfCity = new FixedLengthTextField(100);
		tfCity.setColumns(20);

		tfState = new FixedLengthTextField(50);
		tfState.setColumns(20);

		tfZip = new FixedLengthTextField(20);
		tfZip.setColumns(20);

		tfCountry = new FixedLengthTextField(100);
		tfCountry.setColumns(20);

		tfEmail = new FixedLengthTextField(200);
		tfEmail.setColumns(20);

		tfPhone = new FixedLengthTextField(50);
		tfPhone.setColumns(20);

		chkVisible = new JCheckBox(Messages.getString("InventoryVendorForm.visible")); //$NON-NLS-1$

		add(new JLabel(Messages.getString("InventoryVendorForm.name")));    add(tfName); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.address"))); add(tfAddress); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.city")));    add(tfCity); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.state")));   add(tfState); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.zip")));     add(tfZip); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.country"))); add(tfCountry); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.email")));   add(tfEmail); //$NON-NLS-1$
		add(new JLabel(Messages.getString("InventoryVendorForm.phone")));   add(tfPhone); //$NON-NLS-1$
		add(new JLabel());
		add(chkVisible);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) return false;
			InventoryVendor vendor = (InventoryVendor) getBean();
			new InventoryVendorDAO().saveOrUpdate(vendor);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		InventoryVendor v = (InventoryVendor) getBean();
		if (v == null) return;
		tfName.setText(v.getName() == null ? "" : v.getName()); //$NON-NLS-1$
		tfAddress.setText(v.getAddress() == null ? "" : v.getAddress()); //$NON-NLS-1$
		tfCity.setText(v.getCity() == null ? "" : v.getCity()); //$NON-NLS-1$
		tfState.setText(v.getState() == null ? "" : v.getState()); //$NON-NLS-1$
		tfZip.setText(v.getZip() == null ? "" : v.getZip()); //$NON-NLS-1$
		tfCountry.setText(v.getCountry() == null ? "" : v.getCountry()); //$NON-NLS-1$
		tfEmail.setText(v.getEmail() == null ? "" : v.getEmail()); //$NON-NLS-1$
		tfPhone.setText(v.getPhone() == null ? "" : v.getPhone()); //$NON-NLS-1$
		chkVisible.setSelected(v.isVisible());
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			MessageDialog.showError(Messages.getString("InventoryVendorForm.nameRequired")); //$NON-NLS-1$
			return false;
		}
		InventoryVendor v = (InventoryVendor) getBean();
		v.setName(name);
		v.setAddress(tfAddress.getText());
		v.setCity(tfCity.getText());
		v.setState(tfState.getText());
		v.setZip(tfZip.getText());
		v.setCountry(tfCountry.getText());
		v.setEmail(tfEmail.getText());
		v.setPhone(tfPhone.getText());
		v.setVisible(chkVisible.isSelected());
		return true;
	}

	@Override
	public String getDisplayText() {
		InventoryVendor v = (InventoryVendor) getBean();
		if (v.getId() == null) return Messages.getString("InventoryVendorForm.newTitle"); //$NON-NLS-1$
		return Messages.getString("InventoryVendorForm.editTitle"); //$NON-NLS-1$
	}
}
