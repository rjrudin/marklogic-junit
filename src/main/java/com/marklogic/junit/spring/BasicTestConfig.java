package com.marklogic.junit.spring;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.ext.modulesloader.ModulesLoader;
import com.marklogic.client.ext.modulesloader.impl.AssetFileLoader;
import com.marklogic.client.ext.modulesloader.impl.DefaultModulesLoader;
import com.marklogic.client.ext.spring.BasicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Extends BasicConfig (from ml-javaclient-util) with test-specific properties.
 */
@Configuration
@PropertySource({"file:gradle.properties"})
public class BasicTestConfig extends BasicConfig {

	@Value("${mlTestRestPort:0}")
	private Integer mlTestRestPort;

	@Override
	protected Integer getRestPort() {
		return (mlTestRestPort != null && mlTestRestPort > 0) ? mlTestRestPort : getMlRestPort();
	}

	public Integer getMlTestRestPort() {
		return mlTestRestPort;
	}

	/**
	 * This is included by default so that ModulesLoaderTestExecutionListener can be used.
	 *
	 * @return
	 */
	@Bean
	public ModulesLoader modulesLoader() {
		return new DefaultModulesLoader(modulesAssetFileLoader());
	}

	/**
	 * Makes some assumptions about how to connect to the modules database and how to authenticate - feel free to
	 * override in a subclass.
	 *
	 * @return
	 */
	@Bean
	public AssetFileLoader modulesAssetFileLoader() {
		DatabaseClient client = DatabaseClientFactory.newClient(getMlHost(), 8000, getMlAppName() + "-modules",
			getMlUsername(), getMlPassword(), DatabaseClientFactory.Authentication.DIGEST);
		return new AssetFileLoader(client);
	}

	@Override
	protected String buildContentDatabaseName(String mlAppName) {
		return mlAppName + "-test-content";
	}

}