package org.fog.entities;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoLocation;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.Distribution;

public class Sensor extends SimEntity{
	
	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long outputSize;
	private String appId;
	private int userId;
	private String tupleType;
	private String sensorName;
	private String destModuleName;
	private Distribution transmitDistribution;
	private int controllerId;
	private Application app;
	private double latency;

	public Sensor(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, 
			Distribution transmitDistribution, int cpuLength, int nwLength, String tupleType, String destModuleName) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setDestModuleName(destModuleName);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
	}
	
	public Sensor(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, 
			Distribution transmitDistribution, String tupleType) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
	}
	
	/**
	 * This constructor is called from the code that generates PhysicalTopology from JSON
	 * @param name
	 * @param tupleType
	 * @param// string
	 * @param userId
	 * @param appId
	 * @param transmitDistribution
	 */
	public Sensor(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
		super(name);
		this.setAppId(appId);
		this.setTransmitDistribution(transmitDistribution);
		setTupleType(tupleType);
		setSensorName(tupleType);
		setUserId(userId);
	}

	/*
	Code added by Ben Farmer
 	*/

	//Keys for encryption and decryption
	private String encryptionKey;
	private String publicKey;
	private String privateKey;

	/*
	code end
	*/

	public void transmit(){
		AppEdge _edge = null;
		for(AppEdge edge : getApp().getEdges()){
			if(edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		long cpuLength = (long) _edge.getTupleCpuLength();
		long nwLength = (long) _edge.getTupleNwLength();
		
		Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, 1, nwLength, outputSize, 
				new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setTupleType(getTupleType());
		
		tuple.setDestModuleName(_edge.getDestination());
		tuple.setSrcModuleName(getSensorName());
		int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName() );
		tuple.setActualTupleId(actualTupleId);

		/*
		Ben Farmer code For adding data to the tuple
		*/

		if(CryptographyConfig.getCryptographyEnabled()) {
			DataPacket tupleData = new DataPacket(generateRandomSensorData());
			System.out.println("Sensor " + getId() + " : is sending a Tuple with data - " + new String(tupleData.getData()));

			tupleData.setEncryptionType(CryptographyConfig.getEncryptionType());

			//Check if RSA is being used and enable the digital signature
			if(CryptographyConfig.getEncryptionType() == EncryptionType.RSA){
				if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.STOREDASYMMETRIC){
					tupleData.setDigitalSignature(Cryptography.sign(tupleData.getData(),Cryptography.getStoredAsymmetricPrivateKey()));
				}
				else{
					tupleData.setDigitalSignature(Cryptography.sign(tupleData.getData(),privateKey));
				}
				System.out.println("Sensor " + getId() + " : Has signed the data");
			}
			tupleData.setData(Cryptography.Encrypt(tupleData.getData(), encryptionKey, tupleData.encryptionType));
			tupleData.setOriginSource(getId());
			tupleData.setEncDecLocations(CryptographyConfig.getSensorKeyLocation());

			tuple.setDataPacket(tupleData);
		}

		/*
		End Ben Farmer code
		*/

		send(gatewayDeviceId, getLatency(), FogEvents.TUPLE_ARRIVAL,tuple);

	}

	/*
	Code added by Ben Farmer
	 */
	private byte[] generateRandomSensorData(){ //Used to generate the random sensor data for the packets
		long leftLimit = 1L;
		long rightLimit = 10L;
		long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
		long time = System.currentTimeMillis();

		String finalData = Long.toString(generatedLong) + String.valueOf(time);
		return finalData.getBytes();
	}

	/*
	Code end
	*/

	private int updateTimings(String src, String dest){
		Application application = getApp();
		for(AppLoop loop : application.getLoops()){
			if(loop.hasEdge(src, dest)){
				
				int tupleId = TimeKeeper.getInstance().getUniqueId();
				if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
					TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
				TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
				TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
				return tupleId;
			}
		}
		return -1;
	}


	@Override
	public void startEntity() {

		send(gatewayDeviceId, CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);

		/*
		Code added by Ben Farmer
		Added functionality for key exchange send a key request. Send the key request to either the cloud of fog device.
		 */
		if(app.getCyrptographyConfig().getCryptographyEnabled()){
			Tuple tuple;
			switch (CryptographyConfig.getKeyExchangeType()){

				//Depending on the key exchange type do the following
				case STOREDSYMMETRIC:
					//Stored symmetric, get the stored key from the configuration
					setPublicKey(Cryptography.getStoredSymmetricKey());
					send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
					break;
				case STOREDASYMMETRIC:
					//Stored asymmetric, get the stored key from the configuration
					setPublicKey(Cryptography.getStoredAsymmetricPublicKey());
					send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
					break;

				case EXCHANGEAYSMMETRIC:
					//Exchange asymmetric, send a key request to the cloud along with the public key for digital signature
					tuple = new Tuple(getAppId(), FogUtils
							.generateTupleId(), Tuple.UP, 1, 1, 1, outputSize,
							new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
					tuple.setUserId(getUserId());
					tuple.setSourceDeviceId(getId());
					tuple.setTupleType("KEYREQUEST");

					//Generate key pair
					Cryptography.generateAsymmetricKeys();
					publicKey = Cryptography.getAsymmetricPublicKey();
					privateKey = Cryptography.getAsymmetricPrivateKey();
					//Send the public key to the device
					tuple.setKeyExchangeLocation(CryptographyConfig.getSensorKeyLocation());
					tuple.setDataPacket(new DataPacket(publicKey.getBytes()));
					//Send the tuple key  request to the gateway device
					send(gatewayDeviceId,CloudSim.getMinTimeBetweenEvents(),FogEvents.KEY_REQUEST,tuple);
					System.out.println("Sensor" + getId() + " : Has sent a key request");
					break;
				case EXCHANGESYMMETRIC:
					//Key exchange for symmetric
					tuple = new Tuple(getAppId(), FogUtils
							.generateTupleId(), Tuple.UP, 1, 1, 1, outputSize,
							new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
					tuple.setUserId(getUserId());
					tuple.setSourceDeviceId(getId());
					tuple.setTupleType("SENSORKEYREQUEST");
					tuple.setKeyExchangeLocation(CryptographyConfig.getSensorKeyLocation());
					//Generate key pair
					Cryptography.generateAsymmetricKeys();
					publicKey = Cryptography.getAsymmetricPublicKey();
					privateKey = Cryptography.getAsymmetricPrivateKey();

					//Send the public key to the device
					DataPacket key = new DataPacket(publicKey.getBytes());
					tuple.setDataPacket(key);
					send(gatewayDeviceId,CloudSim.getMinTimeBetweenEvents(),FogEvents.KEY_REQUEST,tuple);
					System.out.println("Sensor" + getId() + " : Has sent a key request");

					break;
			}
		}
		else {
			//If encryption is disabled dont do the key exchange and swap keys
			send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
		}
		/*
		Code End
		 */
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
			case FogEvents.KEY_ARRIVAL: // Process the key arriving
				//If using symmetric decrypt the ecnrypted key and store it
				if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.EXCHANGESYMMETRIC){
					byte[] keyDecrypted = Cryptography.Decrypt((byte[]) ev.getData(),privateKey,EncryptionType.RSA);
					String tempKey = new String(keyDecrypted);
					System.out.println("Sensor : " + getId() + " : Has received a key - " + tempKey);
					setPublicKey(tempKey);
				}else{
					//If using asymmetric just store the key
					System.out.println("Sensor : " + getId() + " : Has received a key - " + ev.getData());

					setPublicKey((String) ev.getData()); //Store the key for usage
				}

				send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE); //Start the sensor sensing values
				break;
		case FogEvents.TUPLE_ACK:
			//transmit(transmitDistribution.getNextValue());
			break;
		case FogEvents.EMIT_TUPLE:

			Log.printLine("Sending tuple to" + ev.getDestination() + "tuple data = " + ev.getData());

			transmit();
			send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
			break;
		}
			
	}
	/*
	Code added by Ben Farmer
	Added key storing methods to store the key sent from the fog device.
	 */
	protected void setPublicKey(String key){
		this.encryptionKey = key;
	}

	/*
	Code End
	 */

	@Override
	public void shutdownEntity() {
		
	}

	public int getGatewayDeviceId() {
		return gatewayDeviceId;
	}

	public void setGatewayDeviceId(int gatewayDeviceId) {
		this.gatewayDeviceId = gatewayDeviceId;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDestModuleName() {
		return destModuleName;
	}

	public void setDestModuleName(String destModuleName) {
		this.destModuleName = destModuleName;
	}

	public Distribution getTransmitDistribution() {
		return transmitDistribution;
	}

	public void setTransmitDistribution(Distribution transmitDistribution) {
		this.transmitDistribution = transmitDistribution;
	}

	public int getControllerId() {
		return controllerId;
	}

	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

}
