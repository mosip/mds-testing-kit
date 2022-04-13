# SBI Testing Kit

This module contains SBI Testing Kit. Which will validate response from MOSIP Device service implementation as per defined [SPEC](https://docs.mosip.io/platform/biometrics/mosip-device-service-specification) 

Device specification can be found in /data/config/masterdata.json

Test case details can be found in /data/config/test-definitions.json
Any new Test cases needed should be updated in above file in json format.

MISP license,partner Id and partner API key should be updated in application.properties for Auth validations

Java 11 is a prerequisite

### Supported SPEC versions

0.9.5

### Defaults

server.port=8081

### Build and run

If any updates in the UI place the build files in the /src/main/resources/static/

Execute the below command to build module

> mvn clean install

Deployable jar file will be created inside target folder. 'test-definitions.json' should be updated in the server

otherwise, we could simply run Main class

> io.mosip.mds.MosipDeviceSpecificationApplication