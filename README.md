# Meter Reader (Spring Boot app)
This application will read in an input file in the [**NEM12**](https://aemo.com.au/-/media/files/electricity/nem/retail_and_metering/market_settlement_and_transfer_solutions/2022/mdff-specification-nem12-nem13-v25.pdf?la=en) format and returns the SQL insert statement for it.
It will also ideally throw some errors if the input file did not comply with the **NEM12** format.

## Features
- Reads NEM12 formatted files.
- Generates SQL insert statements.
- Validates input file format and throws errors for non-compliant files.
- Integrated with springdoc for API documentation accessible via Swagger UI.

## API Documentation
The application uses the springdoc library to provide interactive API documentation. You can access the Swagger UI at:

http://localhost:8080/swagger-ui.html

## Requirements

For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven 3](https://maven.apache.org)

## Running the application locally

There are several ways to run this Spring Boot application on your local machine. One way is to execute the `main` method in the `com.skyplor.meterreader.MeterReaderApplication` class from your IDE.

Alternatively, you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```

## Building the Project
To build the project and generate the executable jar, use the following command:

```shell
mvn clean install
```

## Testing
You can run the tests using the following command:

```shell
mvn test
```

## Usage

1. Start the application.
2. Use a tool like `curl` or Postman to upload your NEM12 formatted file to the following endpoint:

    **Endpoint:**
    ```bash
    POST /api/v1/meter-readings/upload
    ```

    **Example using `curl`:**
    ```shell
    curl -F "file=@path/to/your/file.csv" http://localhost:8080/api/v1/meter-readings/upload
    ```

Replace `path/to/your/file.csv` with the actual path to your NEM12 formatted file.

The response will contain the SQL insert statement or an error message if the file did not comply with the NEM12 format.
