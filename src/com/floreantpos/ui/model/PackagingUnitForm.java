package com.floreantpos.ui.model;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.PackagingDimension;
import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.dao.PackagingUnitDAO;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.util.POSUtil;

public class PackagingUnitForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private FixedLengthTextField tfShortName;
	private DoubleTextField tfFactor;
	private JComboBox<PackagingDimension> cbDimension;
	private JCheckBox chkBaseUnit;

	public PackagingUnitForm() {
		this(new PackagingUnit());
	}

	public PackagingUnitForm(PackagingUnit unit) {
		initComponents();
		setBean(unit);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,120::]15[grow,fill,240::]"));
		setPreferredSize(new Dimension(450, 220));

		tfName = new FixedLengthTextField(100);
		tfName.setColumns(20);

		tfShortName = new FixedLengthTextField(20);
		tfShortName.setColumns(10);

		tfFactor = new DoubleTextField(10);
		cbDimension = new JComboBox<PackagingDimension>(PackagingDimension.values());
		chkBaseUnit = new JCheckBox(Messages.getString("PackagingUnitForm.baseUnit")); //$NON-NLS-1$

		add(new JLabel(Messages.getString("PackagingUnitForm.name")));      add(tfName); //$NON-NLS-1$
		add(new JLabel(Messages.getString("PackagingUnitForm.shortName"))); add(tfShortName); //$NON-NLS-1$
		add(new JLabel(Messages.getString("PackagingUnitForm.factor")));    add(tfFactor); //$NON-NLS-1$
		add(new JLabel(Messages.getString("PackagingUnitForm.dimension"))); add(cbDimension); //$NON-NLS-1$
		add(new JLabel());
		add(chkBaseUnit);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) return false;
			PackagingUnit unit = (PackagingUnit) getBean();
			new PackagingUnitDAO().saveOrUpdate(unit);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		PackagingUnit unit = (PackagingUnit) getBean();
		if (unit == null) return;
		tfName.setText(unit.getName() == null ? "" : unit.getName()); //$NON-NLS-1$
		tfShortName.setText(unit.getShortName() == null ? "" : unit.getShortName()); //$NON-NLS-1$
		tfFactor.setText(unit.getFactor() == null ? "" : String.valueOf(unit.getFactor())); //$NON-NLS-1$
		chkBaseUnit.setSelected(unit.isBaseUnit());
		if (unit.getPackagingDimension() != null) {
			cbDimension.setSelectedItem(unit.getPackagingDimension());
		}
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			MessageDialog.showError(Messages.getString("PackagingUnitForm.nameRequired")); //$NON-NLS-1$
			return false;
		}
		PackagingUnit unit = (PackagingUnit) getBean();
		unit.setName(name);
		unit.setShortName(tfShortName.getText());
		unit.setFactor(tfFactor.getDoubleOrZero());

		PackagingDimension dim = (PackagingDimension) cbDimension.getSelectedItem();
		if (dim != null) {
			unit.setPackagingDimension(dim);
		}
		unit.setBaseUnit(chkBaseUnit.isSelected());
		return true;
	}

	@Override
	public String getDisplayText() {
		PackagingUnit unit = (PackagingUnit) getBean();
		if (unit.getId() == null) return Messages.getString("PackagingUnitForm.newTitle"); //$NON-NLS-1$
		return Messages.getString("PackagingUnitForm.editTitle"); //$NON-NLS-1$
	}
}
