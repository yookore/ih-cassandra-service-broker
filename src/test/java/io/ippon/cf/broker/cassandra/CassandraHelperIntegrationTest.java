package io.ippon.cf.broker.cassandra;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * <p>
 * These integration tests fro {@link CassandraHelper} use <a
 * href="https://github.com/jsevellec/cassandra-unit">CassandraUnit</a>
 * </p>
 * 
 * @author pariviere
 */
public class CassandraHelperIntegrationTest {

	@Before
	public void before() throws Exception {
		EmbeddedCassandraServerHelper
				.startEmbeddedCassandra("cassandra-config-for-helper-test.yaml");

		// When using authentication (configured via
		// cassandra-config-for-helper-test.yaml)
		// Cassandra is not directly available after
		// EmbeddedCassandraServerHelper.startEmbeddedCassandra(String)
		// So just wait a bit for it.
		System.out.println("Waiting for full initialization...");
		Thread.sleep(5000);
	}

	@After
	public void after() throws Exception {
		// When using authentication
		// EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
		// do not work...
		String address = "127.0.0.1";
		int port = 9142;
		String username = "cassandra";
		String password = "cassandra";

		Builder builder = Cluster.builder().addContactPoint(address)
				.withPort(port).withCredentials(username, password);

		Cluster cluster = builder.build();
		Session session = cluster.connect();

		List<String> systemKsNames = new ArrayList<String>();
		systemKsNames.add("system");
		systemKsNames.add("system_auth");
		systemKsNames.add("system_traces");

		ResultSet results = session
				.execute("select * from system.schema_keyspaces");

		for (Row row : results.all()) {
			String ksName = row.getString("keyspace_name");

			if (!systemKsNames.contains(ksName)) {
				session.execute("DROP KEYSPACE " + ksName + ";");
			}
		}

		session.execute("select * from system_auth.users;");
		for (Row row : results.all()) {
			String uName = row.getString("name");

			if (!uName.equals("cassandra")) {
				session.execute("DROP USER " + uName);
			}
		}

		session.close();
	}

	protected CassandraHelper buildHelper() {
		CassandraHelper helper = new CassandraHelper("127.0.0.1", 9142,
				"cassandra", "cassandra");
		return helper;
	}

	@Test
	public void listKeySpaces() throws Exception {
		CassandraHelper helper = buildHelper();

		List<String> ksNames = helper.listKeyspace();
		assertFalse(ksNames.contains("system"));
		assertFalse(ksNames.contains("system_traces"));
	}

	@Test
	public void testkeyspaceExists() throws Exception {
		CassandraHelper helper = buildHelper();

		assertFalse(helper.keyspaceExists("system"));
		assertFalse(helper.keyspaceExists("myownkeyspace"));
	}

	@Test
	public void testCreateAndDropKeyspace() throws Exception {
		CassandraHelper helper = buildHelper();

		String ksName = "mykeyspacefortestcreatekeyspace";

		assertFalse(helper.keyspaceExists(ksName));

		helper.createKeypace(ksName);

		assertTrue(helper.keyspaceExists(ksName));

		helper.dropKeyspace(ksName);

		assertFalse(helper.keyspaceExists(ksName));
	}

	@Test
	public void testCreateUser() throws Exception {
		CassandraHelper helper = buildHelper();

		String uName = "jdoe";
		String uPasswd = "passwd";

		helper.createUser(uName, uPasswd, false);
		helper.dropUser(uName);
	}

	@Test
	public void testGrantUser() throws Exception {
		CassandraHelper helper = buildHelper();

		String uName = "tmosby";
		String uPasswd = "passwd";
		String ksName = "tmobsykeyspace";

		helper.createUser(uName, uPasswd, false);
		helper.createKeypace(ksName);

		helper.grantUserToKeyspace(uName, ksName);
	}

	@Test
	public void testRevokeUser() throws Exception {
		CassandraHelper helper = buildHelper();

		String uName = "bstinson";
		String uPasswd = "passwd";
		String ksName = "bstinsonkeyspace";

		helper.createUser(uName, uPasswd, false);
		helper.createKeypace(ksName);

		helper.revokeUserToKeyspace(uName, ksName);
		helper.grantUserToKeyspace(uName, ksName);
		helper.revokeUserToKeyspace(uName, ksName);

	}

	@Test
	public void testPostConstruct() throws Exception {
		CassandraHelper helper = buildHelper();

		helper.ensureBrokerKeyspace();
	}

	@Test
	public void testCreateServiceInstance() throws Exception {
		CassandraHelper helper = buildHelper();
		helper.ensureBrokerKeyspace();

		Thread.sleep(5000);
		helper.createServiceInstance("8081a3db-f323-4ef1-bdf8-1440adc866a0",
				"mykeyspace", "459b99a6-1834-4376-9903-5fe14650ab98",
				"c4617b8b-463a-466c-a769-6d3a62d614bf",
				"0fc30d27-0d25-492c-b21b-bd0900f6c4ce");
	}
}
