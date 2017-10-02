# General
_Mangfold_ is a tool for development purposes. It provides scripting functionality at runtime of your application.
The scripts can be developed and run directly from _IntelliJ_ allowing code completion and other gimmicks only a true IDE can offer.

One usecase of this library is testing. It allows to run your application in an nearly unaltered way with all threads running that might be needed for complete functionality tests. 
For example if one wants to test an API which needs to communicate with other services, there is an initialization phase which would make iterational unit tests undesirable.

# Example
In the following I will give a short example how this library can be used:

```java
public interface ClientAPI {
    void open();
    void close();
    
    void rpcOne(String function, String param, CompletableFuture<String> result);
    void rpcTwo(String function, String param, CompletableFuture<String> result);
    void rpcThree(String function, String param, CompletableFuture<String> result);
    void rpcFour(String function, String param, CompletableFuture<String> result);
    void rpcFive(String function, String param, CompletableFuture<String> result);
}
```

The Client API will connect to the server by calling open and keeps the connection open until `close()` is called.
If one wants to test the functionality of this API, it would be nice to test it without having to restart the application.
NOTE: This should always be just an addition to normal unit tests.

So one would add the following to the application code and add the dependencies to either maven or gradle:

## Java
```java
public class Starter {
    public static void main(String[] args){
        // [...]
        ClientAPI client = ClientAPIBuilder.server("address").port(4711).build();
        client.open();
        final MangfoldAgent agent = new MangfoldAgent(14237, Thread.currentThread().getContextClassLoader()) // use the classloader of the current thread
                                       .putBinding("client", client) // make the client available from scripts
                                       .start(); // start the server
        // [...]
    }
}
```
 
## Maven
```maven
<dependencies>
    <dependency>
        <groupId>it.tty0.mangfold</groupId>
        <artifactId>mangfold-agent</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- For jython support -->
    <dependency>
        <groupId>org.python</groupId>
        <artifactId>jython-standalone</artifactId>
        <version>2.7.1</version>
    </dependency>
    <!-- For Groovy support -->
    <dependency>
        <groupId>org.codehaus.groovy</groupId>
        <artifactId>groovy-all</artifactId>
        <version>2.4.12</version>
    </dependency>
    <!-- For Kotlin Support -->
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-stdlib</artifactId>
        <version>1.1.51</version>
    </dependency>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-compiler</artifactId>
        <version>1.1.51</version>
    </dependency>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-script-util</artifactId>
        <version>1.1.51</version>
    </dependency>
</dependencies>
```


# FAQ
## Why _Mangfold_?
Mangfold is the norwegian word for _diversity_ which fits nicely with the various possibilities of this library. 