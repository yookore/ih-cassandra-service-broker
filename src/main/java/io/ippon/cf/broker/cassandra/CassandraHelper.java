package io.ippon.cf.broker.cassandra;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * <p>
 * Dedicated to Cassandra manipulation using the Datastax driver.
 * </p>
 * 
 * @author pariviere
 */
public class CassandraHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(CassandraHelper.class);

	private Cluster cluster;

	/**
	 * <p>
	 * Default constructor :
	 * <ul>
	 * <li>Use <code>localhost</code> as host</li>
	 * <li>Use <code>9160</code> as port</li>
	 * </ul>
	 * </p>
	 */
	public CassandraHelper() {
		this("localhost", 9160, null, null);
	}

	/**
	 * <p>
	 * <ul>
	 * <li>Use <code>9160</code> as port</li>
	 * </ul>
	 * </p>
	 * 
	 * @param address
	 *            Adress of the contact point
	 */
	public CassandraHelper(String address) {
		this(address, 9160, null, null);

	}

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
	public CassandraHelper(String address, int port, String username,
			String password) {

		String msg = String
				.format("Create connection to Cassandra cluster %s:%s. ",
						address, port);

		Builder builder = Cluster.builder();

		builder.addContactPoint(address).withPort(port);
		builder.withPort(port);

		if (username != null) {
			msg += String.format(" Username is %s.", username);
			builder.withCredentials(username, password);
		}

		logger.info(msg);

		this.cluster = builder.build();
	}

	/**
	 * 
	 * @param address
	 * @param username
	 * @param password
	 */
	public CassandraHelper(String address, String username, String password) {
		this(address, 9160, username, password);
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
	protected ResultSet executeCQL(String cql) throws Exception {
		Session session = cluster.connect();

		try {
			logger.debug("Executes CQL : '%s'", cql);
			return session.execute(cql);
		} catch (Exception ex) {
			logger.error("Fail to execute CQL order", ex);
			throw ex;
		} finally {
			session.close();
		}
	}

	/**
	 * @param name
	 *            Keyspace name
	 * 
	 * @throws Exception
	 */
	public void createKeypace(String name) throws Exception {

		executeCQL(String.format("CREATE KEYSPACE %s  with replication = "
				+ "{'class': 'SimpleStrategy', 'replication_factor' : 1};",
				name));
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
