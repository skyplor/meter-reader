# Meter Reader (Spring Boot app)
This application will read in an input file in the [**NEM12**](https://aemo.com.au/-/media/files/electricity/nem/retail_and_metering/market_settlement_and_transfer_solutions/2022/mdff-specification-nem12-nem13-v25.pdf?la=en) format and returns the SQL insert statement for it.
It will also ideally throw some errors if the input file did not comply with the **NEM12** format

## Requirements

For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven 3](https://maven.apache.org)

## Running the application locally

There are several ways to run this Spring Boot application on your local machine. One way is to execute the `main` method in the `com.skyplor.meterreader.MeterReaderApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```
