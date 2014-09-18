package com.pivotal.cf.broker.service.impl;

import com.pivotal.cf.broker.model.Catalog;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.service.BeanCatalogService;

public class BeanCatalogServiceTest {

	private BeanCatalogService service;
	
	private Catalog catalog;
	private ServiceDefinition serviceDefinition;
	private static final String SVC_DEF_ID = "svc-def-id";
	
// not revelant
//	@Before
//	public void setup() {
//		serviceDefinition = new ServiceDefinition(SVC_DEF_ID, "Name", "Description", true, null);
//		List<ServiceDefinition> defs = new ArrayList<ServiceDefinition>();
//		defs.add(serviceDefinition);
//		catalog = new Catalog(defs);	
//		service = new BeanCatalogService(catalog);
//	}
//	
//	@Test
//	public void catalogIsReturnedSuccessfully() {
//		assertEquals(catalog, service.getCatalog());
//	}
//	
//	@Test 
//	public void itFindsServiceDefinition() {
//		assertEquals(serviceDefinition, service.getServiceDefinition(SVC_DEF_ID));
//	}
//	
//	
//	@Test 
//	public void itDoesNotFindServiceDefinition() {
//		assertNull(service.getServiceDefinition("NOT_THERE"));
//	}
	
}
