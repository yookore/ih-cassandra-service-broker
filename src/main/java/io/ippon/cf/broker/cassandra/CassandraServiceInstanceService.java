package io.ippon.cf.broker.cassandra;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceExistsException;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.service.ServiceInstanceService;

@Service
public class CassandraServiceInstanceService implements ServiceInstanceService {

	@Override
	public List<ServiceInstance> getAllServiceInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstance createServiceInstance(ServiceDefinition service,
			String serviceInstanceId, String planId, String organizationGuid,
			String spaceGuid) throws ServiceInstanceExistsException,
			ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstance getServiceInstance(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstance deleteServiceInstance(String id)
			throws ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

}
