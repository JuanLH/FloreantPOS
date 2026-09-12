package com.floreantpos.bo.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

import com.floreantpos.POSConstants;
import com.floreantpos.bo.ui.BackOfficeWindow;
import com.floreantpos.bo.ui.explorer.IngredientExplorer;
import com.floreantpos.util.POSUtil;

public class IngredientExplorerAction extends AbstractAction {

	public IngredientExplorerAction() {
		super(POSConstants.INGREDIENTS);
	}

	public IngredientExplorerAction(String name) {
		super(name);
	}

	public IngredientExplorerAction(String name, Icon icon) {
		super(name, icon);
	}

	public void actionPerformed(ActionEvent e) {
		BackOfficeWindow backOfficeWindow = POSUtil.getBackOfficeWindow();
		JTabbedPane tabbedPane = backOfficeWindow.getTabbedPane();
		IngredientExplorer explorer;
		int index = tabbedPane.indexOfTab(POSConstants.INGREDIENTS);
		if (index == -1) {
			explorer = new IngredientExplorer();
			tabbedPane.addTab(POSConstants.INGREDIENTS, explorer);
		} else {
			explorer = (IngredientExplorer) tabbedPane.getComponentAt(index);
		}
		tabbedPane.setSelectedComponent(explorer);
	}
}
