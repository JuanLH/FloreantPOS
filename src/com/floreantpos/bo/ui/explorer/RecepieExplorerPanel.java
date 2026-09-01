package com.floreantpos.bo.ui.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.floreantpos.Messages;
import com.floreantpos.POSConstants;
import com.floreantpos.bo.ui.BOMessageDialog;
import com.floreantpos.model.MenuItem;
import com.floreantpos.model.Recepie;
import com.floreantpos.model.RecepieItem;
import com.floreantpos.model.dao.MenuItemDAO;
import com.floreantpos.model.dao.RecepieDAO;
import com.floreantpos.model.dao.RecepieItemDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.model.RecepieItemEntryDialog;
import com.floreantpos.util.POSUtil;

/**
 * Panel showing two linked tables:
 *  - Top: list of all Recepie (linked to MenuItems), with calculated total cost columns
 *  - Bottom: RecepieItems for the selected Recepie, with calculated per-row price columns
 */
public class RecepieExplorerPanel extends TransparentPanel {

	// --- Top (Recepie) table ---
	private JXTable recepieTable;
	private RecepieTableModel recepieTableModel;

	// --- Bottom (RecepieItem) table ---
	private JXTable itemTable;
	private RecepieItemTableModel itemTableModel;

	private RecepieDAO recepieDAO = RecepieDAO.getInstance();
	private RecepieItemDAO itemDAO = new RecepieItemDAO();

	/** Currently selected recipe (drives bottom table). */
	private Recepie selectedRecepie = null;

	public RecepieExplorerPanel() {
		initComponents();
		loadRecepies();
	}

	/**
	 * Selects the Recepie for the given MenuItem if it exists, or creates a new one and selects it.
	 * Also loads the recipe items into the bottom table.
	 */
	public void selectOrCreateRecipeFor(MenuItem menuItem) {
		if (menuItem == null) return;

		loadRecepies();

		int targetModelIndex = -1;
		for (int i = 0; i < recepieTableModel.getRowCount(); i++) {
			Recepie r = recepieTableModel.getRow(i);
			if (r.getMenuItem() != null && r.getMenuItem().getId() != null && r.getMenuItem().getId().equals(menuItem.getId())) {
				targetModelIndex = i;
				break;
			}
		}

		if (targetModelIndex == -1) {
			Recepie newRecepie = new Recepie();
			newRecepie.setMenuItem(menuItem);
			recepieDAO.saveOrUpdate(newRecepie);
			recepieTableModel.addRow(newRecepie);
			targetModelIndex = recepieTableModel.getRowCount() - 1;
		}

		if (targetModelIndex >= 0 && targetModelIndex < recepieTableModel.getRowCount()) {
			int viewIndex = recepieTable.convertRowIndexToView(targetModelIndex);
			recepieTable.setRowSelectionInterval(viewIndex, viewIndex);
			recepieTable.scrollRowToVisible(viewIndex);
			selectedRecepie = recepieTableModel.getRow(targetModelIndex);
			loadRecepieItems();
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout(5, 5));

		// ---- Top panel (Recepie list) ----
		recepieTableModel = new RecepieTableModel(new ArrayList<Recepie>());
		recepieTable = new JXTable(recepieTableModel);
		recepieTable.setDefaultRenderer(Object.class, new PosTableRenderer());
		recepieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		recepieTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int idx = recepieTable.getSelectedRow();
					if (idx >= 0) {
						idx = recepieTable.convertRowIndexToModel(idx);
						selectedRecepie = recepieTableModel.getRow(idx);
						loadRecepieItems();
					}
				}
			}
		});

		JPanel topPanel = new JPanel(new BorderLayout(2, 2));
		topPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),
			Messages.getString("RecepieExplorerPanel.recepieTable.title"), //$NON-NLS-1$
			TitledBorder.LEFT, TitledBorder.TOP));
		topPanel.add(new JScrollPane(recepieTable), BorderLayout.CENTER);
		topPanel.add(createRecepieButtonPanel(), BorderLayout.SOUTH);

		// ---- Bottom panel (RecepieItem list) ----
		itemTableModel = new RecepieItemTableModel(new ArrayList<RecepieItem>());
		itemTable = new JXTable(itemTableModel);
		itemTable.setDefaultRenderer(Object.class, new PosTableRenderer());
		itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel bottomPanel = new JPanel(new BorderLayout(2, 2));
		bottomPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),
			Messages.getString("RecepieExplorerPanel.recepieItemTable.title"), //$NON-NLS-1$
			TitledBorder.LEFT, TitledBorder.TOP));
		bottomPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
		bottomPanel.add(createItemButtonPanel(), BorderLayout.SOUTH);

		// ---- Split pane ----
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
		splitPane.setResizeWeight(0.45);
		splitPane.setDividerSize(6);

		add(splitPane, BorderLayout.CENTER);
	}

	// ---- Recepie (top) button panel ----
	private JPanel createRecepieButtonPanel() {
		JButton btnAddRecepie    = new JButton(Messages.getString("RecepieExplorerPanel.btn.addRecepie")); //$NON-NLS-1$
		JButton btnDeleteRecepie = new JButton(Messages.getString("RecepieExplorerPanel.btn.deleteRecepie")); //$NON-NLS-1$

		btnAddRecepie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doAddRecepie(); }
		});
		btnDeleteRecepie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doDeleteRecepie(); }
		});

		TransparentPanel panel = new TransparentPanel();
		panel.add(btnAddRecepie);
		panel.add(btnDeleteRecepie);
		return panel;
	}

	// ---- RecepieItem (bottom) button panel ----
	private JPanel createItemButtonPanel() {
		JButton btnAddItem    = new JButton(Messages.getString("RecepieExplorerPanel.btn.addItem")); //$NON-NLS-1$
		JButton btnEditItem   = new JButton(Messages.getString("RecepieExplorerPanel.btn.editItem")); //$NON-NLS-1$
		JButton btnDeleteItem = new JButton(Messages.getString("RecepieExplorerPanel.btn.deleteItem")); //$NON-NLS-1$

		btnAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doAddItem(); }
		});
		btnEditItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doEditItem(); }
		});
		btnDeleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { doDeleteItem(); }
		});

		TransparentPanel panel = new TransparentPanel();
		panel.add(btnAddItem);
		panel.add(btnEditItem);
		panel.add(btnDeleteItem);
		return panel;
	}

	// ---- Data loading ----
	private void loadRecepies() {
		List<Recepie> recepies = recepieDAO.findAll();
		recepieTableModel.setRows(recepies);
		itemTableModel.setRows(new ArrayList<RecepieItem>());
		selectedRecepie = null;
	}

	private void loadRecepieItems() {
		if (selectedRecepie == null) {
			itemTableModel.setRows(new ArrayList<RecepieItem>());
			return;
		}
		List<RecepieItem> items = selectedRecepie.getRecepieItems();
		itemTableModel.setRows(items == null ? new ArrayList<RecepieItem>() : new ArrayList<RecepieItem>(items));
	}

	/** Refreshes only the selected row in the recepie table (cost totals). */
	private void refreshSelectedRecepieRow() {
		int idx = recepieTable.getSelectedRow();
		if (idx >= 0) {
			int modelIdx = recepieTable.convertRowIndexToModel(idx);
			recepieTableModel.fireTableRowsUpdated(modelIdx, modelIdx);
		}
	}

	// ---- Recepie CRUD ----
	private void doAddRecepie() {
		try {
			// Let the user pick a MenuItem that doesn't yet have a recipe
			List<MenuItem> allItems = MenuItemDAO.getInstance().getMenuItems();
			List<Recepie> existing = recepieDAO.findAll();
			List<MenuItem> used = new ArrayList<MenuItem>();
			for (Recepie r : existing) {
				if (r.getMenuItem() != null) used.add(r.getMenuItem());
			}
			allItems.removeAll(used);

			if (allItems.isEmpty()) {
				POSMessageDialog.showMessage(this, Messages.getString("RecepieExplorerPanel.noAvailableMenuItems")); //$NON-NLS-1$
				return;
			}

			JComboBox<MenuItem> cbMenuItems = new JComboBox<MenuItem>(allItems.toArray(new MenuItem[0]));
			int result = JOptionPane.showConfirmDialog(
				POSUtil.getBackOfficeWindow(),
				new Object[]{ new JLabel(Messages.getString("RecepieExplorerPanel.selectMenuItem")), cbMenuItems }, //$NON-NLS-1$
				Messages.getString("RecepieExplorerPanel.btn.addRecepie"), //$NON-NLS-1$
				JOptionPane.OK_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION) return;

			MenuItem selected = (MenuItem) cbMenuItems.getSelectedItem();
			if (selected == null) return;

			Recepie newRecepie = new Recepie();
			newRecepie.setMenuItem(selected);
			recepieDAO.saveOrUpdate(newRecepie);
			recepieTableModel.addRow(newRecepie);

		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	private void doDeleteRecepie() {
		int index = recepieTable.getSelectedRow();
		if (index < 0) return;
		if (POSMessageDialog.showYesNoQuestionDialog(this, POSConstants.CONFIRM_DELETE, POSConstants.DELETE) != JOptionPane.YES_OPTION) return;
		index = recepieTable.convertRowIndexToModel(index);
		try {
			Recepie r = recepieTableModel.getRow(index);
			recepieDAO.delete(r);
			recepieTableModel.removeRow(index);
			selectedRecepie = null;
			itemTableModel.setRows(new ArrayList<RecepieItem>());
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	// ---- RecepieItem CRUD ----
	private void doAddItem() {
		if (selectedRecepie == null) {
			POSMessageDialog.showMessage(this, Messages.getString("RecepieExplorerPanel.selectRecepie")); //$NON-NLS-1$
			return;
		}
		try {
			RecepieItemEntryDialog dialog = new RecepieItemEntryDialog(POSUtil.getBackOfficeWindow(), selectedRecepie);
			dialog.setVisible(true);
			if (!dialog.isCanceled()) {
				RecepieItem newItem = dialog.getRecepieItem();
				itemDAO.saveOrUpdate(newItem);
				selectedRecepie.addRecepieItem(newItem);
				itemTableModel.addRow(newItem);
				refreshSelectedRecepieRow();
			}
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	private void doEditItem() {
		int index = itemTable.getSelectedRow();
		if (index < 0) {
			POSMessageDialog.showMessage(this, Messages.getString("RecepieExplorerPanel.selectItem")); //$NON-NLS-1$
			return;
		}
		index = itemTable.convertRowIndexToModel(index);
		try {
			RecepieItem item = itemTableModel.getRow(index);
			RecepieItemEntryDialog dialog = new RecepieItemEntryDialog(POSUtil.getBackOfficeWindow(), selectedRecepie, item);
			dialog.setVisible(true);
			if (!dialog.isCanceled()) {
				itemDAO.saveOrUpdate(item);
				itemTableModel.fireTableRowsUpdated(index, index);
				refreshSelectedRecepieRow();
			}
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	private void doDeleteItem() {
		int index = itemTable.getSelectedRow();
		if (index < 0) {
			POSMessageDialog.showMessage(this, Messages.getString("RecepieExplorerPanel.selectItem")); //$NON-NLS-1$
			return;
		}
		if (POSMessageDialog.showYesNoQuestionDialog(this, POSConstants.CONFIRM_DELETE, POSConstants.DELETE) != JOptionPane.YES_OPTION) return;
		index = itemTable.convertRowIndexToModel(index);
		try {
			RecepieItem item = itemTableModel.getRow(index);
			itemDAO.delete(item);
			if (selectedRecepie.getRecepieItems() != null) {
				selectedRecepie.getRecepieItems().remove(item);
			}
			itemTableModel.removeRow(index);
			refreshSelectedRecepieRow();
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	// ==============================================================
	// Inner Table Models
	// ==============================================================

	/** Table model for the top (Recepie) table. Shows MenuItem name and calculated cost totals. */
	class RecepieTableModel extends AbstractTableModel {
		private final String[] columns = {
			Messages.getString("RecepieExplorerPanel.col.id"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.menuItem"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.totalPurchaseCost"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.totalSellingCost") //$NON-NLS-1$
		};
		private List<Recepie> rows;

		RecepieTableModel(List<Recepie> rows) { this.rows = rows; }

		void setRows(List<Recepie> rows) {
			this.rows = rows;
			fireTableDataChanged();
		}

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			Recepie r = rows.get(row);
			switch (col) {
				case 0: return r.getId();
				case 1: return r.getMenuItem() != null ? r.getMenuItem().getName() : ""; //$NON-NLS-1$
				case 2: return String.format("%.4f", r.getTotalPurchaseCost()); //$NON-NLS-1$
				case 3: return String.format("%.4f", r.getTotalSellingCost()); //$NON-NLS-1$
			}
			return null;
		}

		public Recepie getRow(int index) { return rows.get(index); }
		public void addRow(Recepie r) { rows.add(r); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}

	/** Table model for the bottom (RecepieItem) table. Shows inventory item details and calculated prices. */
	class RecepieItemTableModel extends AbstractTableModel {
		private final String[] columns = {
			Messages.getString("RecepieExplorerPanel.col.id"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.inventoryItem"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.packagingUnit"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.quantity"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.purchasePrice"), //$NON-NLS-1$
			Messages.getString("RecepieExplorerPanel.col.sellingPrice") //$NON-NLS-1$
		};
		private List<RecepieItem> rows;

		RecepieItemTableModel(List<RecepieItem> rows) { this.rows = rows; }

		void setRows(List<RecepieItem> rows) {
			this.rows = rows;
			fireTableDataChanged();
		}

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			RecepieItem item = rows.get(row);
			switch (col) {
				case 0: return item.getId();
				case 1: return item.getInventoryItem() != null ? item.getInventoryItem().getName() : ""; //$NON-NLS-1$
				case 2: return (item.getInventoryItem() != null && item.getInventoryItem().getPackagingUnit() != null)
					? item.getInventoryItem().getPackagingUnit().getName() : ""; //$NON-NLS-1$
				case 3: return item.getQuantity();
				case 4: return item.getInventoryItem() != null
					? String.format("%.4f", item.getQuantity() * item.getInventoryItem().getUnitPurchasePrice()) : ""; //$NON-NLS-1$
				case 5: return item.getInventoryItem() != null
					? String.format("%.4f", item.getQuantity() * item.getInventoryItem().getUnitSellingPrice()) : ""; //$NON-NLS-1$
			}
			return null;
		}

		public RecepieItem getRow(int index) { return rows.get(index); }
		public void addRow(RecepieItem it) { rows.add(it); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}
}
