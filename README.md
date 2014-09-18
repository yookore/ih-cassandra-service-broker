IH Cassandra Service Broker
===========================

IH Cassandra Service Broker provides provides Cassandra keyspaces as service in Cloud Foundry. It has been developped by [Ippon Hosting](http://www.ippon-hosting.com/)

The broker itself does not provides Cassandra but is meant to be deployed alongside a Cassandra cluster and will perform management tasks : 

 - provision keyspace
 - user creation
 - unprovision keyspace
 
# Requirements

Multi-tenant capabilities is a requirement which is not the default with Cassandra. To implement this needs, IH Cassandra Service Broker requires Cassandra to implement both authentication and authorization. Short story : setup `cassandra.yaml` directive as follow :

    #authenticator: AllowAllAuthenticator
    authenticator: PasswordAuthenticator
    
    #authorizer: AllowAllAuthorizer
    authorizer: CassandraAuthorizer


Long story : use the reference documentation to configure authentication and authorization :

  - http://www.datastax.com/documentation/cassandra/2.0/cassandra/security/security_config_native_authenticate_t.htm
  - http://www.datastax.com/documentation/cassandra/2.0/cassandra/security/secure_config_native_authorize_t.html


# Build

Gradle is required to build this service broker. Once installed, you can build the broker WAR via 

    # gradle war
    

The generated file is `build/libs/ih-cassandra-service-broker.war`



# Deployment

You have several alternatives to deploy a service broker in Cloud Foundry :

 - Deploy as usual with Tomcat/Jetty alongside Cloud Foundry
 - Deploy via a bosh release
 - Deploy as an Cloud Foundry application via `cf push`

Once the service broker is available via an URL accessible from Cloud Foundry, you can then register the service broker via the  `cf create-service-broker` command.

# Credits

This broker is build from the sample [spring-service-broker](https://github.com/cloudfoundry-community/spring-service-broker) made by the Cloud Foundry community.
