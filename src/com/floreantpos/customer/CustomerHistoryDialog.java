/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
package com.floreantpos.customer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.POSConstants;
import com.floreantpos.main.Application;
import com.floreantpos.model.Customer;
import com.floreantpos.model.Ticket;
import com.floreantpos.model.dao.TicketDAO;
import com.floreantpos.swing.PosButton;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.ui.TicketListView;
import com.floreantpos.ui.TitlePanel;
import com.floreantpos.ui.dialog.POSDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.views.OrderInfoDialog;
import com.floreantpos.ui.views.OrderInfoView;

public class CustomerHistoryDialog extends POSDialog {
	private Customer customer;
	private TicketListView ticketListView;

	public CustomerHistoryDialog(Customer customer) {
		super(Application.getPosWindow(), true);
		this.customer = customer;
		initUI();
	}

	@Override
	protected void initUI() {
		setLayout(new BorderLayout());

		TitlePanel titlePanel = new TitlePanel();
		String customerTitle = "";
		if (customer != null && customer.getName() != null) {
			customerTitle = " - " + customer.getName();
		}
		titlePanel.setTitle(Messages.getString("CustomerSelectionDialog.24") + customerTitle); //$NON-NLS-1$
		add(titlePanel, BorderLayout.NORTH);

		Integer customerId = customer != null ? customer.getAutoId() : null;
		ticketListView = new TicketListView(customerId, true);
		ticketListView.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showTicketDetails();
				}
			}
		});
		add(ticketListView, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new MigLayout("fill, ins 5", "[grow, align center]", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		PosButton btnDetails = new PosButton(POSConstants.ORDER_INFO_BUTTON_TEXT);
		btnDetails.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showTicketDetails();
			}
		});

		PosButton btnClose = new PosButton(POSConstants.CLOSE);
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		int buttonWidth = PosUIManager.getSize(120);
		int buttonHeight = PosUIManager.getSize(40);
		bottomPanel.add(btnDetails, "w " + buttonWidth + "!, h " + buttonHeight + "!, split 2, align center"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		bottomPanel.add(btnClose, "w " + buttonWidth + "!, h " + buttonHeight + "!"); //$NON-NLS-1$ //$NON-NLS-2$

		add(bottomPanel, BorderLayout.SOUTH);

		setSize(PosUIManager.getSize(850), PosUIManager.getSize(600));
		setLocationRelativeTo(Application.getPosWindow());
	}

	private void showTicketDetails() {
		try {
			Ticket ticket = ticketListView.getSelectedTicket();
			if (ticket == null) {
				POSMessageDialog.showMessage(this, Messages.getString("TicketListView.14")); //$NON-NLS-1$
				return;
			}
			ticket = TicketDAO.getInstance().loadFullTicket(ticket.getId());
			OrderInfoView view = new OrderInfoView(Arrays.asList(ticket));
			OrderInfoDialog dialog = new OrderInfoDialog(view);
			dialog.setSize(PosUIManager.getSize(600), PosUIManager.getSize(600));
			dialog.open();
		} catch (Exception ex) {
			com.floreantpos.PosLog.error(getClass(), ex);
			POSMessageDialog.showError(this, Messages.getString("PosMessage.100"), ex); //$NON-NLS-1$
		}
	}
}
