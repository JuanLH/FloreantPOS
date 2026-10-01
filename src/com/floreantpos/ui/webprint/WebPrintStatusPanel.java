package com.floreantpos.ui.webprint;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

import com.floreantpos.swing.TransparentPanel;

public class WebPrintStatusPanel extends TransparentPanel {
	private JLabel lblServiceStatus;
	private JLabel lblDbStatus;
	private JLabel lblAutoPrintStatus;

	public WebPrintStatusPanel() {
		setLayout(new MigLayout("fillx, insets 10", "[][grow][][grow][][grow]", "[]"));
		
		JLabel titleService = new JLabel("Service State:");
		titleService.setFont(titleService.getFont().deriveFont(Font.BOLD, 13f));
		lblServiceStatus = new JLabel("STOPPED");
		lblServiceStatus.setFont(lblServiceStatus.getFont().deriveFont(Font.BOLD, 13f));
		lblServiceStatus.setForeground(Color.RED);

		JLabel titleDb = new JLabel("Database Status:");
		titleDb.setFont(titleDb.getFont().deriveFont(Font.BOLD, 13f));
		lblDbStatus = new JLabel("INITIALIZING");
		lblDbStatus.setFont(lblDbStatus.getFont().deriveFont(Font.BOLD, 13f));

		JLabel titleAutoPrint = new JLabel("Auto Print Mode:");
		titleAutoPrint.setFont(titleAutoPrint.getFont().deriveFont(Font.BOLD, 13f));
		lblAutoPrintStatus = new JLabel("ENABLED");
		lblAutoPrintStatus.setFont(lblAutoPrintStatus.getFont().deriveFont(Font.BOLD, 13f));
		lblAutoPrintStatus.setForeground(new Color(0, 128, 0));

		add(titleService, "cell 0 0");
		add(lblServiceStatus, "cell 1 0");
		add(titleDb, "cell 2 0");
		add(lblDbStatus, "cell 3 0");
		add(titleAutoPrint, "cell 4 0");
		add(lblAutoPrintStatus, "cell 5 0");
	}

	public void setServiceStatus(final boolean running) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (running) {
					lblServiceStatus.setText("RUNNING (ACTIVE)");
					lblServiceStatus.setForeground(new Color(0, 128, 0));
				} else {
					lblServiceStatus.setText("STOPPED");
					lblServiceStatus.setForeground(Color.RED);
				}
			}
		});
	}

	public void setDbStatus(final boolean connected, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (connected) {
					lblDbStatus.setText("CONNECTED (5s Heartbeat)");
					lblDbStatus.setForeground(new Color(0, 128, 0));
				} else {
					lblDbStatus.setText("ERROR: " + message);
					lblDbStatus.setForeground(Color.RED);
				}
			}
		});
	}

	public void setAutoPrintStatus(final boolean enabled) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (enabled) {
					lblAutoPrintStatus.setText("ENABLED (2m Auto)");
					lblAutoPrintStatus.setForeground(new Color(0, 128, 0));
				} else {
					lblAutoPrintStatus.setText("DISABLED (Manual Only)");
					lblAutoPrintStatus.setForeground(Color.ORANGE.darker());
				}
			}
		});
	}
}
