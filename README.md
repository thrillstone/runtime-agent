# Event Management Agent

The Event Management Agent is a tool used by architects and developers working with Event-Driven Architecture  (EDA) to
discover event streams flowing through a broker as well as the related configuration information. The Event Management
Agent can be used in two different ways:

* As a standalone tool that discovers runtime event data from event or message brokers in the runtime to retrieve EDA
  related data. This data can be exported as an AsyncAPI specification for the broker service and can be imported in
  event management software.
* As the Event Management Agent component of the Solace’s PubSub+ Event Portal product to:
    - discover runtime event data from runtime event brokers
    - populate the Designer and Catalog services of the Event Portal with the runtime data and thus enable the
      management and reuse of EDA assets
    - continuously audit the runtime data and flag discrepancies between the runtime and the intent of the design time
      to ensure that the runtime and design time configurations stay in-sync

Our plan is to open source the Event Management Agent to enable architects and developers to contribute to it as well as
to build plugins so that:

* runtime data can be discovered from additional broker types
* existing plugins can discover additional data
* EDA data can be discovered from other systems, e.g. schemas from schema registries

At this stage (September 2022), the Event Management Agent is still in an active development phase.

### Available today:

* Users can discover Solace PubSub+ and Apache Kafka brokers event flow data
    - Users can discover Solace PubSub+ queues and subscriptions
    - Users can discover Apache Kafka topics and consumer groups
* The Event Management Agent architecture is currently in the form of Java packages

On the roadmap:

* The Event Management Agent has an open source plugin framework
* Support additional Solace PubSub+ and Apache Kafka event broker authentication types in the form of plugins such as
  basic authentication, certificates, Kerberos, etc.
* Collection of topics from events flowing though Solace PubSub+ brokers
* Import discovered data in the Solace PubSub+ Event Portal
* Addition of the infrastructure needed for the Event Management Agent to be a true open source project
* Additional support to more broker types
* Discovery of Apache Kafka connectors
* Discovery of schemas from schema registries
* Introduction of a UI for the Event Management Agent
* Export discovered data as AsyncAPI specifications
* Support for Confluent and MSK flavours of Apache Kafka

## Running the Runtime Agent

### Prerequisites:

* Java 11 (AdoptOpenJDK 11.0.14+ https://adoptium.net/temurin/releases)
* Maven
* Docker
* Runtime Agent Region (for cloud mode)

### Minimum hardware requirements

The Runtime Agent was tested to run with

* 1 CPU
* 1 GB RAM

### Spring-boot properties

These properties are required to run this spring boot application in cloud mode. Update the following properties in the
application.yml file

```
eventPortal.gateway.messaging.connections.url = <secure smf host and port> example  tcps://<host>:<port>
eventPortal.gateway.messaging.connections.msgVpn = <your vpn>
eventPortal.gateway.messaging.connections.users.name= <your name>
eventPortal.gateway.messaging.connections.users.username = <your username>
eventPortal.gateway.messaging.connections.users.password = <your password>
eventPortal.gateway.messaging.connections.users.clientName= <your client name>
```

### Cloning and Building

#### Steps to build and run the service

1. Clone the runtime-agent repository

```
git clone git@github.com:SolaceLabs/runtime-agent.git
```

2. Install maven dependencies

```
cd runtime-agent/service
mvn clean install
```

3. Start the Runtime Agent

```
java -jar application/target/runtime-agent-0.0.1-SNAPSHOT.jar 
```

Alternatively, to build and run the service in IDE

1. Clone the runtime-agent repository

```
git clone git@github.com:SolaceLabs/runtime-agent.git
```

2. Save the code below to a yml file, then run `docker-compose up` against the file **Note**: For Macbook users with M1
   chip, add the property `platform: linux/x86_64` to the file

```
version: '3.1'

services:
  db:
    image: mysql
    container_name: mysql8
    command: --default-authentication-plugin=mysql_native_password
    restart: always
    ports:
      - "3308:3306"
    environment:
      MYSQL_ROOT_PASSWORD: secret
    volumes:
      - ./my-datavolume:/var/lib/mysql 
   ```

3. On a different terminal window, run the command

```
docker exec -it mysql8 /usr/bin/mysql -psecret
```

4. Create the `runtime_agent` database

```
create database if not exists runtime_agent;
```

5. Create an active profile named `mysql-dev` in Spring Boot Run Configurations

![Alt text](docs/images/run-configuration.png "run configuration")

6. Create new yml file in resources with the name `application-mysql-dev.yml`&nbsp;


7. Add the code below to the file

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3308/runtime_agent
    username: root
    password: secret
    driver-class-name: com.mysql.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: create
```

8. Start the application by running this class in Intellij

```
service/application/src/main/java/com/solace/maas/ep/runtime/agent/RuntimeApplication.java
```

## Broker Plugins

The Runtime Agent comes with the following event or message broker plugins included:

* Apache Kafka
* Solace PubSub+
* Confluent
* MSK

## Deployment

There are essentially 2 main modes of deployment:

* SC Connected: The Runtime Agent connects to the runtime region and can be controlled remotely via Event Portal

* Stand-alone: The Runtime Agent is controlled via the REST API and results must be uploaded manually.

## Running a scan

### REST interface

The Runtime Agent includes a REST API that allows the user to initiate a scan. Each plugin requires its own custom set
of authentication and identification attributes that must be supplied by the user.

See [REST Documentation](docs/rest.md) for additional information

## Motivations

See [motivations](./docs/motivations.md)

## Contributions

Contributions are encouraged! If you have ideas to improve an existing plugin, create a new plugin, or improve/extend
the agent framework then please contribute!

## Testing

There are several interesting scenarios to test the Event Management Agent. These scenarios can be divided into two main
categories according to the deployment mode.

* Testing the Event Management Agent as stand-alone service (stand-alone deployment).
* Testing the End to end flow in SC connected mode (From the FrontEnd to the Event Portal, then to the Event Management
  Agent)

### Testing the Event Management Agent in stand-alone mode

The most important test in the stand-alone mode is to ensure that the Event Management Agent runs and collects data
properly. To that end, the test includes the steps below:

1. Update the `plugins` section of the `application.yml` with the details of the messaging service you want to scan.
2. Start the Event Management Agent either from IntelliJ or by running the JAR file.
3. Examine the on-console logs for a log from `RuntimeAgentConfig` class indicating that the messaging service(s) has
   been created.

```
c.s.m.e.r.a.config.RuntimeAgentConfig : Created Kafka messaging service: kafkaDefaultService confluent kafka cluster
c.s.m.e.r.a.config.RuntimeAgentConfig : Created Solace messaging service: solaceDefaultService staging service
```

4. Check the Swagger documentation to learn about the available REST endpoints for the Event Management Agent. To access
   the Swagger documentation, use the link `http://localhost:8180/runtime-agent/swagger-ui/index.html` (Note:
   The Event Management Agent is under continuous development. Therefore, please check the Swagger document to make sure
   you are using the recent endpoint schema).
5. Initiate a scan against the message service of choice by sending a POST request to the endpoint that triggers the
   data collection `/api/v2/runtime/messagingServices/{messagingServiceId}/scan`. The request can be sent either via
   Postman or `curl` command.
6. Ensure that the `destinations` in the request body contains `FILE_WRITER`, i.e., `"destinations":["FILE_WRITER"]`,
   then send the request.
7. Confirm that you receive a scan id, e.g., `3a41a0f5-cd85-455c-a863-9636f69dc7b2`
8. Examine the Event Management Agent console logs to make sure that individual scan types are complete. e.g.,
   `Route subscriptionConfiguration completed for scanId 3a41a0f5-cd85-455c-a863-9636f69dc7b2`
9. Examine the collected data by browsing to the directory `data_collection`. This directory is organized as
   {schedule_id}/{scan_id}/{scan_type.json}
10. Verify that the collected data contains a separate JSON file for each scan type.
11. Verify the contents of each JSON file.
12. Check the logs by browsing to `data_collection/logs/{scan_id}.log` and `general-logs.log` to make sure no exceptions
    or errors occurred.
13. Finally, if you have added the `EVENT_PORTAL` as a destination, check the Event Portal tables to confirm they
    contain the scanned data.

### Testing the Event Management Agent in SC mode

The most important test is SC mode is to verify that the entire end to end flow works properly. That is, from utilizing
the front end to initiate the scan all the way to receiving the scan data in EP database.

1. Start the Front end by running the `maas-ui` locally, then visiting `http://localhost:9000/`
2. Sign in, then enable the option `New Event Portal 2.0 : On`
3. Navigate to the `Runtime Event Manager`, then either choose an existing `Modeled Event Mesh` or create a new one.
4. Open the `Modeled Event Mesh` view, then navigate to the `Runtime` tab.
5. Add a messaging service either by selecting a new or an existing one.
6. Fill in the messaging service details, i.e., `Name`, `SEMP Username`, `SEMP Password`, `SEMP URL`, and `Message VPN`.
   (You can retrieve these details from your messaging service in cluster manager). 7.Select a messaging service
7. After associate a message service to the Modeled Event Mesh, navigate to `Runtime Event Manager`
   then `Runtime Agents`.
8. Set up the Event Management Agent's connection and add the messaging service, then save and create the connection
   file.
9. Update the `application.yml` with the details from the connection file.
10. Start the Event Management Agent either from IntelliJ or by running the JAR file.
11. Examine the on-console logs for a log from `RuntimeAgentConfig` class indicating that the messaging service(s) has
    been created.

```
c.s.m.e.r.a.config.RuntimeAgentConfig : Created Kafka messaging service: kafkaDefaultService confluent kafka cluster
c.s.m.e.r.a.config.RuntimeAgentConfig : Created Solace messaging service: solaceDefaultService staging service
```

10. Refresh the FE and make sure that the badge next to the Event Management Agent shows `Connected`.
11. Navigate to `Runtime Event Manager`, then select the Modeled Event Mesh and navigate to the `Runtime` tab.
12. Select the messaging service and click on `Collect Data`.
13. Examine the Event Management Agent console logs to make sure that individual scan types are complete. e.g.,
    `Route subscriptionConfiguration completed for scanId 3a41a0f5-cd85-455c-a863-9636f69dc7b2`
14. Examine the collected data by browsing to the directory `data_collection`. This directory is organized as
    {schedule_id}/{scan_id}/{scan_type.json}
15. Verify that the collected data contains a separate JSON file for each scan type.
16. Verify the contents of each JSON file.
17. Check the logs by browsing to `data_collection/logs/{scan_id}.log` and `general-logs.log` to make sure no exceptions
    or errors occurred.
18. Finally, check the EP tables to confirm they contain the scanned data.

## Contributors

@gregmeldrum @slunelsolace @AHabes @MichaelDavisSolace
