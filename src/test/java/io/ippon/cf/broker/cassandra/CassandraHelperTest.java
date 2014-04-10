package io.ippon.cf.broker.cassandra;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class CassandraHelperTest {

	@Before
	public void before() throws Exception {
		EmbeddedCassandraServerHelper.startEmbeddedCassandra();
	}
	
	@After
	public void after() throws Exception {
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}
	
	
	@Test
	public void listKeySpaces() throws Exception {
		CassandraHelper helper = new CassandraHelper("localhost:9171", null, null);
		
		List<String> ksNames = helper.listKeySpace();
		assertFalse(ksNames.contains("system"));
		assertFalse(ksNames.contains("system_traces"));
	}
	
	@Test
	public void testkeyspaceExists() throws Exception {
		CassandraHelper helper = new CassandraHelper("localhost:9171", null, null);
		
		assertTrue(helper.keyspaceExists("system"));
		assertFalse(helper.keyspaceExists("myownkeyspace"));
	}
	
	
	@Test
	public void testcreateKeyspace() throws Exception {
		CassandraHelper helper = new CassandraHelper("localhost:9171", null, null);
		String ksName = "mykeyspacefortestcreatekeyspace";
		
		assertFalse(helper.keyspaceExists(ksName));
		
		helper.createKeySpace(ksName);
		
		assertTrue(helper.keyspaceExists(ksName));
	}
}
