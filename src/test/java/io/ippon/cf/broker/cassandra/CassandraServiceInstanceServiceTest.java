package io.ippon.cf.broker.cassandra;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class CassandraServiceInstanceServiceTest {
	
	@Test
	public void testComputeKsName() throws Exception {
		String serviceInstanceId = "386ee368-d268-41a5-92b8-f3d64bbe6381";
		
		CassandraServiceInstanceService serviceInstance = new CassandraServiceInstanceService();
		
		assertEquals("cf_386ee368_d268_41a5_92b8_f3d64bbe6381", serviceInstance.computeKsName(serviceInstanceId));
	}
	
	@Test
	public void testExtractServiceInstanceId() throws Exception {
		
		String ksName = "cf_386ee368_d268_41a5_92b8_f3d64bbe6381";
		
		CassandraServiceInstanceService serviceInstance = new CassandraServiceInstanceService();
		
		assertEquals("386ee368-d268-41a5-92b8-f3d64bbe6381", serviceInstance.extractServiceInstanceId(ksName));
		
	}

}
