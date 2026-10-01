package com.floreantpos.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import com.floreantpos.config.TerminalConfig;
import com.floreantpos.services.webprint.WebPrintController;
import com.floreantpos.ui.webprint.WebPrintWindow;
import com.floreantpos.util.DatabaseUtil;

public class WebPrintServiceMain {

	public static void main(final String[] args) {
		try {
			if (TerminalConfig.getDefaultLocale() != null) {
				java.util.Locale.setDefault(TerminalConfig.getDefaultLocale());
			}

			try {
				PlasticXPLookAndFeel.setPlasticTheme(new ExperienceBlue());
				UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
			} catch (Exception ignored) {
			}

			Application.getInstance().initializeSystemHeadless();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					WebPrintController controller = new WebPrintController();
					WebPrintWindow window = new WebPrintWindow(controller);
					window.setVisible(true);
					controller.startService();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
