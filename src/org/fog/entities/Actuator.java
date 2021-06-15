package org.fog.entities;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.utils.*;

public class Actuator extends SimEntity{

	private int gatewayDeviceId;
	private double latency;
	private GeoLocation geoLocation;
	private String appId;
	private int userId;
	private String actuatorType;
	private Application app;
	
	public Actuator(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, String actuatorType, String srcModuleName) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		setUserId(userId);
		setActuatorType(actuatorType);
		setLatency(latency);
	}
	
	public Actuator(String name, int userId, String appId, String actuatorType) {
		super(name);
		this.setAppId(appId);
		setUserId(userId);
		setActuatorType(actuatorType);
	}

	private String decryptionKey;
	private String connectedDevicePublicKey;
	private String publicKey;
	private String privateKey;

	@Override
	public void startEntity() {

		sendNow(gatewayDeviceId, FogEvents.ACTUATOR_JOINED, getLatency());

		/*
		Code added by Ben Farmer
		 */
		//When an actuator joins the system it tells other devices what key it is using.
		if(CryptographyConfig.getCryptographyEnabled()) {
			Tuple tuple;
			switch (CryptographyConfig.getKeyExchangeType()){
				case STOREDSYMMETRIC:
					//Use the standard stored key from the cryptography class
					decryptionKey = Cryptography.getStoredSymmetricKey();
					break;
				case STOREDASYMMETRIC:
					//Use the standard stored decryption key from the cryptography class
					privateKey = Cryptography.getStoredAsymmetricPrivateKey();
					break;
				case EXCHANGEAYSMMETRIC:
					//Generate, store and send the public key to defined location from the cryptography config
					Cryptography.generateAsymmetricKeys();
					publicKey = Cryptography.getAsymmetricPublicKey();
					privateKey = Cryptography.getAsymmetricPrivateKey();
					tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, 100, 1, 100, 1,
							new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
					tuple.setUserId(getUserId());
					tuple.setSourceDeviceId(getId());
					tuple.setTupleType("ACTUATORKEYREQUEST");

					tuple.setKeyExchangeLocation(CryptographyConfig.getActuatorKeyLocation());

					//Have to set key within the data of the tuple to make sure it gets to the correct devices
					tuple.setDataPacket(new DataPacket(publicKey.getBytes()));
					System.out.println("Actuator" + getId() + " : Has sent a key and waiting for a response");
					send(gatewayDeviceId, CloudSim.getMinTimeBetweenEvents(), FogEvents.KEY_ARRIVAL, tuple);
					break;

				case EXCHANGESYMMETRIC:
					//Generate an asymmetric key to use to encrypt the key sent from the FogDevice
					Cryptography.generateAsymmetricKeys();
					publicKey = Cryptography.getAsymmetricPublicKey();
					privateKey = Cryptography.getAsymmetricPrivateKey();
					tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, 100, 1, 100, 1,
						new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
					tuple.setUserId(getUserId());
					tuple.setSourceDeviceId(getId());
					tuple.setTupleType("ACTUATORKEYREQUEST");

					tuple.setKeyExchangeLocation(CryptographyConfig.getActuatorKeyLocation());

					//Have  to set key within the data of the tuple to make sure it gets to the correct devices
					tuple.setDataPacket(new DataPacket(publicKey.getBytes()));
					System.out.println("Actuator" + getId() + " : Has sent a key and waiting for a response");
					send(gatewayDeviceId, CloudSim.getMinTimeBetweenEvents(), FogEvents.KEY_REQUEST, tuple);
					break;
			}
		}
		/*
		End Code
		*/

	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
			case FogEvents.TUPLE_ACK:
				break;
			case FogEvents.TUPLE_ARRIVAL:
				processTupleArrival(ev);
				break;
			case FogEvents.KEY_ARRIVAL:
				//Process key arriving
				//If using symmetric decrypt the arriving key
				if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.EXCHANGESYMMETRIC){
					decryptionKey = new String(Cryptography.Decrypt((byte[]) ev.getData(),privateKey,EncryptionType.RSA));
					System.out.println("Actuator : " + getId() + " : Has received a key - " + decryptionKey);

				}else{
					//If using asymmetric just store the key used for digital signatures
					connectedDevicePublicKey = ev.getData().toString();
					System.out.println("Actuator : " + getId() + " : Has received a key - " + connectedDevicePublicKey);

				}
				break;
		}
	}

	private void processTupleArrival(SimEvent ev) {
		Tuple tuple = (Tuple)ev.getData();

		String srcModule = tuple.getSrcModuleName();
		String destModule = tuple.getDestModuleName();
		Application app = getApp();

		/*
		Ben Farmer Code
		 */

		if(CryptographyConfig.getCryptographyEnabled()){
			//If the cryptography is enabled then decrypt the packet on arrival using the correct type
			System.out.println("Actuator" + getId() + " : has received a encrypted tuple");

			if(tuple.getDataPacket().getEncryptionType() == EncryptionType.NONE){
			}
			else if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.STOREDSYMMETRIC){
				tuple.getDataPacket().setData(Cryptography.Decrypt(tuple.getDataPacket().getData(), decryptionKey,tuple.getDataPacket().encryptionType));
				tuple.getDataPacket().setEncryptionType(EncryptionType.NONE);
			}
			else if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.STOREDASYMMETRIC){
				tuple.getDataPacket().setData(Cryptography.Decrypt(tuple.getDataPacket().getData(), privateKey,tuple.getDataPacket().encryptionType));
				//Verify the data using the digital signature
				if(Cryptography.verify(tuple.getDataPacket().getData(), tuple.getDataPacket().getDigitalSignature(),Cryptography.getStoredAsymmetricPublicKey())){
					System.out.println(getName() + " : Has verified the digital signature");
				}else{
					System.out.println(getName() + " : Has failed to verify the digital signature");
				}
				tuple.getDataPacket().setEncryptionType(EncryptionType.NONE);

			}
			else if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.EXCHANGEAYSMMETRIC){
				tuple.getDataPacket().setData(Cryptography.Decrypt(tuple.getDataPacket().getData(), privateKey,tuple.getDataPacket().encryptionType));
				//Verify the data using the digital signature
				if(Cryptography.verify(tuple.getDataPacket().getData(), tuple.getDataPacket().getDigitalSignature(), connectedDevicePublicKey)){
					System.out.println(getName() + " : Has verified the digital signature");
				}
				else{
					System.out.println(getName() + " : Has failed to verify the digital signature");
				}
				tuple.getDataPacket().setEncryptionType(EncryptionType.NONE);

			}
			else if(CryptographyConfig.getKeyExchangeType() == KeyExchangeTypes.EXCHANGESYMMETRIC){
				tuple.getDataPacket().setData(Cryptography.Decrypt(tuple.getDataPacket().getData(), decryptionKey,tuple.getDataPacket().encryptionType));
				tuple.getDataPacket().setEncryptionType(EncryptionType.NONE);
			}

			System.out.println("Actuator " + getId() + " : Has decrypted a Tuple to - "  +  new String(tuple.getDataPacket().getData()));

		}else{
			System.out.println("Actuator received an unencrypted tuple");

		}

		/*
		End Code Ben Farmer
		 */

		for(AppLoop loop : app.getLoops()){
			if(loop.hasEdge(srcModule, destModule) && loop.isEndModule(destModule)){
				
				Double startTime = TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				if(startTime==null)
					break;
				if(!TimeKeeper.getInstance().getLoopIdToCurrentAverage().containsKey(loop.getLoopId())){
					TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), 0.0);
					TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), 0);
				}
				double currentAverage = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop.getLoopId());
				int currentCount = TimeKeeper.getInstance().getLoopIdToCurrentNum().get(loop.getLoopId());
				double delay = CloudSim.clock()- TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				TimeKeeper.getInstance().getEmitTimes().remove(tuple.getActualTupleId());
				double newAverage = (currentAverage*currentCount + delay)/(currentCount+1);
				TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), newAverage);
				TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), currentCount+1);
				break;
			}
		}
	}

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

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(String actuatorType) {
		this.actuatorType = actuatorType;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

}
