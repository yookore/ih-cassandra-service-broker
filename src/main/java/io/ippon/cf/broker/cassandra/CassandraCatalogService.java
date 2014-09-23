package io.ippon.cf.broker.cassandra;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivotal.cf.broker.model.Catalog;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.service.CatalogService;

/**
 * An implemnentation of the CatalogService that retrieve catalog from 
 * <em>ih-cassandra-service-broker-metadata.json</em> file.
 * 
 * @author pariviere@ippon.fr
 * 
 */
@Service
public class CassandraCatalogService implements CatalogService {

	private Catalog catalog;
	private List<ServiceDefinition> serviceDefinitions;

	@PostConstruct
	private void postConstruct() throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		InputStream json = getClass().getClassLoader().getResourceAsStream(
				"ih-cassandra-service-broker-metadata.json");

		ServiceDefinition serviceDefinition = mapper.readValue(json,
				ServiceDefinition.class);
		
		serviceDefinitions = new ArrayList<>();
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

		for (ServiceDefinition serviceDefinition : serviceDefinitions) {
			if (serviceDefinition.getId().equals(serviceId)) {
				return serviceDefinition;
			}
		}

		return null;
	}

}
