package com.floreantpos.services.webprint.model;

import java.util.Date;
import com.floreantpos.model.Ticket;

public class WebTicketDTO {
	public enum PrintStatus {
		PENDING,
		ALARMING,
		SILENCED,
		PRINTED,
		PRINT_ERROR
	}

	private Integer ticketId;
	private String customerName;
	private Date orderTime;
	private long elapsedSeconds;
	private double totalAmount;
	private PrintStatus status;
	private boolean silenced;
	private String errorMessage;
	private Ticket ticket;

	public WebTicketDTO() {
		this.status = PrintStatus.PENDING;
	}

	public WebTicketDTO(Ticket ticket) {
		this();
		this.ticket = ticket;
		if (ticket != null) {
			this.ticketId = ticket.getId();
			this.orderTime = ticket.getCreateDate();
			this.totalAmount = ticket.getTotalAmount();
			
			String name = null;
			try {
				com.floreantpos.model.Customer c = ticket.getCustomer();
				if (c != null) {
					name = c.getName();
				}
			} catch (Exception e) {
				// Ignore if DB lookup fails
			}
			if (name == null || name.trim().isEmpty()) {
				name = ticket.getProperty(Ticket.CUSTOMER_NAME);
			}

			Integer cid = ticket.getCustomerId();
			boolean hasCid = (cid != null && cid > 0);
			boolean hasName = (name != null && !name.trim().isEmpty());

			if (hasCid && hasName) {
				this.customerName = cid + " - " + name.trim();
			} else if (hasCid) {
				this.customerName = "Customer #" + cid;
			} else if (hasName) {
				this.customerName = name.trim();
			} else {
				this.customerName = "Web Guest";
			}

			updateElapsedSeconds();
		}
	}

	public void updateElapsedSeconds() {
		if (orderTime != null) {
			long now = System.currentTimeMillis();
			this.elapsedSeconds = Math.max(0, (now - orderTime.getTime()) / 1000);
		}
	}

	public Integer getTicketId() {
		return ticketId;
	}

	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
		updateElapsedSeconds();
	}

	public long getElapsedSeconds() {
		return elapsedSeconds;
	}

	public void setElapsedSeconds(long elapsedSeconds) {
		this.elapsedSeconds = elapsedSeconds;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public PrintStatus getStatus() {
		return status;
	}

	public void setStatus(PrintStatus status) {
		this.status = status;
	}

	public boolean isSilenced() {
		return silenced;
	}

	public void setSilenced(boolean silenced) {
		this.silenced = silenced;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}
}
