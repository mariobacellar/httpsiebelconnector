package br.com.config;


/**
 * 
 * <ChaveSiebel>
 *   <NomeChave>ConfigSiebelExposicaoServicos</NomeChave>
 *   <URL>/framework/start.swe</URL>
 *   <Server>crmhw07.oi.corp.net</Server>
 *   <Port>80</Port>
 *   <SWEExtSource>OiFrmEAI</SWEExtSource>
 *   <MaxSessions>5</MaxSessions>
 *   <Timeout>35000</Timeout>
 *   <MaxInvalidate>1</MaxInvalidate>
 *   <UserName>pcsvitria</UserName>
 *   <Password>vitpcs</Password>
 *   <ConnectionTimeout>10000</ConnectionTimeout>
 *   <ReadTimeout>10000</ReadTimeout>
 *   <TTL_Minutes>30</TTL_Minutes>
 *   </ChaveSiebel>
 * </Chaves>
 */
public class ChaveSiebel {
	
	private String NomeChave;
	private String URL;
	private String Server;
	private String Port;
	private String SWEExtSource;
	private String MaxSessions;
	private String Timeout;
	private String MaxInvalidates;
	private String UserName;
	private String Password;
	private String ConnectionTimeout;
	private String ReadTimeout;

	public String getConnectionTimeout() {
		return ConnectionTimeout;
	}

	public void setConnectionTimeout(String connectionTimeout) {
		ConnectionTimeout = connectionTimeout;
	}

	public String getReadTimeout() {
		return ReadTimeout;
	}

	public void setReadTimeout(String readTimeout) {
		ReadTimeout = readTimeout;
	}

	public String getMaxInvalidates() {
		return MaxInvalidates;
	}

	public void setMaxInvalidates(String maxInvalidates) {
		MaxInvalidates = maxInvalidates;
	}

	public String getMaxSessions() {
		return MaxSessions;
	}

	public void setMaxSessions(String maxSessions) {
		MaxSessions = maxSessions;
	}

	public String getNomeChave() {
		return NomeChave;
	}

	public void setNomeChave(String nomeChave) {
		NomeChave = nomeChave;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getPort() {
		return Port;
	}

	public void setPort(String port) {
		Port = port;
	}

	public String getServer() {
		return Server;
	}

	public void setServer(String server) {
		Server = server;
	}

	public String getSWEExtSource() {
		return SWEExtSource;
	}

	public void setSWEExtSource(String extSource) {
		SWEExtSource = extSource;
	}

	public String getTimeout() {
		return Timeout;
	}

	public void setTimeout(String timeout) {
		Timeout = timeout;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String url) {
		URL = url;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

}
