package org.fog.entities;

/*
Class to use the user defined configuration for there IoT model
Includes:
    - Type of encryption
    - Sensor data decryption
    - Sensor data encryption
    - Stored or Exchange keys
 */

public class CryptographyConfig {
    //Create a new cryptography config based of the user input
    public static void create(boolean enabled,EncryptionType type, CommunicatedDevice sensorKeyLocation, CommunicatedDevice actuatorKeyLocation, KeyExchangeTypes keyExchangeType){
        new CryptographyConfig( enabled, type, sensorKeyLocation, actuatorKeyLocation, keyExchangeType);
    }

    private CryptographyConfig(boolean enabled,EncryptionType type, CommunicatedDevice sensorKeyLocation, CommunicatedDevice actuatorKeyLocation, KeyExchangeTypes keyExchangeType){
        if(actuatorKeyLocation == CommunicatedDevice.CLOUD && (keyExchangeType == KeyExchangeTypes.EXCHANGEAYSMMETRIC ||keyExchangeType== KeyExchangeTypes.EXCHANGESYMMETRIC)){
            throw new IllegalStateException("Actuator cannot request a key from the cloud");
        }
        if(type == EncryptionType.AES &&( keyExchangeType == KeyExchangeTypes.EXCHANGEAYSMMETRIC || keyExchangeType == KeyExchangeTypes.STOREDASYMMETRIC)){
            throw new IllegalStateException("Encryption type must match key exchange type");
        }
        if(type == EncryptionType.RSA && (keyExchangeType == KeyExchangeTypes.EXCHANGESYMMETRIC || keyExchangeType == KeyExchangeTypes.STOREDSYMMETRIC)){
            throw new IllegalStateException("Encryption type must match key exchange type");
        }
        setCrpytographyEnabled(enabled);
        setEncryptionType(type);
        setSensorKeyLocation(sensorKeyLocation);
        setActuatorKeyLocation(actuatorKeyLocation);
        setKeyExchangeType(keyExchangeType);
    }


    //Enable encryption and decryption of classes
    private static boolean cryptographyEnabled = false;

    //Encryption type
    private static EncryptionType encryptionType;

    //Key exchange type
    private static CommunicatedDevice sensorDecryptionLocation;
    private static CommunicatedDevice actuatorEncryptionLocation;

    //Key exchange type pure or stored
    private static KeyExchangeTypes keyExchangeType;

    //Getters and sitters
    public static boolean getCryptographyEnabled(){
        return cryptographyEnabled;
    }
    void setCrpytographyEnabled(boolean enabled){
        cryptographyEnabled = enabled;
    }
    public static EncryptionType getEncryptionType(){
        return encryptionType;
    }
    void setEncryptionType(EncryptionType type){
        encryptionType = type;
    }

    public static CommunicatedDevice getSensorKeyLocation(){
        return sensorDecryptionLocation;
    }
    public static CommunicatedDevice getActuatorKeyLocation(){
        return actuatorEncryptionLocation;
    }
    public void setSensorKeyLocation(CommunicatedDevice point){
        sensorDecryptionLocation = point;
    }
    public void setActuatorKeyLocation(CommunicatedDevice point){
        actuatorEncryptionLocation = point;
    }

    public static KeyExchangeTypes getKeyExchangeType(){
        return keyExchangeType;
    }
    public static void setKeyExchangeType(KeyExchangeTypes keyExchangeType) {
        CryptographyConfig.keyExchangeType = keyExchangeType;
    }
}
