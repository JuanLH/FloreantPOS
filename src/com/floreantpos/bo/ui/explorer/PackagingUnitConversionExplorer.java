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
import javax.swing.JCheckBox;
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
import com.floreantpos.model.PackagingUnitConversion;
import com.floreantpos.model.dao.PackagingUnitConversionDAO;
import com.floreantpos.swing.TransparentPanel;
import com.floreantpos.ui.PosTableRenderer;
import com.floreantpos.ui.dialog.BeanEditorDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.model.PackagingUnitConversionForm;
import com.floreantpos.util.POSUtil;

public class PackagingUnitConversionExplorer extends TransparentPanel {

	private JXTable table;
	private PackagingUnitConversionTableModel tableModel;
	private PackagingUnitConversionDAO dao = new PackagingUnitConversionDAO();

	private JTextField tfSearch;
	private JCheckBox chkActiveOnly;

	public PackagingUnitConversionExplorer() {
		tableModel = new PackagingUnitConversionTableModel(dao.findAll());
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
		panel.setLayout(new MigLayout("", "[][]15[]15[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		tfSearch = new JTextField(15);
		chkActiveOnly = new JCheckBox(Messages.getString("PackagingUnitConversionExplorer.activeOnly")); //$NON-NLS-1$

		JButton searchBttn = new JButton(Messages.getString("MenuItemExplorer.3")); //$NON-NLS-1$
		JButton resetBttn = new JButton(Messages.getString("Explorer.reset")); //$NON-NLS-1$

		panel.add(new JLabel(Messages.getString("Explorer.searchTitle") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
		panel.add(tfSearch);
		panel.add(chkActiveOnly);
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
		tfSearch.addActionListener(searchListener);
		chkActiveOnly.addActionListener(searchListener);

		resetBttn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfSearch.setText(""); //$NON-NLS-1$
				chkActiveOnly.setSelected(false);
				searchItem();
			}
		});

		return panel;
	}

	private void searchItem() {
		String text = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase(); //$NON-NLS-1$
		boolean activeOnly = chkActiveOnly.isSelected();

		List<PackagingUnitConversion> all = dao.findAll();
		List<PackagingUnitConversion> filtered = new ArrayList<PackagingUnitConversion>();

		for (PackagingUnitConversion c : all) {
			if (activeOnly && !c.isActive()) {
				continue;
			}
			if (!text.isEmpty()) {
				String src = c.getSourceUnit() == null || c.getSourceUnit().getName() == null ? "" : c.getSourceUnit().getName().toLowerCase(); //$NON-NLS-1$
				String tgt = c.getTargetUnit() == null || c.getTargetUnit().getName() == null ? "" : c.getTargetUnit().getName().toLowerCase(); //$NON-NLS-1$
				if (!src.contains(text) && !tgt.contains(text)) {
					continue;
				}
			}
			filtered.add(c);
		}

		tableModel.setRows(filtered);
	}

	private TransparentPanel createButtonPanel() {
		JButton addButton    = new JButton(Messages.getString("PackagingUnitConversionExplorer.add")); //$NON-NLS-1$
		JButton editButton   = new JButton(Messages.getString("PackagingUnitConversionExplorer.edit")); //$NON-NLS-1$
		JButton deleteButton = new JButton(Messages.getString("PackagingUnitConversionExplorer.delete")); //$NON-NLS-1$

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
			PackagingUnitConversionForm form = new PackagingUnitConversionForm();
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
			PackagingUnitConversion conv = tableModel.getRow(index);
			PackagingUnitConversionForm form = new PackagingUnitConversionForm(conv);
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
			PackagingUnitConversion conv = tableModel.getRow(index);
			dao.delete(conv);
			tableModel.removeRow(index);
		} catch (Throwable x) {
			BOMessageDialog.showError(POSConstants.ERROR_MESSAGE, x);
		}
	}

	class PackagingUnitConversionTableModel extends AbstractTableModel {
		private String[] columns = {
			Messages.getString("PackagingUnitConversionExplorer.col.id"), //$NON-NLS-1$
			Messages.getString("PackagingUnitConversionExplorer.col.sourceUnit"), //$NON-NLS-1$
			Messages.getString("PackagingUnitConversionExplorer.col.targetUnit"), //$NON-NLS-1$
			Messages.getString("PackagingUnitConversionExplorer.col.factor"), //$NON-NLS-1$
			Messages.getString("PackagingUnitConversionExplorer.col.active") //$NON-NLS-1$
		};
		private List<PackagingUnitConversion> rows;

		PackagingUnitConversionTableModel(List<PackagingUnitConversion> rows) {
			this.rows = rows;
		}

		void setRows(List<PackagingUnitConversion> rows) {
			this.rows = rows;
			fireTableDataChanged();
		}

		@Override public int getRowCount() { return rows == null ? 0 : rows.size(); }
		@Override public int getColumnCount() { return columns.length; }
		@Override public String getColumnName(int c) { return columns[c]; }

		@Override
		public Object getValueAt(int row, int col) {
			PackagingUnitConversion c = rows.get(row);
			switch (col) {
				case 0: return c.getId();
				case 1: return c.getSourceUnit() == null ? "" : c.getSourceUnit().getName(); //$NON-NLS-1$
				case 2: return c.getTargetUnit() == null ? "" : c.getTargetUnit().getName(); //$NON-NLS-1$
				case 3: return c.getFactor();
				case 4: return c.isActive();
			}
			return null;
		}

		public PackagingUnitConversion getRow(int index) { return rows.get(index); }
		public void addRow(PackagingUnitConversion c) { rows.add(c); fireTableRowsInserted(rows.size()-1, rows.size()-1); }
		public void removeRow(int index) { rows.remove(index); fireTableRowsDeleted(index, index); }
	}
}
