package com.floreantpos.customer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.POSConstants;
import com.floreantpos.model.CustomerAddress;
import com.floreantpos.swing.PosButton;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.ui.TitlePanel;
import com.floreantpos.ui.dialog.POSDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.util.POSUtil;

public class CustomerAddressSelectionDialog extends POSDialog {

	private JTable table;
	private CustomerAddressTableModel tableModel;
	private CustomerAddress selectedAddress;

	public CustomerAddressSelectionDialog(List<CustomerAddress> addresses) {
		super(POSUtil.getFocusedWindow(), "Select Delivery Address", true);
		setupDialog(addresses);
	}

	private void setupDialog(List<CustomerAddress> addresses) {
		setTitle("Select Delivery Address");
		setLayout(new BorderLayout(10, 10));

		TitlePanel titlePanel = new TitlePanel();
		titlePanel.setTitle("Select Delivery Address");
		add(titlePanel, BorderLayout.NORTH);

		tableModel = new CustomerAddressTableModel(addresses);
		table = new JTable(tableModel);
		table.setRowHeight(PosUIManager.getSize(35));
		table.setFont(table.getFont().deriveFont(Font.PLAIN, PosUIManager.getFontSize(14)));
		table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, PosUIManager.getFontSize(14)));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Column widths
		if (table.getColumnModel().getColumnCount() >= 3) {
			table.getColumnModel().getColumn(0).setPreferredWidth(100);
			table.getColumnModel().getColumn(1).setPreferredWidth(320);
			table.getColumnModel().getColumn(2).setPreferredWidth(70);
		}

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					doSelect();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(PosUIManager.getSize(540), PosUIManager.getSize(250)));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new MigLayout("insets 10, fill", "[grow][][]", "[]"));
		PosButton btnSelect = new PosButton("SELECT");
		PosButton btnClose = new PosButton(POSConstants.CLOSE.toUpperCase());

		btnSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSelect();
			}
		});

		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		});

		buttonPanel.add(new JPanel(), "cell 0 0, growx");
		buttonPanel.add(btnSelect, "cell 1 0, w 120!, h 45!");
		buttonPanel.add(btnClose, "cell 2 0, w 120!, h 45!");

		add(buttonPanel, BorderLayout.SOUTH);

		// Select default row if available
		if (tableModel.getRowCount() > 0) {
			int defaultRow = 0;
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				CustomerAddress a = tableModel.getAddress(i);
				if (Boolean.TRUE.equals(a.getIsDefault())) {
					defaultRow = i;
					break;
				}
			}
			table.setRowSelectionInterval(defaultRow, defaultRow);
		}

		pack();
		setLocationRelativeTo(POSUtil.getFocusedWindow());
	}

	public void setSelectedAddress(String addressText, String addressLabel) {
		if (tableModel == null || tableModel.getRowCount() == 0) return;
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			CustomerAddress a = tableModel.getAddress(i);
			if (addressText != null && addressText.equals(a.getAddressText())) {
				table.setRowSelectionInterval(i, i);
				return;
			}
			if (addressLabel != null && addressLabel.equalsIgnoreCase(a.getAddressLabel())) {
				table.setRowSelectionInterval(i, i);
				return;
			}
		}
	}

	private void doSelect() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			POSMessageDialog.showError(this, "Please select an address.");
			return;
		}
		selectedAddress = tableModel.getAddress(selectedRow);
		canceled = false;
		dispose();
	}

	public CustomerAddress getSelectedAddress() {
		return selectedAddress;
	}
}
