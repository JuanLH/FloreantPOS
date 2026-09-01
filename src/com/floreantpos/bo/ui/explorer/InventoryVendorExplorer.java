package com.floreantpos.bo.ui.explorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXTable;

import com.floreantpos.Messages;
import com.floreantpos.POSConstants;
import com.floreantpos.bo.ui.BOMessageDialog;
import com.floreantpos.model.InventoryVendor;
import com.floreantpos.model.dao.InventoryVendorDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.BeanEditorDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.model.InventoryVendorForm;
import com.floreantpos.util.POSUtil;

public class InventoryVendorExplorer extends TransparentPanel {

	private JXTable table;
	private InventoryVendorTableModel tableModel;
	private InventoryVendorDAO dao = new InventoryVendorDAO();

	private JTextField tfName;
	private JTextField tfCity;

	public InventoryVendorExplorer() {
		tableModel = new InventoryVendorTableModel(dao.findAll());
		table = new JXTable(tableModel);
		table.setDefaultRenderer(Object.class, new PosTableRenderer());

		setLayout(new BorderLayout(5, 5));
		add(buildSearchForm(), BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 2) doEdit();
			}
		});
	}

	private JPanel buildSearchForm() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[][]15[][]15[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		tfName = new JTextField(15);
		tfCity = new JTextField(12);

		JButton searchBttn = new JButton(Messages.getString("MenuItemExplorer.3")); //$NON-NLS-1$
		JButton resetBttn = new JButton(Messages.getString("Explorer.reset")); //$NON-NLS-1$

		panel.add(new JLabel(Messages.getString("InventoryVendorExplorer.col.name") + ":")); //$NON-NLS-1$
		panel.add(tfName);
		panel.add(new JLabel(Messages.getString("InventoryVendorExplorer.col.city") + ":")); //$NON-NLS-1$
		panel.add(tfCity);
		panel.add(searchBttn);
		panel.add(resetBttn);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(loweredetched, Messages.getString("Explorer.searchTitle")); //$NON-NLS-1$
		title.setTitleJustification(TitledBorder.LEFT);
		panel.setBorder(title);

		ActionListener searchListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchItem();
			}
		};

		searchBttn.addActionListener(searchListener);
		tfName.addActionListener(searchListener);
		tfCity.addActionListener(searchListener);

		resetBttn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfName.setText(""); //$NON-NLS-1$
				tfCity.setText(""); //$NON-NLS-1$
				searchItem();
			}
		});

		return panel;
	}

	private void searchItem() {
		String textName = tfName.getText() == null ? "" : tfName.getText().trim().toLowerCase(); //$NON-NLS-1$
		String textCity = tfCity.getText() == null ? "" : tfCity.getText().trim().toLowerCase(); //$NON-NLS-1$

		List<InventoryVendor> all = dao.findAll();
		List<InventoryVendor> filtered = new ArrayList<InventoryVendor>();

		for (InventoryVendor v : all) {
			if (!textName.isEmpty()) {
				String name = v.getName() == null ? "" : v.getName().toLowerCase(); //$NON-NLS-1$
				String phone = v.getPhone() == null ? "" : v.getPhone().toLowerCase(); //$NON-NLS-1$
				String email = v.getEmail() == null ? "" : v.getEmail().toLowerCase(); //$NON-NLS-1$
				if (!name.contains(textName) && !phone.contains(textName) && !email.contains(textName)) {
					continue;
				}
			}
			if (!textCity.isEmpty()) {
				String city = v.getCity() == null ? "" : v.getCity().toLowerCase(); //$NON-NLS-1$
				String state = v.getState() == null ? "" : v.getState().toLowerCase(); //$NON-NLS-1$
				if (!city.contains(textCity) && !state.contains(textCity)) {
					continue;
				}
			}
			filtered.add(v);
		}

		tableModel.setRows(filtered);
	}

	private TransparentPanel createButtonPanel() {
		JButton addButton    = new JButton(Messages.getString("InventoryVendorExplorer.add")); //$NON-NLS-1$
		JButton editButton   = new JButton(Messages.getString("InventoryVendorExplorer.edit")); //$NON-NLS-1$
		JButton deleteButton = new JButton(Messages.getString("InventoryVendorExplorer.delete")); //$NON-NLS-1$

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
			InventoryVendorForm form = new InventoryVendorForm();
			BeanEditorDialog dialog = new BeanEditorDialog(POSUtil.getBackOfficeWindow(), form);
			dialog.open();
			if (!dialog.isCanceled()) {
				searchItem();
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
			InventoryVendor vendor = tableModel.getRow(index);
			InventoryVendorForm form = new InventoryVendorForm(vendor);
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
			InventoryVendor vendor = tableModel.getRow(index);
			dao.delete(vendor);
			tableModel.removeRow(index);
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	class InventoryVendorTableModel extends AbstractTableModel {
		private String[] columns = {
			Messages.getString("InventoryVendorExplorer.col.id"), //$NON-NLS-1$
			Messages.getString("InventoryVendorExplorer.col.name"), //$NON-NLS-1$
			Messages.getString("InventoryVendorExplorer.col.phone"), //$NON-NLS-1$
			Messages.getString("InventoryVendorExplorer.col.email"), //$NON-NLS-1$
			Messages.getString("InventoryVendorExplorer.col.city") //$NON-NLS-1$
		};
		private List<InventoryVendor> rows;

		InventoryVendorTableModel(List<InventoryVendor> rows) { this.rows = rows; }

		void setRows(List<InventoryVendor> rows) {
			this.rows = rows;
			fireTableDataChanged();
		}

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			InventoryVendor v = rows.get(row);
			switch (col) {
				case 0: return v.getId();
				case 1: return v.getName();
				case 2: return v.getPhone();
				case 3: return v.getEmail();
				case 4: return v.getCity();
			}
			return null;
		}

		public InventoryVendor getRow(int index) { return rows.get(index); }
		public void addRow(InventoryVendor v) { rows.add(v); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}
}
