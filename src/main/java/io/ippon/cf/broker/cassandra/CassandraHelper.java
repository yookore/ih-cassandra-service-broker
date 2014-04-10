package io.ippon.cf.broker.cassandra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

/*
 * 
 */
public class CassandraHelper {
	private Cluster cluster;

	public CassandraHelper(String address, String login, String password) {
		CassandraHostConfigurator cHostConf = new CassandraHostConfigurator(
				address);

		HashMap<String, String> credentials = new HashMap<String, String>();

		if (login != null)
			credentials.put("username", login);

		if (password != null)
			credentials.put("password", password);

		this.cluster = HFactory.getOrCreateCluster("cassandra-helper-cluster",
				cHostConf, credentials);
	}

	/**
	 * 
	 * @return
	 */
	public List<String> listKeySpace() {

		List<String> keyspaceNames = new ArrayList<String>();

		for (KeyspaceDefinition ksDef : cluster.describeKeyspaces()) {
			keyspaceNames.add(ksDef.getName());
		}

		// Remove Cassandra default keyspace
		keyspaceNames.remove("system");
		keyspaceNames.remove("system_traces");

		return keyspaceNames;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public void createKeySpace(String name) throws Exception {
		ThriftKsDef ksDef = new ThriftKsDef(name);
		
		cluster.addKeyspace(ksDef, true);
	}

	public void dropKeySpace(String name) {
		cluster.dropKeyspace(name, true);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean keyspaceExists(String name) {
		KeyspaceDefinition ksDef = cluster.describeKeyspace(name);

		if (ksDef == null)
			return false;
		else
			return true;
	}
	

	/**
	 * 
	 * @param username
	 * @param password
	 */
	public void createUser(String username, String password) {

	}
}
