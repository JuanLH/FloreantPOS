package com.floreantpos.bo.ui.explorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.floreantpos.Messages;
import com.floreantpos.POSConstants;
import com.floreantpos.bo.ui.BOMessageDialog;
import com.floreantpos.model.InventoryItem;
import com.floreantpos.model.dao.InventoryItemDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.BeanEditorDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.model.InventoryItemForm;
import com.floreantpos.util.POSUtil;

public class InventoryItemExplorer extends TransparentPanel {

	private JXTable table;
	private InventoryItemTableModel tableModel;
	private InventoryItemDAO dao = new InventoryItemDAO();

	public InventoryItemExplorer() {
		tableModel = new InventoryItemTableModel(dao.findAll());
		table = new JXTable(tableModel);
		table.setDefaultRenderer(Object.class, new PosTableRenderer());

		setLayout(new BorderLayout(5, 5));
		add(new JScrollPane(table), BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 2) doEdit();
			}
		});
	}

	private TransparentPanel createButtonPanel() {
		JButton addButton    = new JButton(Messages.getString("InventoryItemExplorer.add")); //$NON-NLS-1$
		JButton editButton   = new JButton(Messages.getString("InventoryItemExplorer.edit")); //$NON-NLS-1$
		JButton deleteButton = new JButton(Messages.getString("InventoryItemExplorer.delete")); //$NON-NLS-1$

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doAdd(); }
		});
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doEdit(); }
		});
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doDelete(); }
		});

		TransparentPanel panel = new TransparentPanel();
		panel.add(addButton);
		panel.add(editButton);
		panel.add(deleteButton);
		return panel;
	}

	private void doAdd() {
		try {
			InventoryItemForm form = new InventoryItemForm();
			BeanEditorDialog dialog = new BeanEditorDialog(POSUtil.getBackOfficeWindow(), form);
			dialog.open();
			if (!dialog.isCanceled()) {
				tableModel.addRow((InventoryItem) form.getBean());
			}
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	private void doEdit() {
		int index = table.getSelectedRow();
		if (index < 0) return;
		index = table.convertRowIndexToModel(index);
		try {
			InventoryItem item = tableModel.getRow(index);
			InventoryItemForm form = new InventoryItemForm(item);
			BeanEditorDialog dialog = new BeanEditorDialog(POSUtil.getBackOfficeWindow(), form);
			dialog.open();
			if (!dialog.isCanceled()) {
				tableModel.fireTableRowsUpdated(index, index);
			}
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	private void doDelete() {
		int index = table.getSelectedRow();
		if (index < 0) return;
		if (POSMessageDialog.showYesNoQuestionDialog(this, POSConstants.CONFIRM_DELETE, POSConstants.DELETE) != JOptionPane.YES_OPTION) return;
		index = table.convertRowIndexToModel(index);
		try {
			InventoryItem item = tableModel.getRow(index);
			dao.delete(item);
			tableModel.removeRow(index);
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	class InventoryItemTableModel extends AbstractTableModel {
		private String[] columns = {
			Messages.getString("InventoryItemExplorer.col.id"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.name"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.group"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.location"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.vendor"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.packagingUnit"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.purchasePrice"), //$NON-NLS-1$
			Messages.getString("InventoryItemExplorer.col.sellingPrice") //$NON-NLS-1$
		};
		private List<InventoryItem> rows;

		InventoryItemTableModel(List<InventoryItem> rows) { this.rows = rows; }

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			InventoryItem it = rows.get(row);
			switch (col) {
				case 0: return it.getId();
				case 1: return it.getName();
				case 2: return it.getItemGroup() != null ? it.getItemGroup().getName() : ""; //$NON-NLS-1$
				case 3: return it.getItemLocation() != null ? it.getItemLocation().getName() : ""; //$NON-NLS-1$
				case 4: return it.getItemVendor() != null ? it.getItemVendor().getName() : ""; //$NON-NLS-1$
				case 5: return it.getPackagingUnit() != null ? it.getPackagingUnit().getName() : ""; //$NON-NLS-1$
				case 6: return it.getUnitPurchasePrice();
				case 7: return it.getUnitSellingPrice();
			}
			return null;
		}

		public InventoryItem getRow(int index) { return rows.get(index); }
		public void addRow(InventoryItem it) { rows.add(it); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}
}
