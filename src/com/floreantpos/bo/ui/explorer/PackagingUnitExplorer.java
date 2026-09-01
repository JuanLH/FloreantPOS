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
import com.floreantpos.model.PackagingDimension;
import com.floreantpos.model.PackagingUnit;
import com.floreantpos.model.dao.PackagingUnitDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.BeanEditorDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.model.PackagingUnitForm;
import com.floreantpos.util.POSUtil;

public class PackagingUnitExplorer extends TransparentPanel {

	private JXTable table;
	private PackagingUnitTableModel tableModel;
	private PackagingUnitDAO dao = new PackagingUnitDAO();

	private JTextField tfName;
	private JComboBox cbDimension;

	public PackagingUnitExplorer() {
		tableModel = new PackagingUnitTableModel(dao.findAll());
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

		cbDimension = new JComboBox();
		cbDimension.addItem(Messages.getString("Explorer.all")); //$NON-NLS-1$
		for (PackagingDimension dim : PackagingDimension.values()) {
			cbDimension.addItem(dim);
		}

		JButton searchBttn = new JButton(Messages.getString("MenuItemExplorer.3")); //$NON-NLS-1$
		JButton resetBttn = new JButton(Messages.getString("Explorer.reset")); //$NON-NLS-1$

		panel.add(new JLabel(Messages.getString("PackagingUnitExplorer.col.name") + ":")); //$NON-NLS-1$
		panel.add(tfName);
		panel.add(new JLabel(Messages.getString("PackagingUnitExplorer.col.dimension") + ":")); //$NON-NLS-1$
		panel.add(cbDimension);
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
				cbDimension.setSelectedIndex(0);
				searchItem();
			}
		});

		return panel;
	}

	private void searchItem() {
		String text = tfName.getText() == null ? "" : tfName.getText().trim().toLowerCase(); //$NON-NLS-1$
		Object dim = cbDimension.getSelectedItem();

		List<PackagingUnit> all = dao.findAll();
		List<PackagingUnit> filtered = new ArrayList<PackagingUnit>();

		for (PackagingUnit u : all) {
			if (!text.isEmpty()) {
				String name = u.getName() == null ? "" : u.getName().toLowerCase(); //$NON-NLS-1$
				String shortName = u.getShortName() == null ? "" : u.getShortName().toLowerCase(); //$NON-NLS-1$
				if (!name.contains(text) && !shortName.contains(text)) {
					continue;
				}
			}
			if (dim instanceof PackagingDimension) {
				if (u.getPackagingDimension() != dim) {
					continue;
				}
			}
			filtered.add(u);
		}

		tableModel.setRows(filtered);
	}

	private TransparentPanel createButtonPanel() {
		JButton addButton    = new JButton(Messages.getString("PackagingUnitExplorer.add")); //$NON-NLS-1$
		JButton editButton   = new JButton(Messages.getString("PackagingUnitExplorer.edit")); //$NON-NLS-1$
		JButton deleteButton = new JButton(Messages.getString("PackagingUnitExplorer.delete")); //$NON-NLS-1$

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
			PackagingUnitForm form = new PackagingUnitForm();
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
			PackagingUnit unit = tableModel.getRow(index);
			PackagingUnitForm form = new PackagingUnitForm(unit);
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
			PackagingUnit unit = tableModel.getRow(index);
			dao.delete(unit);
			tableModel.removeRow(index);
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	class PackagingUnitTableModel extends AbstractTableModel {
		private String[] columns = {
			Messages.getString("PackagingUnitExplorer.col.id"), //$NON-NLS-1$
			Messages.getString("PackagingUnitExplorer.col.name"), //$NON-NLS-1$
			Messages.getString("PackagingUnitExplorer.col.shortName"), //$NON-NLS-1$
			Messages.getString("PackagingUnitExplorer.col.dimension") //$NON-NLS-1$
		};
		private List<PackagingUnit> rows;

		PackagingUnitTableModel(List<PackagingUnit> rows) { this.rows = rows; }

		void setRows(List<PackagingUnit> rows) {
			this.rows = rows;
			fireTableDataChanged();
		}

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			PackagingUnit u = rows.get(row);
			switch (col) {
				case 0: return u.getId();
				case 1: return u.getName();
				case 2: return u.getShortName();
				case 3: return u.getDimension();
			}
			return null;
		}

		public PackagingUnit getRow(int index) { return rows.get(index); }
		public void addRow(PackagingUnit u) { rows.add(u); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}
}
