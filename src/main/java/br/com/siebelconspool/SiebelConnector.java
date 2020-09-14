package br.com.siebelconspool;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import br.com.config.ChaveSiebel;

public class SiebelConnector {
	
	private static final String OCORREU_ERRO_500_NO_SIEBEL_NA_HORA_DE_LOGIN = "Ocorreu erro 500 no Siebel na hora de login";
	private static final String OCORREU_ERRO_500_NO_SIEBEL_NA_HORA_DE_LOGOFF = "Ocorreu erro 500 no Siebel na hora de logoff";
	private static final String POST = "POST";
	private static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE = "Cookie";
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String DES_CONECTANDO_SIEBEL = SiebelLoginSpool.idClass + "DesConectando Siebel - ";
	private static final String CONECTANDO_SIEBEL = SiebelLoginSpool.idClass + "Conectando Siebel - ";
	private static final String LOGIN = "LOGIN";
	private static final String LOGOFF = "LOGOFF";
	private static final String MSGENVIO = "SWEExtSource=%s&SWEExtCmd=ExecuteLogin&UserName=%s&Password=%s";
	private static final String MSGLOGOFF = "SWEExtSource=%s&SWEExtCmd=Logoff&UserName=%s&Password=%s";
	private static final String URLFORMAT = "http://%s:%s%s";
	private static final String MSGLOG = SiebelLoginSpool.idClass + "Conseguiu alocar a sess�o: %s \n";
	private static final String MSGLOG2 = SiebelLoginSpool.idClass + "Server: %s  |  Porta: %s \n";
	
	private ChaveSiebel chave;
	private Logger log = Logger.getLogger(getClass());
	
	@SuppressWarnings("static-access") 
	protected String getSession() throws Exception{ 
		
		log.debug(CONECTANDO_SIEBEL.concat(LOGIN));
		HttpURLConnection conn = getConnection();
		String envio = MSGENVIO.format(MSGENVIO, chave.getSWEExtSource(), chave.getUserName(), chave.getPassword());
		conn.setRequestProperty(CONTENT_LENGTH, String.valueOf(envio.length()));
		conn.setRequestProperty(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(envio);   
        wr.flush(); 
        
        log.debug(DES_CONECTANDO_SIEBEL.concat(LOGIN));
		
        if (conn.getResponseCode() !=  200)
        {
        	log.error(SiebelLoginSpool.idClass + OCORREU_ERRO_500_NO_SIEBEL_NA_HORA_DE_LOGIN);
            wr.close();
            conn.disconnect();
        	throw new Exception(OCORREU_ERRO_500_NO_SIEBEL_NA_HORA_DE_LOGIN); 
        }
        String strSession = conn.getHeaderField(SET_COOKIE); 
        log.debug(MSGLOG.format(MSGLOG,strSession));
        wr.close();
        conn.disconnect();
        return strSession;
	}
	
	@SuppressWarnings("static-access")
	protected void logOff(String Session) throws Exception{ 
		log.debug(CONECTANDO_SIEBEL.concat(LOGOFF));
		HttpURLConnection conn = getConnection();
		String envio = MSGLOGOFF.format(MSGLOGOFF, chave.getSWEExtSource(), chave.getUserName(), chave.getPassword());
		conn.setRequestProperty(CONTENT_LENGTH, String.valueOf(envio.length()));
		conn.setRequestProperty(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
		conn.setRequestProperty(COOKIE, Session);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(envio);   
        wr.flush(); 
        
        log.debug(DES_CONECTANDO_SIEBEL.concat(LOGOFF));
		
        if (conn.getResponseCode() !=  200)
        {
        	log.error(SiebelLoginSpool.idClass + OCORREU_ERRO_500_NO_SIEBEL_NA_HORA_DE_LOGOFF);
        }
        wr.close();
        conn.disconnect();
	}
	
	
	@SuppressWarnings("static-access")
	private HttpURLConnection getConnection() throws Exception{
		
		URLConnection conn = null;
		HttpURLConnection connection = null;
	      try {
	    	 URL url = new URL(URLFORMAT.format(URLFORMAT,chave.getServer(),chave.getPort(),chave.getURL()));
	    	 log.debug(MSGLOG2.format(MSGLOG2, chave.getServer(), chave.getPort()));
	    	 conn = url.openConnection();
	         connection = (HttpURLConnection) conn;
	         connection.setConnectTimeout(Integer.parseInt(chave.getConnectionTimeout()));
	         connection.setReadTimeout(Integer.parseInt(chave.getReadTimeout()));
	         connection.setDoOutput(true);
	         connection.setDoInput(true);
	         connection.setRequestMethod(POST);	         	         
	      }
	      catch(Exception e){
	         throw e;
	      }
	      return connection;
	}

	@SuppressWarnings("static-access")
	public String getURL() throws Exception{
		  return URLFORMAT.format(URLFORMAT,chave.getServer(),chave.getPort(),chave.getURL());
	    
	}
	
	
	protected void setChave(ChaveSiebel chave) {
		this.chave = chave;
	}	
	

	

}
