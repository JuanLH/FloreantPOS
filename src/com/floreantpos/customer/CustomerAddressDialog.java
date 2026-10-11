package com.floreantpos.customer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import com.floreantpos.model.CustomerAddress;
import com.floreantpos.swing.FixedLengthDocument;
import com.floreantpos.swing.PosSmallButton;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.ui.TitlePanel;
import com.floreantpos.ui.dialog.POSDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.util.POSUtil;

public class CustomerAddressDialog extends POSDialog {

	private TitlePanel titlePanel;
	private JTextField tfAddressLabel;
	private JTextArea tfAddressText;
	private JCheckBox cbDefault;

	private CustomerAddress customerAddress;

	public CustomerAddressDialog() {
		this(null);
	}

	public CustomerAddressDialog(CustomerAddress address) {
		super(POSUtil.getFocusedWindow(), "Customer Address", true);
		this.customerAddress = address;
		setupDialog();
	}

	private void setupDialog() {
		setTitle("Customer Address");
		setLayout(new BorderLayout(10, 10));

		titlePanel = new TitlePanel();
		titlePanel.setTitle(customerAddress == null ? "Add Address" : "Edit Address");
		add(titlePanel, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new MigLayout("insets 15 15 15 15, fill", "[right][grow, fill]", "[][grow][]"));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel lblLabel = new JLabel("Label:");
		tfAddressLabel = new JTextField(30);

		JLabel lblAddress = new JLabel("Address:");
		tfAddressText = new JTextArea(new FixedLengthDocument(620));
		tfAddressText.setRows(4);
		tfAddressText.setLineWrap(true);
		tfAddressText.setWrapStyleWord(true);
		JScrollPane scrlAddress = new JScrollPane(tfAddressText);
		scrlAddress.setPreferredSize(PosUIManager.getSize(350, 100));

		cbDefault = new JCheckBox("Default Address");

		contentPanel.add(lblLabel, "cell 0 0");
		contentPanel.add(tfAddressLabel, "cell 1 0, growx");

		contentPanel.add(lblAddress, "cell 0 1, top");
		contentPanel.add(scrlAddress, "cell 1 1, grow");

		contentPanel.add(cbDefault, "cell 1 2");

		add(contentPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new MigLayout("insets 10, fill", "[grow][][]", "[]"));
		PosSmallButton btnSave = new PosSmallButton("Save");
		PosSmallButton btnCancel = new PosSmallButton("Cancel");

		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});

		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		});

		buttonPanel.add(new JPanel(), "cell 0 0, growx");
		buttonPanel.add(btnSave, "cell 1 0, w 100!, h 40!");
		buttonPanel.add(btnCancel, "cell 2 0, w 100!, h 40!");

		add(buttonPanel, BorderLayout.SOUTH);

		populateData();
		pack();
		setLocationRelativeTo(POSUtil.getFocusedWindow());
	}

	private void populateData() {
		if (customerAddress != null) {
			tfAddressLabel.setText(customerAddress.getAddressLabel());
			tfAddressText.setText(customerAddress.getAddressText());
			cbDefault.setSelected(Boolean.TRUE.equals(customerAddress.getIsDefault()));
		} else {
			tfAddressLabel.setText("Home");
			cbDefault.setSelected(false);
		}
	}

	private void doSave() {
		String text = tfAddressText.getText();
		if (StringUtils.isBlank(text)) {
			POSMessageDialog.showError(this, "Address cannot be empty.");
			return;
		}

		String label = tfAddressLabel.getText();
		if (StringUtils.isBlank(label)) {
			label = "Home";
		}

		if (customerAddress == null) {
			customerAddress = new CustomerAddress();
		}
		customerAddress.setAddressLabel(label.trim());
		customerAddress.setAddressText(text.trim());
		customerAddress.setIsDefault(cbDefault.isSelected());

		canceled = false;
		dispose();
	}

	public CustomerAddress getCustomerAddress() {
		return customerAddress;
	}
}
