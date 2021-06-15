# IFogSimCryptographyV1.0
## Introduction
This code is an extension to the IFogSim IoT simulator (https://github.com/Cloudslab/iFogSim). This code was completed by myself (Ben Farmer) as part of my dissertaiton topic Implementing Cryptography methods into the Internet of Things. It implements different key exhcange methods, encryption types and digital signatures.

## Classes changed 
Changes to the orgional classes where done throughout but the most notible are : 
- Actuator 
- Sensor
- Tuple
- FogDevice
- Application

## Classes added
As well as changes to the already implemented classes multiple classes where incooperated into the simulation.
- CrytographyConfig
- Cryptography
- Datapacket
- EncryptionType
- CommunicatedDevice
- KeyExchangeType
- TimingSimulation

## Usage
To use the simulator it is the same as previosuly using the IFogSim base simulation but with the addition of enabling the cryptographic methods before starting. This is done using the line :
```java
CryptographyConfig.create(true, EncryptionType.AES,CommunicatedDevice.CLOUD,CommunicatedDevice.ENDPOINT, KeyExchangeTypes.EXCHANGESYMMETRIC);
```
The perameters follow the format:
1. If cryptography is enabled (Boolean) 
2. What type of encryption (Enum EncryptionType)
3. Decrtption location of the sensor data (Enum CommunicatedDevice)
4. Encryption of the actuator data (Enum CommunicatedDevice)
5. Key exhcnage type (Enum KeyExchangeType)

This line needs to be implemented before the simulation starts. The example "CleanFromJson.Class" has been included.

## Note
This project has been tested but there may still be faults. 
## Any questions ?
If you have any questions or for the dissertation write up that goes into more detail about the implamentation. feel free to email me at ben_farmer3@hotmail.com.
## Future additions
This project has multiple additions that could be implemented including adding more encryption methods, key exchange types, ability use actuator ids. 
