package com.floreantpos.ui.model;

import java.awt.Dimension;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.POSConstants;
import com.floreantpos.model.Ingredient;
import com.floreantpos.model.dao.IngredientDAO;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.util.POSUtil;

public class IngredientForm extends BeanEditor {

	private FixedLengthTextField tfName;
	private FixedLengthTextField tfDescription;

	public IngredientForm() {
		this(new Ingredient());
	}

	public IngredientForm(Ingredient ingredient) {
		initComponents();
		setBean(ingredient);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,100::]15[grow,fill,260::]")); //$NON-NLS-1$ //$NON-NLS-2$
		setPreferredSize(new Dimension(450, 160));

		JLabel lblName = new JLabel(POSConstants.INGREDIENT_NAME + ":"); //$NON-NLS-1$
		tfName = new FixedLengthTextField(120);
		tfName.setColumns(20);

		JLabel lblDescription = new JLabel(POSConstants.DESCRIPTION + ":"); //$NON-NLS-1$
		tfDescription = new FixedLengthTextField(255);
		tfDescription.setColumns(20);

		add(lblName);
		add(tfName);
		add(lblDescription);
		add(tfDescription);
	}

	@Override
	public boolean save() {
		try {
			if (!updateModel()) {
				return false;
			}
			Ingredient ingredient = (Ingredient) getBean();
			IngredientDAO.getInstance().saveOrUpdate(ingredient);
		} catch (Exception e) {
			MessageDialog.showError(e);
			return false;
		}
		return true;
	}

	@Override
	protected void updateView() {
		Ingredient ingredient = (Ingredient) getBean();
		if (ingredient == null) {
			return;
		}
		tfName.setText(ingredient.getName());
		tfDescription.setText(ingredient.getDescription());
	}

	@Override
	protected boolean updateModel() {
		String name = tfName.getText();
		if (POSUtil.isBlankOrNull(name)) {
			POSMessageDialog.showError(POSConstants.INGREDIENT_NAME_REQUIRED);
			return false;
		}

		String trimmedName = name.trim();
		Ingredient ingredient = (Ingredient) getBean();

		// Check for case-insensitive duplicate (RF-6, RF-7)
		Ingredient existing = IngredientDAO.getInstance().findByNameIgnoreCase(trimmedName);
		if (existing != null) {
			if (ingredient.getId() == null || !existing.getId().equals(ingredient.getId())) {
				POSMessageDialog.showError(POSConstants.INGREDIENT_NAME_EXISTS);
				return false;
			}
		}

		ingredient.setName(trimmedName);
		String description = tfDescription.getText();
		if (description != null && !description.trim().isEmpty()) {
			ingredient.setDescription(description.trim());
		} else {
			ingredient.setDescription(null);
		}

		return true;
	}

	@Override
	public String getDisplayText() {
		Ingredient ingredient = (Ingredient) getBean();
		if (ingredient == null || ingredient.getId() == null) {
			return POSConstants.NEW_INGREDIENT;
		}
		return POSConstants.EDIT_INGREDIENT;
	}
}
