package com.floreantpos.ui.model;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.POSConstants;
import com.floreantpos.PosRuntimeException;
import com.floreantpos.model.Ingredient;
import com.floreantpos.model.MenuItemIngredient;
import com.floreantpos.model.dao.IngredientDAO;
import com.floreantpos.swing.ListComboBoxModel;
import com.floreantpos.ui.BeanEditor;
import com.floreantpos.ui.dialog.POSMessageDialog;

public class MenuItemIngredientForm extends BeanEditor {

	private JComboBox cbIngredients;
	private JCheckBox chkCanBeRemoved;
	private JCheckBox chkIsAddedByDefault;

	public MenuItemIngredientForm() {
		this(new MenuItemIngredient());
	}

	public MenuItemIngredientForm(MenuItemIngredient menuItemIngredient) {
		initComponents();

		try {
			List<Ingredient> ingredients = IngredientDAO.getInstance().findAll();
			if (ingredients == null || ingredients.isEmpty()) {
				POSMessageDialog.showMessage(this, POSConstants.NO_INGREDIENTS_AVAILABLE);
			}
			cbIngredients.setModel(new ListComboBoxModel(ingredients));
		} catch (Exception e) {
			throw new PosRuntimeException(POSConstants.ERROR_MESSAGE);
		}

		setBean(menuItemIngredient);
	}

	private void initComponents() {
		setLayout(new MigLayout("fillx, insets 15 20 15 20, wrap 2", "[right,100::]15[grow,fill,240::]"));  //$NON-NLS-1$ //$NON-NLS-2$
		setPreferredSize(new Dimension(440, 190));

		JLabel lblIngredient = new JLabel(POSConstants.INGREDIENT + ":"); //$NON-NLS-1$
		cbIngredients = new JComboBox();
		chkCanBeRemoved = new JCheckBox(POSConstants.USER_CAN_REMOVE_INGREDIENT);
		chkIsAddedByDefault = new JCheckBox(POSConstants.IS_ADDED_BY_DEFAULT);

		// When ingredient cannot be removed it is always present by default.
		chkCanBeRemoved.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateIsAddedByDefaultState();
			}
		});

		// Apply initial state (canBeRemoved starts unchecked by default).
		updateIsAddedByDefaultState();

		add(lblIngredient);
		add(cbIngredients);
		add(new JLabel());
		add(chkCanBeRemoved);
		add(new JLabel());
		add(chkIsAddedByDefault);
	}

	/** Keeps chkIsAddedByDefault in sync with chkCanBeRemoved. */
	private void updateIsAddedByDefaultState() {
		if (!chkCanBeRemoved.isSelected()) {
			chkIsAddedByDefault.setSelected(true);
			chkIsAddedByDefault.setEnabled(false);
		} else {
			chkIsAddedByDefault.setEnabled(true);
		}
	}

	@Override
	public boolean save() {
		return updateModel();
	}

	@Override
	protected void updateView() {
		MenuItemIngredient itemIngredient = (MenuItemIngredient) getBean();
		if (itemIngredient == null) {
			return;
		}
		if (itemIngredient.getIngredient() != null) {
			cbIngredients.setSelectedItem(itemIngredient.getIngredient());
		}
		chkCanBeRemoved.setSelected(itemIngredient.isCanBeRemoved() != null ? itemIngredient.isCanBeRemoved() : false);
		chkIsAddedByDefault.setSelected(itemIngredient.isIsAddedByDefault() != null ? itemIngredient.isIsAddedByDefault() : true);
		updateIsAddedByDefaultState();
	}

	@Override
	protected boolean updateModel() {
		Ingredient selected = (Ingredient) cbIngredients.getSelectedItem();
		if (selected == null) {
			POSMessageDialog.showError(this, POSConstants.INGREDIENT_NAME_REQUIRED);
			return false;
		}

		MenuItemIngredient itemIngredient = (MenuItemIngredient) getBean();
		itemIngredient.setIngredient(selected);
		itemIngredient.setCanBeRemoved(chkCanBeRemoved.isSelected());
		itemIngredient.setIsAddedByDefault(chkIsAddedByDefault.isSelected());
		return true;
	}

	@Override
	public String getDisplayText() {
		return POSConstants.INGREDIENT;
	}
}
