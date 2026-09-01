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
import com.floreantpos.model.InventoryGroup;
import com.floreantpos.model.dao.InventoryGroupDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.BeanEditorDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.model.InventoryGroupForm;
import com.floreantpos.util.POSUtil;

public class InventoryGroupExplorer extends TransparentPanel {

	private JXTable table;
	private InventoryGroupTableModel tableModel;
	private InventoryGroupDAO dao = new InventoryGroupDAO();

	public InventoryGroupExplorer() {
		tableModel = new InventoryGroupTableModel(dao.findAll());
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
		JButton addButton    = new JButton(Messages.getString("InventoryGroupExplorer.add")); //$NON-NLS-1$
		JButton editButton   = new JButton(Messages.getString("InventoryGroupExplorer.edit")); //$NON-NLS-1$
		JButton deleteButton = new JButton(Messages.getString("InventoryGroupExplorer.delete")); //$NON-NLS-1$

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
			InventoryGroupForm form = new InventoryGroupForm();
			BeanEditorDialog dialog = new BeanEditorDialog(POSUtil.getBackOfficeWindow(), form);
			dialog.open();
			if (!dialog.isCanceled()) {
				tableModel.addRow((InventoryGroup) form.getBean());
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
			InventoryGroup group = tableModel.getRow(index);
			InventoryGroupForm form = new InventoryGroupForm(group);
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
			InventoryGroup group = tableModel.getRow(index);
			dao.delete(group);
			tableModel.removeRow(index);
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	// ------- Inner Table Model -------
	class InventoryGroupTableModel extends AbstractTableModel {
		private String[] columns = {
			Messages.getString("InventoryGroupExplorer.col.id"), //$NON-NLS-1$
			Messages.getString("InventoryGroupExplorer.col.name"), //$NON-NLS-1$
			Messages.getString("InventoryGroupExplorer.col.visible") //$NON-NLS-1$
		};
		private List<InventoryGroup> rows;

		InventoryGroupTableModel(List<InventoryGroup> rows) { this.rows = rows; }

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			InventoryGroup g = rows.get(row);
			switch (col) {
				case 0: return g.getId();
				case 1: return g.getName();
				case 2: return g.isVisible();
			}
			return null;
		}

		public InventoryGroup getRow(int index) { return rows.get(index); }
		public void addRow(InventoryGroup g) { rows.add(g); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}
}
