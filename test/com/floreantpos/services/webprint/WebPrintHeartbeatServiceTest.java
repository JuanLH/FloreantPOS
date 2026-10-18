package com.floreantpos.services.webprint;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

import com.floreantpos.model.Restaurant;

public class WebPrintHeartbeatServiceTest {

	@Test
	public void testRestaurantHeartbeatProperties() {
		Restaurant restaurant = new Restaurant(1);
		Assert.assertFalse(restaurant.isOnlineOrderingTurnedOn());
		Assert.assertNull(restaurant.getOnlineOrderingLastHeartbeat());

		Date now = new Date();
		restaurant.setOnlineOrderingTurnedOn(Boolean.TRUE);
		restaurant.setOnlineOrderingLastHeartbeat(now);

		Assert.assertTrue(restaurant.isOnlineOrderingTurnedOn());
		Assert.assertEquals(now, restaurant.getOnlineOrderingLastHeartbeat());

		restaurant.setOnlineOrderingTurnedOn(Boolean.FALSE);
		Assert.assertFalse(restaurant.isOnlineOrderingTurnedOn());
	}

	@Test
	public void testHeartbeatServiceStartStopState() {
		WebPrintHeartbeatService service = new WebPrintHeartbeatService();
		Assert.assertFalse(service.isRunning());

		service.start();
		Assert.assertTrue(service.isRunning());

		// Multiple starts should be idempotent
		service.start();
		Assert.assertTrue(service.isRunning());

		service.stop();
		Assert.assertFalse(service.isRunning());

		// Multiple stops should be idempotent
		service.stop();
		Assert.assertFalse(service.isRunning());
	}
}
