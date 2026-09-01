package com.floreantpos.bo.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

import com.floreantpos.Messages;
import com.floreantpos.bo.ui.BackOfficeWindow;
import com.floreantpos.bo.ui.explorer.RecepieExplorer;

/**
 * Action that opens (or re-selects) the RecepieExplorer tab in the BackOffice window.
 * Follows the same pattern as {@link PizzaExplorerAction}.
 */
public class RecepieExplorerAction extends AbstractAction {

	/** Tab title key in messages.properties */
	private static final String TAB_TITLE_KEY = "RecepieExplorerAction.tabTitle"; //$NON-NLS-1$

	private com.floreantpos.model.MenuItem menuItem;

	public RecepieExplorerAction() {
		super(Messages.getString("RecepieExplorerAction.menuLabel")); //$NON-NLS-1$
	}

	public RecepieExplorerAction(com.floreantpos.model.MenuItem menuItem) {
		this();
		this.menuItem = menuItem;
	}

	public RecepieExplorerAction(String name) {
		super(name);
	}

	public RecepieExplorerAction(String name, Icon icon) {
		super(name, icon);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		BackOfficeWindow window = com.floreantpos.util.POSUtil.getBackOfficeWindow();
		JTabbedPane tabbedPane = window.getTabbedPane();

		String tabTitle = Messages.getString(TAB_TITLE_KEY);
		int index = tabbedPane.indexOfTab(tabTitle);
		RecepieExplorer explorer;

		if (index == -1) {
			explorer = new RecepieExplorer();
			tabbedPane.addTab(tabTitle, explorer);
		} else {
			explorer = (RecepieExplorer) tabbedPane.getComponentAt(index);
		}
		tabbedPane.setSelectedComponent(explorer);

		if (menuItem != null) {
			explorer.selectOrCreateRecipeFor(menuItem);
		}
	}

}
