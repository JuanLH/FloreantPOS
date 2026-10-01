package com.floreantpos.services.webprint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import com.floreantpos.model.Ticket;
import com.floreantpos.model.dao.TicketDAO;
import com.floreantpos.services.webprint.model.WebTicketDTO;

public class WebTicketPollingService {
	private static final Log logger = LogFactory.getLog(WebTicketPollingService.class);
	private ScheduledExecutorService scheduler;
	private boolean running = false;
	private PollingListener listener;

	public interface PollingListener {
		void onTicketsPolled(List<WebTicketDTO> tickets);
		void onPollingFailure(Exception e);
	}

	public WebTicketPollingService() {
	}

	public WebTicketPollingService(PollingListener listener) {
		this.listener = listener;
	}

	public void setListener(PollingListener listener) {
		this.listener = listener;
	}

	public synchronized void start() {
		if (running) {
			return;
		}
		running = true;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				pollPendingWebTickets();
			}
		}, 0, 5, TimeUnit.SECONDS);
		logger.info("WebTicketPollingService started (5s interval).");
	}

	public synchronized void stop() {
		if (!running) {
			return;
		}
		running = false;
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdownNow();
		}
		logger.info("WebTicketPollingService stopped.");
	}

	public boolean isRunning() {
		return running;
	}

	public void pollPendingWebTickets() {
		Session session = null;
		try {
			session = TicketDAO.getInstance().createNewSession();
			String hql = "FROM Ticket t WHERE (UPPER(t.ticketType) = 'WEB' OR t.isWeb = true) " +
						 "AND t.id NOT IN (SELECT kt.ticketId FROM KitchenTicket kt WHERE kt.ticketId IS NOT NULL) " +
						 "ORDER BY t.createDate ASC";
			List<Ticket> tickets = session.createQuery(hql).list();
			List<WebTicketDTO> dtoList = new ArrayList<WebTicketDTO>();
			if (tickets != null) {
				for (Ticket ticket : tickets) {
					dtoList.add(new WebTicketDTO(ticket));
				}
			}
			if (listener != null) {
				listener.onTicketsPolled(dtoList);
			}
		} catch (Exception e) {
			logger.error("Error polling pending WEB tickets", e);
			if (listener != null) {
				listener.onPollingFailure(e);
			}
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}
