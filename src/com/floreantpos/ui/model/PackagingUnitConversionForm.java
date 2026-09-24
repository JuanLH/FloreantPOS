package com.floreantpos.ui.model;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.PackagingUnitConversion;
import com.floreantpos.model.dao.PackagingUnitConversionDAO;
import com.floreantpos.model.dao.PackagingUnitDAO;
import com.floreantpos.services.UnitConversionService;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;

public class PackagingUnitConversionForm extends BeanEditor {

	private JComboBox<PackagingUnit> cbSourceUnit;
	private JComboBox<PackagingUnit> cbTargetUnit;
	private DoubleTextField tfFactor;
	private JCheckBox chkActive;

	public PackagingUnitConversionForm() {
		this(new PackagingUnitConversion());
	}

	public PackagingUnitConversionForm(PackagingUnitConversion conversion) {
		initComponents();
		setBean(conversion);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,140::]15[grow,fill,240::]")); //$NON-NLS-1$ //$NON-NLS-2$
		setPreferredSize(new Dimension(480, 220));

		cbSourceUnit = new JComboBox<PackagingUnit>();
		cbTargetUnit = new JComboBox<PackagingUnit>();

		List<PackagingUnit> units = new PackagingUnitDAO().findAll();
		cbSourceUnit.addItem(null);
		cbTargetUnit.addItem(null);
		for (PackagingUnit u : units) {
			cbSourceUnit.addItem(u);
			cbTargetUnit.addItem(u);
		}

		tfFactor = new DoubleTextField(10);
		chkActive = new JCheckBox(Messages.getString("PackagingUnitConversionForm.active")); //$NON-NLS-1$
		chkActive.setSelected(true);

		add(new JLabel(Messages.getString("PackagingUnitConversionForm.sourceUnit"))); add(cbSourceUnit); //$NON-NLS-1$
		add(new JLabel(Messages.getString("PackagingUnitConversionForm.targetUnit"))); add(cbTargetUnit); //$NON-NLS-1$
		add(new JLabel(Messages.getString("PackagingUnitConversionForm.factor")));     add(tfFactor); //$NON-NLS-1$
		add(new JLabel());
		add(chkActive);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) return false;
			PackagingUnitConversion conv = (PackagingUnitConversion) getBean();
			new PackagingUnitConversionDAO().saveOrUpdate(conv);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		PackagingUnitConversion conv = (PackagingUnitConversion) getBean();
		if (conv == null) return;

		cbSourceUnit.setSelectedItem(conv.getSourceUnit());
		cbTargetUnit.setSelectedItem(conv.getTargetUnit());
		tfFactor.setText(conv.getFactor() == null || conv.getFactor() == 0.0 ? "" : String.valueOf(conv.getFactor())); //$NON-NLS-1$
		chkActive.setSelected(conv.getId() == null ? true : conv.isActive());
	}

	@Override
	protected boolean updateModel() {
		PackagingUnit source = (PackagingUnit) cbSourceUnit.getSelectedItem();
		PackagingUnit target = (PackagingUnit) cbTargetUnit.getSelectedItem();
		Double factor = tfFactor.getDoubleOrZero();

		PackagingUnitConversion conv = (PackagingUnitConversion) getBean();
		String validationError = UnitConversionService.getInstance().validateConversion(source, target, factor, conv.getId());
		if (validationError != null) {
			MessageDialog.showError(validationError);
			return false;
		}

		conv.setSourceUnit(source);
		conv.setTargetUnit(target);
		conv.setFactor(factor);
		conv.setActive(chkActive.isSelected());
		return true;
	}

	@Override
	public String getDisplayText() {
		PackagingUnitConversion conv = (PackagingUnitConversion) getBean();
		if (conv == null || conv.getId() == null) {
			return Messages.getString("PackagingUnitConversionForm.newTitle"); //$NON-NLS-1$
		}
		return Messages.getString("PackagingUnitConversionForm.editTitle"); //$NON-NLS-1$
	}
}
