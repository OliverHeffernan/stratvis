# JUnit tests
This project includes JUnit tests for quick verification that each element of the code is running as expected.

## running the tests
To run the default tests, use the command
```zsh
mvn test
```

### test the external api connection
To test the connection to the external API, use the command
```zsh
mvn test -DrunExternalConnectionTest=true
```

## ExternalApiConnectionTest
The purpose of this test is to verify that the API is running at the URL https://api-stratvis.olihef.com

It is not run automatically because it is not necessary for local development.

