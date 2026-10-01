package com.floreantpos.ui.webprint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.miginfocom.swing.MigLayout;

import com.floreantpos.swing.PosButton;
import com.floreantpos.swing.POSToggleButton;
import com.floreantpos.swing.TransparentPanel;

public class WebPrintControlPanel extends TransparentPanel {
	private POSToggleButton btnStartStopService;
	private PosButton btnSilenceAlarm;
	private POSToggleButton btnToggleAutoPrint;
	private PosButton btnPrintToKitchen;

	private ControlPanelListener listener;

	public interface ControlPanelListener {
		void onStartStopToggled(boolean start);
		void onSilenceAlarmClicked();
		void onAutoPrintToggled(boolean enable);
		void onPrintToKitchenClicked();
	}

	public WebPrintControlPanel(final ControlPanelListener listener) {
		this.listener = listener;
		setLayout(new MigLayout("fillx, insets 10", "[grow][grow][grow][grow]", "[50px!]"));

		btnStartStopService = new POSToggleButton("START SERVICE");
		btnSilenceAlarm = new PosButton("SILENCE ALARM");
		btnToggleAutoPrint = new POSToggleButton("AUTO PRINT: ON");
		btnToggleAutoPrint.setSelected(true);
		btnPrintToKitchen = new PosButton("PRINT TO KITCHEN");

		btnStartStopService.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean start = btnStartStopService.isSelected();
				btnStartStopService.setText(start ? "STOP SERVICE" : "START SERVICE");
				if (WebPrintControlPanel.this.listener != null) {
					WebPrintControlPanel.this.listener.onStartStopToggled(start);
				}
			}
		});

		btnSilenceAlarm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (WebPrintControlPanel.this.listener != null) {
					WebPrintControlPanel.this.listener.onSilenceAlarmClicked();
				}
			}
		});

		btnToggleAutoPrint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean enable = btnToggleAutoPrint.isSelected();
				btnToggleAutoPrint.setText(enable ? "AUTO PRINT: ON" : "AUTO PRINT: OFF");
				if (WebPrintControlPanel.this.listener != null) {
					WebPrintControlPanel.this.listener.onAutoPrintToggled(enable);
				}
			}
		});

		btnPrintToKitchen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (WebPrintControlPanel.this.listener != null) {
					WebPrintControlPanel.this.listener.onPrintToKitchenClicked();
				}
			}
		});

		add(btnStartStopService, "grow");
		add(btnSilenceAlarm, "grow");
		add(btnToggleAutoPrint, "grow");
		add(btnPrintToKitchen, "grow");
	}

	public void setServiceRunningState(boolean running) {
		btnStartStopService.setSelected(running);
		btnStartStopService.setText(running ? "STOP SERVICE" : "START SERVICE");
	}

	public void setAutoPrintState(boolean enabled) {
		btnToggleAutoPrint.setSelected(enabled);
		btnToggleAutoPrint.setText(enabled ? "AUTO PRINT: ON" : "AUTO PRINT: OFF");
	}
}
