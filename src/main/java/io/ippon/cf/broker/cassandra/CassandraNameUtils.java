package io.ippon.cf.broker.cassandra;

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

	private static String CF_PREFIX = "cf_";
}
