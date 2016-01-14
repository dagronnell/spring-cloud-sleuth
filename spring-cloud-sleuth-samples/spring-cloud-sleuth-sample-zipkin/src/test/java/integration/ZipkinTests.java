/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.sleuth.zipkin.ZipkinProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.JdkIdGenerator;

import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.HttpSpanCollector;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.SpanCollectorMetricsHandler;

import integration.ZipkinTests.WaitUntilZipkinIsUpConfig;
import io.zipkin.server.ZipkinServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sample.SampleZipkinApplication;
import tools.AbstractIntegrationTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { WaitUntilZipkinIsUpConfig.class,
		SampleZipkinApplication.class })
@WebIntegrationTest
@TestPropertySource(properties = "sample.zipkin.enabled=true")
public class ZipkinTests extends AbstractIntegrationTest {

	private static final String APP_NAME = "testsleuthzipkin";
	private static int port = 3380;
	private static String sampleAppUrl = "http://localhost:" + port;

	@Before
	public void setup() {
		ZipkinServer.main(new String[] { "server.port=9411" });
		await().until(zipkinQueryServerIsUp());
	}

	@Test
	@SneakyThrows
	public void should_propagate_spans_to_zipkin() {
		String traceId = new JdkIdGenerator().generateId().toString();

		await().until(httpMessageWithTraceIdInHeadersIsSuccessfullySent(
				sampleAppUrl + "/hi2", traceId));

		await().until(allSpansWereRegisteredInZipkinWithTraceIdEqualTo(traceId));
	}

	@Override
	protected String getAppName() {
		return APP_NAME;
	}

	@Configuration
	@Slf4j
	public static class WaitUntilZipkinIsUpConfig {
		@Bean
		@SneakyThrows
		public SpanCollector spanCollector(final ZipkinProperties zipkin) {
			await().until(new Runnable() {
				@Override
				public void run() {
					try {
						WaitUntilZipkinIsUpConfig.this.getSpanCollector(zipkin);
					}
					catch (Exception e) {
						log.error("Exception occurred while trying to connect to zipkin ["
								+ e.getCause() + "]");
						throw new AssertionError(e);
					}
				}
			});
			return getSpanCollector(zipkin);
		}

		private SpanCollector getSpanCollector(ZipkinProperties zipkin) {
			String url = "http://localhost:" + zipkin.getPort();
			// TODO: parameterize this
			SpanCollectorMetricsHandler metrics = new EmptySpanCollectorMetricsHandler();
			return HttpSpanCollector.create(url, zipkin.getHttpConfig(), metrics);
		}
	}
}