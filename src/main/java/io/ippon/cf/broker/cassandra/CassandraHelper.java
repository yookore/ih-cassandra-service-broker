package io.ippon.cf.broker.cassandra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;

/**
 * <p>
 * Dedicated to Cassandra manipulation using the Datastax driver.
 * </p>
 * 
 * @author pariviere
 */
@Component
public class CassandraHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(CassandraHelper.class);

	private Cluster cluster;

	public static final String BROKER_KEYSPACE_NAME = "cf_cassandra_service_broker_persistence";

	public static final String SERVICE_INSTANCE_TABLE_NAME = "serviceinstance";

	public static final String SERVICE_INSTANCE_BINDING_TABLE_NAME = "serviceinstancebinding";

	private String hosts;
	private int port;
	private int replicationFactor;

	/**
	 * <p>
	 * </p>
	 * 
	 * @param address
	 *            Adress of the contact point
	 * @param port
	 *            Port used with this contact point
	 * @param username
	 *            Username if authentication is used. If <code>null</code> no
	 *            authentication
	 * @param password
	 */
	@Autowired
	public CassandraHelper(@Value(value = "${cassandra.host}") String address,
			@Value(value = "${cassandra.port}") int port,
			@Value(value = "${cassandra.username}") String username,
			@Value(value = "${cassandra.password}") String password,
			@Value(value = "${cassandra.replication_factor}") int replicationFactor) {

		this.hosts = address;
		this.port = port;
		this.replicationFactor = replicationFactor;

		String msg = String
				.format("Create connection to Cassandra cluster %s:%s. ",
						address, port);

		Builder builder = Cluster.builder();

		builder.addContactPoint(address).withPort(port);
		builder.withPort(port);

		if (username != null) {
			msg += String.format(" Username is %s, Password is %s", username,
					password);
			builder.withCredentials(username, password);
		}

		logger.info(msg);

		this.cluster = builder.build();
	}

	/**
	 * @param name
	 *            Keyspace name
	 * 
	 * @throws Exception
	 */
	public void createKeypace(String name) throws Exception {

		executeCQL(String.format("CREATE KEYSPACE %s with replication = "
				+ "{'class': 'SimpleStrategy', 'replication_factor' : %d};",
				name, replicationFactor));
	}

	public void createServiceInstance(String serviceInstanceId, String ksName,
			String planId, String organizationId, String spaceId)
			throws Exception {

		String cql = String
				.format("INSERT INTO cf_cassandra_service_broker_persistence.serviceinstance(serviceinstanceid, "
						+ "keyspacename, planid, organizationid,spaceid) VALUES ('%s', '%s', '%s', '%s', '%s');",
						serviceInstanceId, ksName, planId, organizationId,
						spaceId);

		executeCQL(cql);
	}

	/**
	 * 
	 * @param serviceInstanceBindingId
	 * @param serviceInstanceId
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public ServiceInstanceBinding createServiceInstanceBinding(
			String serviceInstanceBindingId, String serviceInstanceId,
			String username, String password) throws Exception {
		String cql = String
				.format("INSERT INTO cf_cassandra_service_broker_persistence.serviceinstancebinding(serviceinstancebindingid, "
						+ "serviceinstanceid, username, password) VALUES ('%s', '%s', '%s', '%s');",
						serviceInstanceBindingId, serviceInstanceId, username,
						password);

		executeCQL(cql);

		createUser(username, password, false);

		grantUserToKeyspace(username,
				CassandraNameUtils.computeKsName(serviceInstanceId));

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("address", this.hosts);
		credentials.put("port", 9160);
		credentials.put("keyspace",
				CassandraNameUtils.computeKsName(serviceInstanceId));
		credentials.put("username", username);
		credentials.put("password", password);

		return new ServiceInstanceBinding(serviceInstanceBindingId,
				serviceInstanceId, credentials, "", "");
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param superuser
	 */
	public void createUser(String username, String password, boolean superuser)
			throws Exception {
		String meta = "NOSUPERUSER";
		if (superuser) {
			meta = "SUPERUSER";
		}

		executeCQL(String.format("CREATE USER %s WITH PASSWORD '%s' %s;",
				username, password, meta));
	}

	public void deleteServiceInstance(String serviceInstanceId)
			throws Exception {
		String cql = String
				.format("DELETE FROM cf_cassandra_service_broker_persistence.serviceinstance WHERE serviceinstanceid = '%s';",
						serviceInstanceId);

		executeCQL(cql);
	}

	public void deleteServiceInstanceBinding(
			ServiceInstanceBinding serviceInstanceBinding) throws Exception {

		if (serviceInstanceBinding == null) {
			return;
		}

		String serviceInstanceBindingId = serviceInstanceBinding.getId();

		String cql = String
				.format("DELETE FROM cf_cassandra_service_broker_persistence.serviceinstancebinding WHERE serviceinstancebindingid = '%s';",
						serviceInstanceBindingId);

		String username = serviceInstanceBinding.getCredentials()
				.get("username").toString();
		String serviceInstanceId = serviceInstanceBinding
				.getServiceInstanceId();

		revokeUserToKeyspace(username,
				CassandraNameUtils.computeKsName(serviceInstanceId));

		dropUser(username);

		executeCQL(cql);
	}

	/**
	 * 
	 * @param name
	 */
	public void dropKeyspace(String name) throws Exception {
		executeCQL(String.format("DROP KEYSPACE %s;", name));
	}

	/**
	 * 
	 * @param username
	 */
	public void dropUser(String username) throws Exception {
		executeCQL(String.format("DROP USER %s;", username));
	}

	/**
	 * <p>
	 * Execute CQL order from the configured Cassandra cluster
	 * </p>
	 * 
	 * @param cql
	 *            CQL statement
	 * @return the result of the query
	 * @throws Exception
	 */
	public ResultSet executeCQL(String cql) throws Exception {
		Session session = cluster.connect();

		try {
			logger.debug("Executes CQL : '{}'", cql);
			return session.execute(cql);
		} catch (Exception ex) {
			logger.error(String.format("Fail to execute CQL order '%s'", cql),
					ex);
			throw ex;
		} finally {
			session.close();
		}
	}

	/**
	 * <p>
	 * Will grant all privileges to the given username on the given keyspace.
	 * </p>
	 * 
	 * @param username
	 * @param keyspace
	 */
	public void grantUserToKeyspace(String username, String keyspace)
			throws Exception {

		if (!keyspaceExists(keyspace)) {
			return;
		}

		if (!userExists(username)) {
			return;
		}

		executeCQL(String.format("GRANT ALL ON KEYSPACE %s to %s;", keyspace,
				username));
	}

	/**
	 * <p>
	 * Check if the given keyspace name is present
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	public boolean keyspaceExists(String name) throws Exception {
		List<String> ksNames = listKeyspace();

		return ksNames.contains(name);
	}

	/**
	 * <p>
	 * Will query Cassandra in order to list existing keyspace
	 * </p>
	 * 
	 * @return a list a keyspace name without system,system_traces and
	 *         system_auth
	 */
	public List<String> listKeyspace() throws Exception {

		List<String> keyspaceNames = new ArrayList<String>();

		ResultSet results = executeCQL("select * from system.schema_keyspaces");

		for (Row row : results.all()) {
			keyspaceNames.add(row.getString("keyspace_name"));
		}

		// Remove Cassandra default keyspace
		keyspaceNames.remove("system");
		keyspaceNames.remove("system_traces");
		keyspaceNames.remove("system_auth");

		return keyspaceNames;
	}

	public List<ServiceInstance> listServiceInstance() throws Exception {

		List<ServiceInstance> serviceInstances = new ArrayList<ServiceInstance>();

		String cql = "SELECT * FROM cf_cassandra_service_broker_persistence.serviceinstance";

		ResultSet results = executeCQL(cql);

		for (Row row : results.all()) {
			String serviceInstanceId = row.getString("serviceinstanceid");
			String planId = row.getString("planid");
			String organizationid = row.getString("organizationid");
			String spaceId = row.getString("spaceid");

			serviceInstances.add(new ServiceInstance(serviceInstanceId, "",
					planId, organizationid, spaceId, ""));
		}

		return serviceInstances;
	}

	public ServiceInstanceBinding findServiceInstanceBinding(
			String serviceInstanceBindingId) throws Exception {

		String cql = String.format(
				"SELECT * FROM cf_cassandra_service_broker_persistence.serviceinstancebinding "
						+ "WHERE serviceinstancebindingid = '%s';",
				serviceInstanceBindingId);

		Row row = executeCQL(cql).one();

		if (row == null) {
			return null;
		}

		String serviceInstanceId = row.getString("serviceinstanceid");
		String username = row.getString("username");
		String password = row.getString("password");

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("username", username);
		credentials.put("password", password);

		return new ServiceInstanceBinding(serviceInstanceBindingId,
				serviceInstanceId, credentials, "", "");
	}

	public List<ServiceInstanceBinding> listServiceInstanceBinding()
			throws Exception {
		List<ServiceInstanceBinding> serviceInstanceBindings = new ArrayList<ServiceInstanceBinding>();

		String cql = "SELECT * FROM cf_cassandra_service_broker_persistence.serviceinstancebinding;";

		ResultSet results = executeCQL(cql);

		for (Row row : results.all()) {
			String serviceInstanceBindingId = row
					.getString("serviceinstancebindingid");
			String serviceInstanceId = row.getString("serviceinstanceid");
			String username = row.getString("username");
			String password = row.getString("password");

			Map<String, Object> credentials = new HashMap<String, Object>();
			credentials.put("username", username);
			credentials.put("password", password);

			serviceInstanceBindings.add(new ServiceInstanceBinding(
					serviceInstanceBindingId, serviceInstanceId, credentials,
					"", ""));
		}

		return serviceInstanceBindings;
	}

	/**
	 * <p>
	 * List user present in Cassandra auth system
	 * </p>
	 * 
	 * @return
	 */
	public List<String> listUser() throws Exception {
		List<String> userNames = new ArrayList<String>();

		ResultSet results = executeCQL("select * from system_auth.users;");

		for (Row row : results.all()) {
			userNames.add(row.getString("name"));
		}

		return userNames;
	}

	/**
	 * <p>
	 * Ensure the required keyspace and column families are present in Cassandra for 
	 * support this service broker
	 * </p>
	 * 
	 * @throws Exception
	 */
	@PostConstruct
	public void ensureBrokerKeyspace() throws Exception {

		if (!keyspaceExists(BROKER_KEYSPACE_NAME)) {
			logger.info(String.format("No keyspace %s found. Creating it..",
					BROKER_KEYSPACE_NAME));

			createKeypace(BROKER_KEYSPACE_NAME);	
		}
		
		Row serviceInstanceRow = executeCQL(" select * from system.schema_columnfamilies where keyspace_name = 'cf_cassandra_service_broker_persistence' "
					+ "and columnfamily_name = 'serviceinstance';").one();
		
		if (serviceInstanceRow == null) {
			logger.info(String.format("Creating %s table...",
					"cf_cassandra_service_broker_persistence.serviceinstance"));

			String cql = "CREATE TABLE cf_cassandra_service_broker_persistence.serviceinstance(serviceinstanceid varchar PRIMARY KEY, "
					+ "keyspacename varchar, planid varchar, organizationid varchar, spaceid varchar);";
			
			executeCQL(cql);
		}
		
		
		Row serviceInstanceBindingRow = executeCQL(" select * from system.schema_columnfamilies where keyspace_name = 'cf_cassandra_service_broker_persistence' "
				+ "and columnfamily_name = 'serviceinstancebinding';").one();
	
		if (serviceInstanceBindingRow == null) {
			logger.info(String
					.format("Creating %s table...",
							"cf_cassandra_service_broker_persistence.serviceinstancebinding"));
	
			String cql = "CREATE TABLE cf_cassandra_service_broker_persistence.serviceinstancebinding(serviceinstancebindingid varchar PRIMARY KEY, "
					+ "serviceinstanceid varchar, username varchar, password varchar);";
	
			executeCQL(cql);
		}
	}
	

	/**
	 * <p>
	 * Will revoke all privileges for the given user on the given keyspace
	 * </p>
	 * 
	 * @param username
	 * @param keyspace
	 */
	public void revokeUserToKeyspace(String username, String keyspace)
			throws Exception {
		if (!keyspaceExists(keyspace)) {
			return;
		}

		if (!userExists(username)) {
			return;
		}

		executeCQL(String.format("REVOKE ALL ON KEYSPACE %s FROM %s;",
				keyspace, username));
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean userExists(String name) throws Exception {
		return listUser().contains(name);
	}
}
