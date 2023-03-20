# eco-sched Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ 

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

# Test Generator Configs

Parameters for the test generator are in the application.properties file.

**Available Vm Count:**

- 1024
- 2048
- 3072
- 4096

**Period**

- 10
- 15
- 20
- 25
- 30

**Bid Density:**

- 0.25
- 0.50
- 0.75
- 1.00
- 2.00
- 3.00
- 4.00
- 5.00

**Subbid Count:**

- Poisson(1)
- Poisson(2)
- Poisson(3)


**VM Alternatives:**

- Poisson(1)
- Poisson(2)
- Poisson(3)

**VM Quantity:**

- Poisson(1)
- Poisson(2)
- Poisson(3)
