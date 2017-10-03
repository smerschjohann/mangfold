# General
_Mangfold_ is a tool for development purposes. It provides scripting functionality at runtime of your application.
The scripts can be developed and be executed directly from _IntelliJ_ allowing code completion and other gimmicks only a true IDE can offer.

One usecase of this library is testing. It allows to run your application in an nearly unaltered way with all other application logic running that might be needed for complete functionality tests.

# Example
In the following I will give a short example how this library can be used with the API called `ClientAPI`. It consists of an simple interface:

```java
public interface ClientAPI {
    void open();
    void close();
    
    void rpcOne(String function, String param, CompletableFuture<String> result);
    void rpcTwo(String function, String param, CompletableFuture<String> result);
    void rpcThree(String function, String param, CompletableFuture<String> result);
}
```

In this case the _Client API_ has some initial handshake mechanism hidden behind the `open()` method, which should not be executed too frequently as it is an online service with rate limits active.
Nevertheless, if one wants to test the functionality of this API and the remote service, it would be nice to test it without reconnecting and recompilation.

This problem can be solved with _mangfold_, simply create and run an instance of the `MangfoldAgent` from anywhere in you application and add the dependencies to maven.

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
	<!-- at the current time this has to be installed manually to the local maven repository -->
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

# IntelliJ
In _IntelliJ_ you have to install the _Mangfold-Plugin_. At the current time it is not deployed to the IntelliJ store, so you have to install it from local disk.

After installation, go to `settings -> Tools -> Mangfold` and select the host and port of the system running the agent.

Now create a script file e.g. .kts for kotlin, .py for python, .js for JavaScript or .groovy for groovy. You can now execute the contents of this file with the combination `CTRL + #`. To execute only the selection or if no selection exists, the line of the current caret position hit `CTRL + SHIFT + #`.


# FAQ
## Why _Mangfold_?
Mangfold is the norwegian word for _diversity_ which fits nicely with the various possibilities of this library. 