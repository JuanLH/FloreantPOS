package com.floreantpos.bo.ui.explorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.floreantpos.POSConstants;
import com.floreantpos.bo.ui.BOMessageDialog;
import com.floreantpos.model.Ingredient;
import com.floreantpos.model.dao.IngredientDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.BeanEditorDialog;
import com.floreantpos.ui.dialog.ConfirmDeleteDialog;
import com.floreantpos.ui.model.IngredientForm;
import com.floreantpos.util.POSUtil;

public class IngredientExplorer extends TransparentPanel {
	private List<Ingredient> ingredientList;
	private JTable table;
	private IngredientExplorerTableModel tableModel;

	public IngredientExplorer() {
		ingredientList = IngredientDAO.getInstance().findAll();

		tableModel = new IngredientExplorerTableModel();
		table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class, new PosTableRenderer());

		setLayout(new BorderLayout(5, 5));
		add(new JScrollPane(table));

		JButton addButton = new JButton(POSConstants.ADD);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IngredientForm editor = new IngredientForm();
					BeanEditorDialog dialog = new BeanEditorDialog(POSUtil.getBackOfficeWindow(), editor);
					dialog.open();
					if (dialog.isCanceled()) {
						return;
					}

					tableModel.addIngredient((Ingredient) editor.getBean());
				} catch (Exception x) {
					BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
				}
			}
		});

		JButton editButton = new JButton(POSConstants.EDIT);
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int index = table.getSelectedRow();
					if (index < 0) {
						return;
					}

					Ingredient ingredient = ingredientList.get(index);
					IngredientForm ingredientForm = new IngredientForm(ingredient);
					BeanEditorDialog dialog = new BeanEditorDialog(POSUtil.getBackOfficeWindow(), ingredientForm);
					dialog.open();
					if (dialog.isCanceled()) {
						return;
					}

					table.repaint();
				} catch (Throwable x) {
					BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
				}
			}
		});

		JButton deleteButton = new JButton(POSConstants.DELETE);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int index = table.getSelectedRow();
					if (index < 0) {
						return;
					}

					if (ConfirmDeleteDialog.showMessage(IngredientExplorer.this, POSConstants.CONFIRM_DELETE, POSConstants.DELETE) == ConfirmDeleteDialog.YES) {
						Ingredient ingredient = ingredientList.get(index);
						IngredientDAO.getInstance().delete(ingredient);
						tableModel.deleteIngredient(ingredient, index);
					}
				} catch (Exception x) {
					BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
				}
			}
		});

		TransparentPanel panel = new TransparentPanel();
		panel.add(addButton);
		panel.add(editButton);
		panel.add(deleteButton);
		add(panel, BorderLayout.SOUTH);
	}

	class IngredientExplorerTableModel extends AbstractTableModel {
		String[] columnNames = { POSConstants.ID, POSConstants.NAME, POSConstants.DESCRIPTION };

		public int getRowCount() {
			if (ingredientList == null) {
				return 0;
			}
			return ingredientList.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (ingredientList == null) {
				return ""; //$NON-NLS-1$
			}

			Ingredient ingredient = ingredientList.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return String.valueOf(ingredient.getId());

				case 1:
					return ingredient.getName();

				case 2:
					return ingredient.getDescription() != null ? ingredient.getDescription() : ""; //$NON-NLS-1$
			}
			return null;
		}

		public void addIngredient(Ingredient ingredient) {
			int size = ingredientList.size();
			ingredientList.add(ingredient);
			fireTableRowsInserted(size, size);
		}

		public void deleteIngredient(Ingredient ingredient, int index) {
			ingredientList.remove(ingredient);
			fireTableRowsDeleted(index, index);
		}
	}
}
