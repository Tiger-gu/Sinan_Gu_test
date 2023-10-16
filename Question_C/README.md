A breif introduction:

   As I made progresses on this technical test in the past week, I became more and more ambitious.
I am really really proud of what I have accomplished and am super excited to share with you guys.
I hope that this is a demonstration of my passion for the software development and my skills.

*******************************************************************************************

This project has successfully achieved the following objectives:

a. Scalability

    You can run any any number of nodes you want. After some nodes begin to run,
    you can still add extra nodes. 

b. Maintainability and Readability

c. Resilient to network failures or crashes.

    As long as the zookeeper server is running, any numbers of node failures and crashes will not
    impact the whole system. Even when the master node is down, it doesn't matter. A new master
    will be automatically elected.

d. Real-time geo-replication.  (Writes are real-time)

    This is achieved by utilizing websockets which require less overhead than traditional http.

e. data consistency to some degrees.
    
    Since it is a "only-store-in-RAM" cache service very much like Redis, when a server is down, it will lose all its data.

    There are two ideas:
    a. Save to disk feature.
          OR
    b. We can easily let a newly recovered node send a bulk-sync message to the master node so that the master
    node will send all its cache entries to itself.

    In any case, it is highly unlikely that data will get lost, as long as there is one server running.
    If the master node is down, one of its slave nodes which hold all the data replication will become the 
    master node. No data lost! If a slave node is down, the master node is still running.

f. Cache can expire.

g. (No sure) Simplicty to integrate?
(Once deployed to the cloud, no matter what languages Ormuco uses, it just needs to send 
requests to this cache service with JSON).

h. Flexible Schema(Unsure)
I look at the definition of flexible schema in mongoDB. That is why I choose the data type String for the cache storage.
This project doesn't control the structure. String can be Json Strings which contains complex nested
structure. Or it can just be a simple String.

^^^^^^^^^^^^^^^^^^^^^^^^^^^

Missing functionalities:

a. Read requests should be forwarded to the closest server.
   

**********************************************************************************************************************
This geo-distributed lru cache project utilizes the following comletely free open-source libraries:

1. Spring Boot 
(for handling http requests and websocket communication)

2. Google Guava Cache. 
(for storing the cache. By default, Guava Cache implements LRU caching strategy, especially
when a maximum capacity is set. Also, cache entries can expire. Most important, Guava cache
offers some concurrent capacities)

3. ZooKeeper
(For providing the coordination among nodes(e.g. master election, nodes registry and discovery))

and the following protocols: 

1. REST (this is for handling clients requests)

2. STOMP protocol over websocket (this is to achieve real-time replication from the master to all of its slave nodes)

******************************************************************************************************************************
Summary:

  For this technical test, I decided to write a "Redis-like" geo-distributed cache library
without Redis.(in particular, the master-slave architecture of Redis https://medium.com/@sunny_81705/redis-master-slave-architecture-e730403cb495). Please NOTE that in the Redis's master-slave architecture, 
the master will EXCLUSIVELY handle WRITE requests. READ requests can be handled by both the master and slaves.

  Next, I am going to breifly explain what is going on behind the scene?

  a. Master Election and Nodes discovery

  Each server gets its unique id (nodeId) with the Java UUID class.

  When a server starts to run, it will submit its candidacy for master to Zookeeper by creating an zookeeper ephemeral node under 
  /election (a zookeeper persistent node). 
  
  Once a server is determined to be a slave,  it will be create an zookeeper ephemeral node under /node_discovery(another zookeeper persistent node) and write its own ip address into the node. Also, it will register a watcher on the node preceding itself under /election. Once that node is down, this node will immediately call for a master election. 
  
  Once a server is determined to be a master, it will register a watcher to get the latest updates on the list of live slaves IP addresses under /node_discovery and write its own master ID to the /node_discovery permanent node.

  When a slave server gets promoted from the slave to the master. it will perform similar actions a master will do as described above.
  And, it will remove the ephermal zookeeper node assosiated with itself from the list of ephemermal nodes under /node_discovery.

  A master will send ping over websocket to all of its slaves every 1 second, to which slaves can respond with pong. This is to establish or maintain the websocket sessions. Note that all sessions are stored in the concurrentHashMap in the master node, so that websocket sessions can be re-used.

  b. HTTP and Websocket commmunication

  When a master node recieves a PUT request, it will save the new cache entry into its own cache storage and
  then, it will immediately send sync-put notifications over websocket to all of its slave nodes. Once a
  slave node recieves a sync-put notification, it will store the new entry into its own cache storage.

  When a master node recieves a GET request, it will look up its own cache storage. That is it!

  When a slave node recieves a GET request, it will look up its own cache storage. If it cannot find any,
  what the slave node will do is that it will return null to the client. However, it will clandestinely
  send a help message to the master asking for the answer. Once the master receives the message, it will
  look up in its own storage and send the result back to the slave. Now the slave can store what it doesn't
  have into its own storage. Therefore, if the client gets null the first time, the client can send another
  PUT request to confirm that the paticular cache truly doesn't exsit.

******************************************************************************************************************************
 Q&A

  I am going to address several questions people might ask when they review this project for the first time:

  1. Is the master node a single point of failure?

   No.This is prevented by implmenting master-election algorithm (see above) with ZooKeeper. If a master goes down,
   all slave nodes will automatically elect a new master. Therefore, it gurantees the high availability of the master.
   Please see the code inside the Coordination folder.

  2. Is the Zookeeper which I used to implement the master election a single point of failure?

   If you run this project the way that I describe below, then yes. Do note that: in real production environement, 
   ZooKeeper can be configured to run in a distributed manner spanning over many nodes. However, in this project
   I kept the ZooKeeper configuration minimal, because I didn't have great knowledge of how to configure 
   ZooKeeper in a cloud environment.

  3. Can I add additional nodes any time?

   Yes, as long as the ZooKeeper is ruuning. You can add any number of additional nodes any time. This project
   is designed to be highly scalable. However, do note that regardless how many number of nodes you add, there
   will be only one master.

  4. If a client wants to send a PUT request to create a new cache entry, how does the client know which node
   is the master?

   Ideally, there will be proxy-based load balancers which allow developers to run applications behind a single
   address. Unfortunately, I DID NOT have time to implement any of these in this project.

   Therefore, when you test this project locally, you should check the terminal of each node to see which node
   becomes the master. when a node gets promoted to the master, it will print out "I am the master".

  5. What if there is a spike of PUT requests? The master node will get easily overwhelmed.

   Again, ideally, there should be many masters spanning over many nodes. Each master has its following slaves. 
   PUT requests can be divided among those masters. That possibly requires consistent hashing. 


******************************************************************************************************

How to test this project locally:
    
    1. Make serveral local copies of this project. For each copy, you need to change
       the server.port number in the application.properties file so that each copy gets
       a different port.

    2. First, run the ZooKeeper Server. 
    (Go to https://zookeeper.apache.org/. Download it and unpack it. Change .cfg file in the conf folder to zoo.config.
     set a folder called logs in the root directory. In the zoo.config, set the dataDir to the logs folder location )
    (For Linux: go to the bin folder and run ./zkServer.sh
     For Windows: go to the bin folder and run zkServer.cmd in command prompt)

    3. Click Run in your editor (I use Visual Studio Code) and run all the copies.
       Due to the reasons I described above, you should check which server is the leader.
       On the terminal, it will print out "I am the master".

       Again note: Due to the master-slave architecture, PUT requests can only be sent to the master. 
       
    3. Open your Postman.

       a. Only send PUT requests to the master server. THe new cache entry will be automatically replicated
          to all the slaves servers.

       b. Send GET requests to both the master server and slave servers.

       c. Create more running servers. Stop the master server or any of the slave servers to simulate
          network failures. It will always work as long as the zookeeper is running.


********************************************************************************************************















