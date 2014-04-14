package io.ippon.cf.broker.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;

@Service
public class CassandraServiceInstanceBindingService implements
		ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory
			.getLogger(CassandraServiceInstanceBindingService.class);

	private CassandraHelper helper;

	@Autowired
	public CassandraServiceInstanceBindingService(CassandraHelper helper) {
		this.helper = helper;
	}

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			String bindingId, ServiceInstance serviceInstance,
			String serviceId, String planId, String appGuid)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {

		ServiceInstanceBinding serviceInstanceBinding = null;

		try {
			serviceInstanceBinding = helper
					.findServiceInstanceBinding(bindingId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceBrokerException(ex.getMessage());
		}

		if (serviceInstanceBinding != null) {
			throw new ServiceInstanceBindingExistsException(
					serviceInstanceBinding);
		}

		try {

			String username = CassandraNameUtils.usernameGenerator(bindingId);
			String password = CassandraNameUtils.passwordGenerator();

			return helper.createServiceInstanceBinding(bindingId,
					serviceInstance.getId(), username, password);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceBrokerException(ex.getMessage());
		}
	}

	@Override
	public ServiceInstanceBinding getServiceInstanceBinding(String id) {
		try {
			ServiceInstanceBinding serviceInstanceBinding = helper
					.findServiceInstanceBinding(id);

			return serviceInstanceBinding;
		} catch (Exception ex) {
			logger.error("Can not retrieve service instance binding.", ex);
			return null;
		}
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String id)
			throws ServiceBrokerException {
		try {

			ServiceInstanceBinding serviceInstanceBinding = getServiceInstanceBinding(id);
			helper.deleteServiceInstanceBinding(serviceInstanceBinding);

			return serviceInstanceBinding;
		} catch (Exception ex) {
			throw new ServiceBrokerException(ex.getMessage());
		}
	}

}
