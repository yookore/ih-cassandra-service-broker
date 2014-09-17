package io.ippon.cf.broker.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CassandraNameUtilsTest {

	@Test
	public void testComputeKsName() throws Exception {
		String serviceInstanceId = "386ee368-d268-41a5-92b8-f3d64bbe6381";

		assertEquals("cf_386ee368_d268_41a5_92b8_f3d64bbe6381",
				CassandraNameUtils.computeKsName(serviceInstanceId));
	}

	@Test
	public void testExtractServiceInstanceId() throws Exception {

		String ksName = "cf_386ee368_d268_41a5_92b8_f3d64bbe6381";

		assertEquals("386ee368-d268-41a5-92b8-f3d64bbe6381",
				CassandraNameUtils.extractServiceInstanceId(ksName));
	}

	@Test
	public void testUsernameGenerator() throws Exception {
		
		assertEquals("cf_user_a0fa911608749519", CassandraNameUtils
				.usernameGenerator("386ee368-d268-41a5-92b8-f3d64bbe6381"));
		assertNotEquals("cf_user_a0fa911608749519", CassandraNameUtils
				.usernameGenerator("5b2dccd6-f7e0-4785-a817-6b2a1828ab2c"));
		
		assertEquals("cf_user_30ca8a8dcff26177", CassandraNameUtils
				.usernameGenerator("5b2dccd6-f7e0-4785-a817-6b2a1828ab2c"));
		assertNotEquals("cf_user_30ca8a8dcff26177", CassandraNameUtils
				.usernameGenerator("386ee368-d268-41a5-92b8-f3d64bbe6381"));
	}

	@Test
	public void testPasswordGenerator() throws Exception {
		
		Set<String> passwords = new HashSet<>();
		
		for (int i = 0 ; i < 100 ; i++) {
			String password = CassandraNameUtils.passwordGenerator();
			
			assertNotNull(password);
			assertTrue(!passwords.contains(password));
			
			passwords.add(password);
		}		
	}
}
