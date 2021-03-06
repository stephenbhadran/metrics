package com.yammer.metrics.jersey.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.tests.resources.MeteredResource;

/**
 * Tests importing {@link InstrumentedResourceMethodDispatchAdapter} as a
 * singleton in a Jersey {@link com.sun.jersey.api.core.ResourceConfig}
 */
public class MeteredResourceTest extends JerseyTest {
	static {
		Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
	}

	private MetricsRegistry registry;

	@Override
	protected AppDescriptor configure() {
		this.registry = new MetricsRegistry();

		final DefaultResourceConfig config = new DefaultResourceConfig();
		config.getSingletons().add(new InstrumentedResourceMethodDispatchAdapter(registry));
		config.getClasses().add(MeteredResource.class);

		return new LowLevelAppDescriptor.Builder(config).build();
	}

	@Test
	public void meteredFirstIsMetered() {
		assertThat(resource().path("metered-first").get(String.class), is("yay-first"));

		final Meter meter = registry.newMeter(MeteredResource.class, "meteredFirst", "blah", TimeUnit.SECONDS);
		assertThat(meter.getCount(), is(1L));
	}

	@Test
	public void meteredSecondIsMetered() {
		assertThat(resource().path("metered-second").get(String.class), is("yay-second"));

		final Meter meter = registry.newMeter(MeteredResource.class, "meteredSecond", "blah", TimeUnit.SECONDS);
		assertThat(meter.getCount(), is(1L));
	}

}
