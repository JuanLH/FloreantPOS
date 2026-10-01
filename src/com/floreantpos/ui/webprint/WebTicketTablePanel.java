package com.floreantpos.ui.webprint;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;

import com.floreantpos.services.webprint.model.WebTicketDTO;
import com.floreantpos.swing.TransparentPanel;

public class WebTicketTablePanel extends TransparentPanel {
	private JTable table;
	private WebTicketTableModel tableModel;

	public WebTicketTablePanel() {
		setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow]"));
		tableModel = new WebTicketTableModel();
		table = new JTable(tableModel);
		table.setRowHeight(35);
		table.setFont(table.getFont().deriveFont(13f));
		table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
		table.setDefaultRenderer(Object.class, new WebTicketCellRenderer());

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, "grow");
	}

	public void updateTickets(final List<WebTicketDTO> tickets) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tableModel.setTickets(tickets);
			}
		});
	}

	public WebTicketDTO getSelectedTicket() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
			return tableModel.getTicketAt(selectedRow);
		}
		return null;
	}

	private static class WebTicketTableModel extends AbstractTableModel {
		private String[] columnNames = {"Ticket #", "Customer", "Order Time", "Elapsed Time", "Total", "Status", "Error Log"};
		private List<WebTicketDTO> tickets = new ArrayList<WebTicketDTO>();
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public void setTickets(List<WebTicketDTO> tickets) {
			this.tickets = tickets != null ? new ArrayList<WebTicketDTO>(tickets) : new ArrayList<WebTicketDTO>();
			fireTableDataChanged();
		}

		public WebTicketDTO getTicketAt(int row) {
			if (row >= 0 && row < tickets.size()) {
				return tickets.get(row);
			}
			return null;
		}

		@Override
		public int getRowCount() {
			return tickets.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			WebTicketDTO dto = tickets.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return dto.getTicketId();
				case 1:
					return dto.getCustomerName();
				case 2:
					return dto.getOrderTime() != null ? sdf.format(dto.getOrderTime()) : "";
				case 3:
					long secs = dto.getElapsedSeconds();
					return String.format("%d min %d sec", secs / 60, secs % 60);
				case 4:
					return String.format("$%.2f", dto.getTotalAmount());
				case 5:
					return dto.getStatus() != null ? dto.getStatus().name() : "";
				case 6:
					return dto.getErrorMessage() != null ? dto.getErrorMessage() : "";
				default:
					return "";
			}
		}
	}

	private static class WebTicketCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			WebTicketTableModel model = (WebTicketTableModel) table.getModel();
			WebTicketDTO dto = model.getTicketAt(row);
			if (dto != null && !isSelected) {
				if (dto.getStatus() == WebTicketDTO.PrintStatus.ALARMING) {
					c.setBackground(new Color(255, 200, 200));
					c.setForeground(Color.RED.darker());
				} else if (dto.getStatus() == WebTicketDTO.PrintStatus.PRINT_ERROR) {
					c.setBackground(new Color(255, 230, 200));
					c.setForeground(Color.RED.darker());
				} else if (dto.getStatus() == WebTicketDTO.PrintStatus.SILENCED) {
					c.setBackground(new Color(255, 255, 210));
					c.setForeground(Color.DARK_GRAY);
				} else if (dto.getStatus() == WebTicketDTO.PrintStatus.PRINTED) {
					c.setBackground(new Color(220, 255, 220));
					c.setForeground(new Color(0, 100, 0));
				} else {
					c.setBackground(Color.WHITE);
					c.setForeground(Color.BLACK);
				}
			}
			return c;
		}
	}
}
