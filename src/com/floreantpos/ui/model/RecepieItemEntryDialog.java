package com.floreantpos.ui.model;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.model.InventoryItem;
import com.floreantpos.model.Recepie;
import com.floreantpos.model.RecepieItem;
import com.floreantpos.model.dao.InventoryItemDAO;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.util.POSUtil;

/**
 * Dialog to add or edit a single RecepieItem (inventory item + quantity).
 * Shows calculated purchase price and selling price based on quantity.
 */
public class RecepieItemEntryDialog extends JDialog {

	private JComboBox<InventoryItem> cbInventoryItem;
	private JTextField tfQuantity;
	private JTextField tfPurchasePrice;
	private JTextField tfSellingPrice;

	private RecepieItem recepieItem;
	private boolean canceled = true;

	public RecepieItemEntryDialog(java.awt.Frame owner, Recepie recepie) {
		this(owner, recepie, null);
	}

	public RecepieItemEntryDialog(java.awt.Frame owner, Recepie recepie, RecepieItem existingItem) {
		super(owner, true);

		if (existingItem == null) {
			recepieItem = new RecepieItem();
			recepieItem.setRecepie(recepie);
			setTitle(Messages.getString("RecepieItemEntryDialog.newTitle")); //$NON-NLS-1$
		} else {
			recepieItem = existingItem;
			setTitle(Messages.getString("RecepieItemEntryDialog.editTitle")); //$NON-NLS-1$
		}

		initComponents();
		populateView();
		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		setLayout(new BorderLayout(5, 5));

		JPanel formPanel = new JPanel(new MigLayout("fill,wrap 2", "[right]10[grow,fill,200::]"));

		// Inventory Item combo
		List<InventoryItem> items = new InventoryItemDAO().findAll();
		cbInventoryItem = new JComboBox<>();
		cbInventoryItem.addItem(null);
		for (InventoryItem it : items) cbInventoryItem.addItem(it);

		// Quantity field
		tfQuantity = new JTextField(12);
		tfQuantity.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateCalculatedFields();
			}
		});
		cbInventoryItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateCalculatedFields();
			}
		});

		// Read-only calculated fields
		tfPurchasePrice = new JTextField(12);
		tfPurchasePrice.setEditable(false);
		tfSellingPrice  = new JTextField(12);
		tfSellingPrice.setEditable(false);

		formPanel.add(new JLabel(Messages.getString("RecepieItemEntryDialog.inventoryItem"))); //$NON-NLS-1$
		formPanel.add(cbInventoryItem);
		formPanel.add(new JLabel(Messages.getString("RecepieItemEntryDialog.quantity"))); //$NON-NLS-1$
		formPanel.add(tfQuantity);
		formPanel.add(new JLabel(Messages.getString("RecepieItemEntryDialog.purchasePrice"))); //$NON-NLS-1$
		formPanel.add(tfPurchasePrice);
		formPanel.add(new JLabel(Messages.getString("RecepieItemEntryDialog.sellingPrice"))); //$NON-NLS-1$
		formPanel.add(tfSellingPrice);

		// Buttons
		JButton btnOk     = new JButton(com.floreantpos.POSConstants.OK);
		JButton btnCancel = new JButton(com.floreantpos.POSConstants.CANCEL);

		btnOk.addActionListener(new ActionListener() {
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

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(btnOk);
		buttonPanel.add(btnCancel);

		add(formPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void populateView() {
		if (recepieItem.getInventoryItem() != null) {
			cbInventoryItem.setSelectedItem(recepieItem.getInventoryItem());
		}
		tfQuantity.setText(String.valueOf(recepieItem.getQuantity()));
		updateCalculatedFields();
	}

	private void updateCalculatedFields() {
		InventoryItem item = (InventoryItem) cbInventoryItem.getSelectedItem();
		if (item == null) {
			tfPurchasePrice.setText(""); //$NON-NLS-1$
			tfSellingPrice.setText(""); //$NON-NLS-1$
			return;
		}
		try {
			double qty = Double.parseDouble(tfQuantity.getText());
			tfPurchasePrice.setText(String.format("%.4f", qty * item.getUnitPurchasePrice())); //$NON-NLS-1$
			tfSellingPrice.setText(String.format("%.4f", qty * item.getUnitSellingPrice())); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			tfPurchasePrice.setText(""); //$NON-NLS-1$
			tfSellingPrice.setText(""); //$NON-NLS-1$
		}
	}

	private void doSave() {
		InventoryItem selectedItem = (InventoryItem) cbInventoryItem.getSelectedItem();
		if (selectedItem == null) {
			MessageDialog.showError(Messages.getString("RecepieItemEntryDialog.selectItem")); //$NON-NLS-1$
			return;
		}
		double qty;
		try {
			qty = Double.parseDouble(tfQuantity.getText());
			if (qty <= 0) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			MessageDialog.showError(Messages.getString("RecepieItemEntryDialog.invalidQuantity")); //$NON-NLS-1$
			return;
		}
		recepieItem.setInventoryItem(selectedItem);
		recepieItem.setQuantity(qty);
		canceled = false;
		dispose();
	}

	public boolean isCanceled() {
		return canceled;
	}

	public RecepieItem getRecepieItem() {
		return recepieItem;
	}
}
