package io.ippon.cf.broker.cassandra;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceExistsException;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.service.ServiceInstanceService;

@Service
public class CassandraServiceInstanceService implements ServiceInstanceService {

	private CassandraHelper helper;

	public CassandraServiceInstanceService() {
		helper = new CassandraHelper("10.0.16.202", "cassandra", "cassandra");
	}

	@Override
	public List<ServiceInstance> getAllServiceInstances() {

		List<ServiceInstance> serviceInstances = new ArrayList<ServiceInstance>();

		try {
			for (String ksName : helper.listKeyspace()) {

				String serviceInstanceId = extractServiceInstanceId(ksName);

				if (serviceInstanceId != null) {
					serviceInstances.add(new ServiceInstance(serviceInstanceId,
							null, null, null, null, null));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return serviceInstances;
	}

	@Override
	public ServiceInstance createServiceInstance(ServiceDefinition service,
			String serviceInstanceId, String planId, String organizationGuid,
			String spaceGuid) throws ServiceInstanceExistsException,
			ServiceBrokerException {

		ServiceInstance serviceInstance = new ServiceInstance(
				serviceInstanceId, service.getId(), planId, organizationGuid,
				spaceGuid, null);

		String ksName = computeKsName(serviceInstanceId);
		try {
			boolean ksExists = helper.keyspaceExists(ksName);

			if (ksExists) {
				throw new ServiceInstanceExistsException(serviceInstance);
			} else {
				helper.createKeypace(ksName);

				return serviceInstance;
			}
		} catch (Exception ex) {
			throw new ServiceBrokerException(ex.getMessage());
		}
	}

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return new ServiceInstance(id, null, null, null, null,
				"http://mydashboard/cassandra");
	}

	@Override
	public ServiceInstance deleteServiceInstance(String id)
			throws ServiceBrokerException {

		try {
			String ksName = computeKsName(id);
			helper.dropKeyspace(ksName);
		} catch (Exception ex) {
			throw new ServiceBrokerException(ex.getMessage());
		}

		return getServiceInstance(id);
	}

	protected String computeKsName(String serviceInstanceId) {
		serviceInstanceId = serviceInstanceId.replaceAll("-", "_");
		return CF_PREFIX + serviceInstanceId;
	}

	protected String extractServiceInstanceId(String ksName) {
		if (ksName.startsWith(CF_PREFIX)) {
			String serviceInstanceId = ksName.substring(CF_PREFIX.length(),
					ksName.length());
			return serviceInstanceId.replaceAll("_", "-");
		}

		return null;
	}

	private static String CF_PREFIX = "cf_";

}
