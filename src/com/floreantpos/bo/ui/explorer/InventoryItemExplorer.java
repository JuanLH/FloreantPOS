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
import javax.swing.JComboBox;
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
import com.floreantpos.model.InventoryGroup;
import com.floreantpos.model.InventoryItem;
import com.floreantpos.model.InventoryLocation;
import com.floreantpos.model.InventoryVendor;
import com.floreantpos.model.dao.InventoryGroupDAO;
import com.floreantpos.model.dao.InventoryItemDAO;
import com.floreantpos.model.dao.InventoryLocationDAO;
import com.floreantpos.model.dao.InventoryVendorDAO;
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

	private JTextField tfName;
	private JComboBox cbGroup;
	private JComboBox cbLocation;
	private JComboBox cbVendor;

	public InventoryItemExplorer() {
		tableModel = new InventoryItemTableModel(dao.findAll());
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
		panel.setLayout(new MigLayout("", "[][]15[][]15[][]15[][]15[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		tfName = new JTextField(12);

		cbGroup = new JComboBox();
		cbGroup.addItem(Messages.getString("Explorer.all")); //$NON-NLS-1$
		try {
			List<InventoryGroup> groups = new InventoryGroupDAO().findAll();
			for (InventoryGroup g : groups) cbGroup.addItem(g);
		} catch (Exception ignored) {}

		cbLocation = new JComboBox();
		cbLocation.addItem(Messages.getString("Explorer.all")); //$NON-NLS-1$
		try {
			List<InventoryLocation> locations = new InventoryLocationDAO().findAll();
			for (InventoryLocation l : locations) cbLocation.addItem(l);
		} catch (Exception ignored) {}

		cbVendor = new JComboBox();
		cbVendor.addItem(Messages.getString("Explorer.all")); //$NON-NLS-1$
		try {
			List<InventoryVendor> vendors = new InventoryVendorDAO().findAll();
			for (InventoryVendor v : vendors) cbVendor.addItem(v);
		} catch (Exception ignored) {}

		JButton searchBttn = new JButton(Messages.getString("MenuItemExplorer.3")); //$NON-NLS-1$
		JButton resetBttn = new JButton(Messages.getString("Explorer.reset")); //$NON-NLS-1$

		panel.add(new JLabel(Messages.getString("InventoryItemExplorer.col.name") + ":")); //$NON-NLS-1$
		panel.add(tfName);
		panel.add(new JLabel(Messages.getString("InventoryItemExplorer.col.group") + ":")); //$NON-NLS-1$
		panel.add(cbGroup);
		panel.add(new JLabel(Messages.getString("InventoryItemExplorer.col.location") + ":")); //$NON-NLS-1$
		panel.add(cbLocation);
		panel.add(new JLabel(Messages.getString("InventoryItemExplorer.col.vendor") + ":")); //$NON-NLS-1$
		panel.add(cbVendor);
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

		resetBttn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfName.setText(""); //$NON-NLS-1$
				cbGroup.setSelectedIndex(0);
				cbLocation.setSelectedIndex(0);
				cbVendor.setSelectedIndex(0);
				searchItem();
			}
		});

		return panel;
	}

	private void searchItem() {
		String text = tfName.getText() == null ? "" : tfName.getText().trim().toLowerCase(); //$NON-NLS-1$
		Object group = cbGroup.getSelectedItem();
		Object location = cbLocation.getSelectedItem();
		Object vendor = cbVendor.getSelectedItem();

		List<InventoryItem> all = dao.findAll();
		List<InventoryItem> filtered = new ArrayList<InventoryItem>();

		for (InventoryItem it : all) {
			if (!text.isEmpty()) {
				String name = it.getName() == null ? "" : it.getName().toLowerCase(); //$NON-NLS-1$
				String desc = it.getDescription() == null ? "" : it.getDescription().toLowerCase(); //$NON-NLS-1$
				if (!name.contains(text) && !desc.contains(text)) {
					continue;
				}
			}
			if (group instanceof InventoryGroup) {
				if (it.getItemGroup() == null || !it.getItemGroup().getId().equals(((InventoryGroup) group).getId())) {
					continue;
				}
			}
			if (location instanceof InventoryLocation) {
				if (it.getItemLocation() == null || !it.getItemLocation().getId().equals(((InventoryLocation) location).getId())) {
					continue;
				}
			}
			if (vendor instanceof InventoryVendor) {
				if (it.getItemVendor() == null || !it.getItemVendor().getId().equals(((InventoryVendor) vendor).getId())) {
					continue;
				}
			}
			filtered.add(it);
		}

		tableModel.setRows(filtered);
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

		void setRows(List<InventoryItem> rows) {
			this.rows = rows;
			fireTableDataChanged();
		}

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
