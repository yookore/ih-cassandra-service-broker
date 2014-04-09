package com.pivotal.cf.broker.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivotal.cf.broker.model.Catalog;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceDefinition;

/**
 * An implementation of the CatalogService that gets the catalog injected (ie
 * configure in spring config)
 * 
 * @author sgreenberg@gopivotal.com
 * 
 */
@Service
public class BeanCatalogService implements CatalogService {

	private Catalog catalog;
	private ServiceDefinition serviceDefinition;
	private Map<String, ServiceDefinition> serviceDefs = new HashMap<String, ServiceDefinition>();
	
	
	@Autowired
	public BeanCatalogService(Catalog catalog) {
		// this.catalog = catalog;
		// initializeMap();
		try {
			buildData();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void buildData() throws JsonMappingException,
			MalformedURLException, JsonParseException, IOException {

		ObjectMapper mapper = new ObjectMapper();
				
		InputStream json = getClass().getClassLoader().getResourceAsStream("ih-cassandra-service-broker-metadata.json");

		ServiceDefinition serviceDefinition = mapper.readValue(json,
				ServiceDefinition.class);
		
		this.serviceDefinition = serviceDefinition;

		List<ServiceDefinition> serviceDefinitions = new ArrayList<ServiceDefinition>();
		serviceDefinitions.add(serviceDefinition);

		Catalog catalog = new Catalog(serviceDefinitions);
		this.catalog = catalog;
	}

	@Override
	public Catalog getCatalog() {
		return catalog;
	}

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId) {
		return this.serviceDefinition;
	}

}
