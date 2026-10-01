package com.floreantpos.services.webprint;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.floreantpos.model.Restaurant;
import com.floreantpos.model.dao.RestaurantDAO;

public class WebPrintHeartbeatService {
	private static final Log logger = LogFactory.getLog(WebPrintHeartbeatService.class);
	private ScheduledExecutorService scheduler;
	private boolean running = false;
	private HeartbeatListener listener;

	public interface HeartbeatListener {
		void onHeartbeatSuccess();
		void onHeartbeatFailure(Exception e);
	}

	public WebPrintHeartbeatService() {
	}

	public WebPrintHeartbeatService(HeartbeatListener listener) {
		this.listener = listener;
	}

	public void setListener(HeartbeatListener listener) {
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
				executeHeartbeat();
			}
		}, 0, 5, TimeUnit.SECONDS);
		logger.info("WebPrintHeartbeatService started (5s interval).");
	}

	public synchronized void stop() {
		if (!running) {
			return;
		}
		running = false;
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdownNow();
		}
		logger.info("WebPrintHeartbeatService stopped.");
	}

	public boolean isRunning() {
		return running;
	}

	public void executeHeartbeat() {
		try {
			Restaurant restaurant = RestaurantDAO.getInstance().get(1);
			if (restaurant == null) {
				restaurant = RestaurantDAO.getRestaurant();
			}
			if (restaurant != null) {
				restaurant.setOnlineOrderingTurnedOn(Boolean.TRUE);
				RestaurantDAO.getInstance().saveOrUpdate(restaurant);
			}
			if (listener != null) {
				listener.onHeartbeatSuccess();
			}
		} catch (Exception e) {
			logger.error("Error updating online ordering heartbeat in database", e);
			if (listener != null) {
				listener.onHeartbeatFailure(e);
			}
		}
	}
}
