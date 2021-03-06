package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;




public class Host {

	/** The hostid. */
	private String hostid;
	
	/** The host. */
	private String host;		//hostname
	
	/** The available. */
	private String available;
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new host.
	 */
	public Host(){
		
	}
	
	/**
	 * Gets the hostid.
	 *
	 * @return the hostid
	 */
	public String getHostid() {
		return hostid;
	}
	
	/**
	 * Sets the hostid.
	 *
	 * @param hostid the new hostid
	 */
	public void setHostid(String hostid) {
		this.hostid = hostid;
	}
	
	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Sets the host.
	 *
	 * @param host the new host
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Gets the available.
	 *
	 * @return the available
	 */
	public String getAvailable() {
		return available;
	}
	
	/**
	 * Sets the available.
	 *
	 * @param available the new available
	 */
	public void setAvailable(String available) {
		this.available = available;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
