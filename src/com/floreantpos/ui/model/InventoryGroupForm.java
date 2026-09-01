package com.floreantpos.ui.model;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.InventoryGroup;
import com.floreantpos.model.dao.InventoryGroupDAO;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

public class InventoryGroupForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private JCheckBox chkVisible;

	public InventoryGroupForm() {
		this(new InventoryGroup());
	}

	public InventoryGroupForm(InventoryGroup group) {
		initComponents();
		setBean(group);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,100::]15[grow,fill,240::]"));
		setPreferredSize(new Dimension(420, 150));

		JLabel lblName = new JLabel(Messages.getString("InventoryGroupForm.name")); //$NON-NLS-1$
		tfName = new FixedLengthTextField(100);
		tfName.setColumns(20);
		chkVisible = new JCheckBox(Messages.getString("InventoryGroupForm.visible")); //$NON-NLS-1$

		add(lblName);
		add(tfName);
		add(new JLabel());
		add(chkVisible);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) return false;
			InventoryGroup group = (InventoryGroup) getBean();
			new InventoryGroupDAO().saveOrUpdate(group);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		InventoryGroup group = (InventoryGroup) getBean();
		if (group == null) return;
		tfName.setText(group.getName());
		chkVisible.setSelected(group.isVisible());
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			MessageDialog.showError(Messages.getString("InventoryGroupForm.nameRequired")); //$NON-NLS-1$
			return false;
		}
		InventoryGroup group = (InventoryGroup) getBean();
		group.setName(name);
		group.setVisible(chkVisible.isSelected());
		return true;
	}

	@Override
	public String getDisplayText() {
		InventoryGroup group = (InventoryGroup) getBean();
		if (group.getId() == null) return Messages.getString("InventoryGroupForm.newTitle"); //$NON-NLS-1$
		return Messages.getString("InventoryGroupForm.editTitle"); //$NON-NLS-1$
	}
}
