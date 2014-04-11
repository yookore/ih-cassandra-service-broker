package io.ippon.cf.broker.cassandra;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

public class CassandraNameUtils {

	public static String computeKsName(String serviceInstanceId) {
		serviceInstanceId = serviceInstanceId.replaceAll("-", "_");
		return CF_PREFIX + serviceInstanceId;
	}

	public static String extractServiceInstanceId(String ksName) {
		if (ksName.startsWith(CF_PREFIX)) {
			String serviceInstanceId = ksName.substring(CF_PREFIX.length(),
					ksName.length());
			return serviceInstanceId.replaceAll("_", "-");
		}

		return null;
	}
	
	public static String passwordGenerator() throws Exception {
		return UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]+", "").substring(0, 16);
	}

	public static String usernameGenerator(String id) throws Exception {
		// https://github.com/cloudfoundry-community/cf-mysql-java-broker/blob/master/src/main/groovy/org/cloudfoundry/community/broker/mysql/service/ServiceBindingService.groovy#L53
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(id.getBytes());

		return new BigInteger(1, digest.digest()).toString(16)
				.replaceAll("[^a-zA-Z0-9]+", "").substring(0, 16);
	}

	private static String CF_PREFIX = "cf_";
}
