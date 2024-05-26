- # General
    - #### Team#: 24

    - #### Names: Akshita Akumalla, Niharika Kumar

    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment:
      - Locally:
        - Git clone cs122b-s24-team-suikalords in IntelliJ
        - Edit Project Configuration
          - Select Run/Debug Configuration --> Edit Configurations
          - Add New Configuration --> Tomcat Server (local)
          - Mark cs122b-suikalordsproject:war as the artifact to deploy
        - Deploying
          - Run Tomcat configuration

      - AWS:
        - Connect to AWS instance
        - Get project
          - Git clone cs122b-s24-team-suikalords in IntelliJ
          - cd cs122b-s24-team-suikalords
        - Building project
          - mvn clean package 
          - sudo cp ./target/*.war /var/lib/tomcat10/webapps/
        - Deploying
          - load balancer public ip/cs122b-suikalordsproject/login.html
  
    - #### Collaborations and Work Distribution:
      - Akshita:
        - Full text search
        - Autocomplete
      - Niharika:
        - JDBC connection pooling
        - MYSQL master-slave replication
        - Load balancing


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - webcontent/META-INF/context.xml
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
      - Fabflix utilizes connection pooling to efficiently manage and reuse database connections. MySQL credentials and number of connections in the pool are provided to JDBC through the context.xml file, allowing for the creation of the connection pool. When a servlet requires a database connection, it simply requests one from the pool, which saves time and resources by reusing existing connections instead of establishing new ones for each request.
  
    - #### Explain how Connection Pooling works with two backend SQL.
      - When there are two backend SQL databases, connection pooling operates similarly. The main difference is that with two backend SQL, a load balancer ensures that incoming connection requests are distributed between the two databases, optimizing resource utilization. Each database instance contributes to the connection pool, allowing servlets to request connections from either the master or slave database as needed. This arrangement not only improves efficiency by reducing connection setup overhead but also enhances scalability and reliability by leveraging multiple database instances.


- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
        - webcontent/META-INF/context.xml  

    - #### How read/write requests were routed to Master/Slave SQL?
      - The load balancer routes requests to master/slave SQL depending on which instance is available. In order to make sure that read can happen from both instances, all the queries that are read-only are routed to localhost (the SQL that is on the available instance). In order to make sure that writes only happen from master SQL, a new datasource is setup in context.xml that solely connects to master SQL. 
