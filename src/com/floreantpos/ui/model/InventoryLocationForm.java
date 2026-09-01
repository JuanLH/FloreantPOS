package com.floreantpos.ui.model;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.InventoryLocation;
import com.floreantpos.model.dao.InventoryLocationDAO;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

public class InventoryLocationForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private JCheckBox chkVisible;

	public InventoryLocationForm() {
		this(new InventoryLocation());
	}

	public InventoryLocationForm(InventoryLocation location) {
		initComponents();
		setBean(location);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,100::]15[grow,fill,240::]"));
		setPreferredSize(new Dimension(420, 150));

		JLabel lblName = new JLabel(Messages.getString("InventoryLocationForm.name")); //$NON-NLS-1$
		tfName = new FixedLengthTextField(100);
		tfName.setColumns(20);
		chkVisible = new JCheckBox(Messages.getString("InventoryLocationForm.visible")); //$NON-NLS-1$

		add(lblName);
		add(tfName);
		add(new JLabel());
		add(chkVisible);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) return false;
			InventoryLocation location = (InventoryLocation) getBean();
			new InventoryLocationDAO().saveOrUpdate(location);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		InventoryLocation location = (InventoryLocation) getBean();
		if (location == null) return;
		tfName.setText(location.getName());
		chkVisible.setSelected(location.isVisible());
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			MessageDialog.showError(Messages.getString("InventoryLocationForm.nameRequired")); //$NON-NLS-1$
			return false;
		}
		InventoryLocation location = (InventoryLocation) getBean();
		location.setName(name);
		location.setVisible(chkVisible.isSelected());
		return true;
	}

	@Override
	public String getDisplayText() {
		InventoryLocation location = (InventoryLocation) getBean();
		if (location.getId() == null) return Messages.getString("InventoryLocationForm.newTitle"); //$NON-NLS-1$
		return Messages.getString("InventoryLocationForm.editTitle"); //$NON-NLS-1$
	}
}
