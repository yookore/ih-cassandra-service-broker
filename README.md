IH Cassandra Service Broker
===========================

IH Cassandra Service Broker provides Cassandra keyspaces as service in Cloud Foundry. It has been developped by [Ippon Hosting](http://www.ippon-hosting.com/)

The broker itself does not provides Cassandra but is meant to be deployed alongside a Cassandra cluster and will perform management tasks : 

 - provision keyspace
 - user creation
 - unprovision keyspace
 
# Requirements

Multi-tenant capabilities is a requirement which is not the default with Cassandra. To implement this needs, IH Cassandra Service Broker requires Cassandra to implement both authentication and authorization. 

Short story : setup `cassandra.yaml` directive as follow :

    #authenticator: AllowAllAuthenticator
    authenticator: PasswordAuthenticator
    
    #authorizer: AllowAllAuthorizer
    authorizer: CassandraAuthorizer


Long story : use the reference documentation to configure authentication and authorization :

  - http://www.datastax.com/documentation/cassandra/2.0/cassandra/security/security_config_native_authenticate_t.htm
  - http://www.datastax.com/documentation/cassandra/2.0/cassandra/security/secure_config_native_authorize_t.html


# Broker Internal

This broker will manage its own keyspace on the target Cassandra cluster : `cf_cassandra_service_broker_persistence`.

Service instance and user binding are stored in this keyspace.


 - On service create instance request, the broker creates a dedicated keyspace prepends by `cf_`.
 - On service binding request, an username and its credentials is generated and is given all right on the service instance keyspace.


## Limitations

The broker inherits the same limitations from Cassandra when working with authentication and authorization :

  - each user can see other users keyspace
  - each user can see other users schema
  - an user can not see other users data





# Usage

## Access control

When you register your broker with the cloud controller, you are prompted to enter a username and password. This is used by the broker to verify requests.

Access protect is handle by Spring Security and username and password are stored in `src/main/webapp/WEB-INF/spring/security-context.xml`. __You have to change at least password attribute to
be able to use the broker__.

By default, the password should be encoded using the Spring BCryptPasswordEncoder. A utility class is included to provide encryption. You can encrypt the password executing:

    java com.pivotal.cf.broker.util.PasswordEncoder password-to-encrypt


## Configuration

The property file `src/main/resources/ih-cassandra-service-broker.properties` contains Cassandra related configuration items :

 - `cassandra.host` : address of Cassandra cluster
 - `cassandra.port` : native port of Cassandra cluster
 - `cassandra.replication_factor` : replication factor relative to use on keyspace creation
 - `cassandra.user` and `cassandra.password` : credentials for the Cassandra admin user


# Build

Gradle is required to build this service broker. Once installed, you can build the broker WAR via 

    gradle war
    

The generated file is `build/libs/ih-cassandra-service-broker.war`


# Deployment

You have several alternatives to deploy a service broker in Cloud Foundry :

 - Deploy as usual with Tomcat/Jetty alongside Cloud Foundry
 - Deploy via a bosh release
 - Deploy as an Cloud Foundry application via `cf push`

Once the service broker is available via an URL accessible from Cloud Foundry, you can then register the service broker via the  `cf create-service-broker` command.

# Credits

This broker is build from the sample [spring-service-broker](https://github.com/cloudfoundry-community/spring-service-broker) made by the Cloud Foundry community.
