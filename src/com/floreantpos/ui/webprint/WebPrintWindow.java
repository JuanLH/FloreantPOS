package com.floreantpos.ui.webprint;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.floreantpos.services.webprint.WebPrintController;
import com.floreantpos.services.webprint.WebPrintStateListener;
import com.floreantpos.services.webprint.model.WebPrintServiceState;
import com.floreantpos.services.webprint.model.WebTicketDTO;
import com.floreantpos.ui.dialog.POSMessageDialog;

public class WebPrintWindow extends JFrame implements WebPrintStateListener {
	private WebPrintController controller;
	private WebPrintStatusPanel statusPanel;
	private WebTicketTablePanel tablePanel;
	private WebPrintControlPanel controlPanel;

	public WebPrintWindow(WebPrintController controller) {
		super("FloreantPOS — WebPrintService");
		this.controller = controller;
		this.controller.addStateListener(this);

		initUI();
		initListeners();
	}

	private void initUI() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(900, 600);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(10, 10));

		statusPanel = new WebPrintStatusPanel();
		tablePanel = new WebTicketTablePanel();
		controlPanel = new WebPrintControlPanel(new WebPrintControlPanel.ControlPanelListener() {
			@Override
			public void onStartStopToggled(boolean start) {
				if (start) {
					controller.startService();
				} else {
					controller.stopService();
				}
			}

			@Override
			public void onSilenceAlarmClicked() {
				controller.silenceAlarm();
			}

			@Override
			public void onAutoPrintToggled(boolean enable) {
				controller.setAutoPrintEnabled(enable);
				statusPanel.setAutoPrintStatus(enable);
			}

			@Override
			public void onPrintToKitchenClicked() {
				WebTicketDTO selected = tablePanel.getSelectedTicket();
				if (selected != null && selected.getTicketId() != null) {
					controller.manualPrintTicket(selected.getTicketId());
				} else {
					POSMessageDialog.showMessage(WebPrintWindow.this, "Please select a web ticket from the table first.");
				}
			}
		});

		add(statusPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		statusPanel.setServiceStatus(controller.isRunning());
		statusPanel.setAutoPrintStatus(controller.isAutoPrintEnabled());
		controlPanel.setServiceRunningState(controller.isRunning());
		controlPanel.setAutoPrintState(controller.isAutoPrintEnabled());
	}

	private void initListeners() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int option = JOptionPane.showConfirmDialog(
					WebPrintWindow.this,
					"Stopping WebPrintService will close online ordering for customers.\nAre you sure you want to exit?",
					"Confirm Application Exit",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				if (option == JOptionPane.YES_OPTION) {
					controller.stopService();
					dispose();
					System.exit(0);
				}
			}
		});
	}

	@Override
	public void onServiceStateChanged(final WebPrintServiceState state) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				boolean running = (state == WebPrintServiceState.RUNNING);
				statusPanel.setServiceStatus(running);
				controlPanel.setServiceRunningState(running);
			}
		});
	}

	@Override
	public void onTicketsUpdated(final List<WebTicketDTO> tickets) {
		tablePanel.updateTickets(tickets);
	}

	@Override
	public void onHeartbeatStatusChanged(final boolean connected, final String message) {
		statusPanel.setDbStatus(connected, message);
	}

	@Override
	public void onPrintError(final Integer ticketId, final String errorMessage) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				POSMessageDialog.showError(WebPrintWindow.this, "Kitchen Printer Error for Ticket #" + ticketId + ":\n" + errorMessage);
			}
		});
	}
}
