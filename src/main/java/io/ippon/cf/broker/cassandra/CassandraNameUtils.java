package io.ippon.cf.broker.cassandra;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * <p>
 * Utility class for names and identity generation.
 * </p>
 * 
 * @author pariviere
 * 
 */
public class CassandraNameUtils {

	/**
	 * <p>
	 * </p>
	 * 
	 * @param serviceInstanceId
	 * @return
	 */
	public static String computeKsName(String serviceInstanceId) {
		serviceInstanceId = serviceInstanceId.replaceAll("-", "_");
		return CF_PREFIX + serviceInstanceId;
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param ksName
	 * @return
	 */
	public static String extractServiceInstanceId(String ksName) {
		if (ksName.startsWith(CF_PREFIX)) {
			String serviceInstanceId = ksName.substring(CF_PREFIX.length(),
					ksName.length());
			return serviceInstanceId.replaceAll("_", "-");
		}

		return null;
	}

	/**
	 * <p>
	 * Generates a Cassandra compatible password
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String passwordGenerator() throws Exception {
		return UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]+", "")
				.substring(0, 16);
	}

	/**
	 * <p>
	 * Generates a Cassandra compatible username from an id. 
	 * </p>
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static String usernameGenerator(String id) throws Exception {
		// https://github.com/cloudfoundry-community/cf-mysql-java-broker/blob/master/
		// src/main/groovy/org/cloudfoundry/community/broker/mysql/service/ServiceBindingService.groovy#L53
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(id.getBytes());

		String uuid = new BigInteger(1, digest.digest()).toString(16)
				.replaceAll("[^a-zA-Z0-9]+", "").substring(0, 16);

		return String.format("%suser_%s", CF_PREFIX, uuid);
	}

	private static String CF_PREFIX = "cf_";
}
