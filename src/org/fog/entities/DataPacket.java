package org.fog.entities;
/*
Data packet method used on the tuple to store the actual data
 */
public class DataPacket {
    //Information about the data
    EncryptionType encryptionType;
    CommunicatedDevice encDecLocations;
    int originSource;
    byte[] digitalSignature;

    //The actual data
    private byte[] data;

    public void setDigitalSignature(byte[] digitalSignature){
        this.digitalSignature = digitalSignature;
    }
    public byte[] getDigitalSignature(){
        return digitalSignature;
    }
    public void setOriginSource(int originId){
        this.originSource = originId;
    }
    int getOriginSource(){
        return this.originSource;
    }
    public void setEncryptionType(EncryptionType type){
        this.encryptionType = type;
    }
    public EncryptionType getEncryptionType(){
        return encryptionType;
    }
    DataPacket(byte[] data){
        this.data = data;
    }
    public void setData(byte[] data){
        this.data = data;
    }
    public byte[] getData() {
        return data;
    }
    public void setEncDecLocations(CommunicatedDevice location){
        this.encDecLocations = location;
    }
    public CommunicatedDevice getEncDecLocations(){
        return this.encDecLocations;
    }
}
