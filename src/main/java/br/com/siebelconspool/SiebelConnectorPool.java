package br.com.siebelconspool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.config.ChaveSiebel;

public class SiebelConnectorPool{
	private static final String REINICIANDO_POR_EXCESSO_INVALIDACOES = SiebelLoginSpool.idClass + "Reiniciando por excesso de Invalida��es o POOL : ";
	private static final String DESTRUINDO_POOL = SiebelLoginSpool.idClass + "Destruindo o POOL : ";
	private static final String LOGOFF_SESSAO = SiebelLoginSpool.idClass + "LOGOFF da sess�o : ";
	private static final String VAI_LANCAR_EXCECAO = SiebelLoginSpool.idClass + "Vai lan�ar exce��o";
	private static final String CONSEGUIU_LIBERAR_SESSOES = SiebelLoginSpool.idClass + "Conseguiu liberar sess�es";
	private static final String EXPIROU_SESSAO = SiebelLoginSpool.idClass + "Expirou a sess�o: %s";
	private static final String INVALIDANDO_SESSAO = SiebelLoginSpool.idClass + "Invalidando a sess�o: %s";
	private static final String LIBERANDO_A_SESSAO = SiebelLoginSpool.idClass + "Liberando a sess�o: %s";
	private static final String NOVA_SESSAO = SiebelLoginSpool.idClass + "Criando nova sess�o";
	private static final String LIMITE_POOL_ATINGIDO = SiebelLoginSpool.idClass + "Limite m�ximo do pool atingido";
	private static final String PEGANDO_SESSAO_IDLE = SiebelLoginSpool.idClass + "Pegando a sess�o no Idle";
	private List IdlePool = null;
	private HashMap ActivePool = null;
	private SiebelConnector con;
	private ChaveSiebel Chave;
	private int TotalSession = 0;
	private int MaxSession = 0;
	private int MaxInvalidate = 0;
	private int TotalInvalidate = 0;
	private Logger log = Logger.getLogger(getClass());
	
	public SiebelConnectorPool(ChaveSiebel chave){
		con = new SiebelConnector();
		con.setChave(chave);
		MaxSession = Integer.parseInt(chave.getMaxSessions()); //Guardando o m�ximo de sess�es que o pool suportar�.
		MaxInvalidate = Integer.parseInt(chave.getMaxInvalidates()); //Guardando o m�ximo de Invalida��es que podem ocorrer.
		IdlePool = new ArrayList(); //Iniciando a lista de Sessions disponiveis
		ActivePool = new HashMap(); //Iniciando o hash de sessions em uso 
		Chave = chave; //Classe que detem as informa
	}
	
	@SuppressWarnings("unchecked")
	public String getURL() throws Exception{		
		return con.getURL();
	}

	@SuppressWarnings("unchecked")
	public String getSession(boolean force) throws Exception{		
		return getSession(null, force);
	}
	
	@SuppressWarnings("unchecked")
	private String getSession(SiebelSession sessionCriada, boolean force) throws Exception{
		
		SiebelSession session = null;
		boolean novaSessao = force;
		
		synchronized (this) {
			if (!novaSessao){
				if (sessionCriada != null && TotalSession < MaxSession) {
					++TotalSession;
					setActive(sessionCriada);						
					log.debug(SiebelLoginSpool.idClass + "APOS CRIACAO - TotalSession: "+String.valueOf(TotalSession)+"  |  MaxSession: "+String.valueOf(MaxSession)+"  |  Idle: "+String.valueOf(IdlePool.size())+"  |  Active: "+String.valueOf(ActivePool.size()));
					return sessionCriada.getSession();
				}
				
				if (IdlePool.size() > 0){
					session = getIdleSession();
					setActive(session);
				}
				else {
					//log.debug(SiebelLoginSpool.idClass + "ANTES IF COMPAR - TotalSession: "+String.valueOf(TotalSession)+"  |  MaxSession: "+String.valueOf(MaxSession)+"  |  Idle: "+String.valueOf(IdlePool.size())+"  |  Active: "+String.valueOf(ActivePool.size()));
					if (TotalSession >=  MaxSession){
						if (liberaExpirados() > 0) {
							session = getIdleSession();
							setActive(session);
							log.debug(CONSEGUIU_LIBERAR_SESSOES);
						}
						else {
							log.debug(VAI_LANCAR_EXCECAO);
							throw new Exception(LIMITE_POOL_ATINGIDO);
						}
					}
					else {					
						novaSessao = true;
					}
				}
			}
		}	
		/*
		 * A cria��o de uma nova sess�o � feita fora do synchronized, para que 
		 * o desempenho seja melhor.
		 */
		if (novaSessao){
			log.debug(NOVA_SESSAO);			
			session = new SiebelSession(con.getSession());	 		
			return getSession(session, false);
			
		}		
		log.info(SiebelLoginSpool.idClass +"Sess�o Alocada - TotalSession: "+String.valueOf(TotalSession)+"  |  MaxSession: "+String.valueOf(MaxSession)+"  |  Idle: "+String.valueOf(IdlePool.size())+"  |  Active: "+String.valueOf(ActivePool.size()));
		return session.getSession();
	}

	private SiebelSession getIdleSession() {
		SiebelSession session;
		log.debug(PEGANDO_SESSAO_IDLE);
		session = (SiebelSession) IdlePool.get(0);
		IdlePool.remove(0);
		return session;
	}

	@SuppressWarnings("unchecked")
	private void setActive(SiebelSession session) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, Integer.parseInt(Chave.getTimeout()));
		session.setLive(cal);
		ActivePool.put(session.getSession(), session);
	}
	
	@SuppressWarnings("unchecked")
	protected void freeSession(String SessionId) throws Exception{
		synchronized (this) {
			log.debug(LIBERANDO_A_SESSAO.format(LIBERANDO_A_SESSAO, SessionId));
			if (ActivePool.containsKey(SessionId)){
				IdlePool.add(ActivePool.remove(SessionId));
				TotalInvalidate = 0;
			}
			log.debug(SiebelLoginSpool.idClass + "APOS LIBERACAO DE SESSAO:  TotalSession: "+String.valueOf(TotalSession)+"  |  MaxSession: "+String.valueOf(MaxSession)+"  |  Idle: "+String.valueOf(IdlePool.size())+"  |  Active: "+String.valueOf(ActivePool.size()));
		}
	}
	
	protected void invalidateSession(String SessionId) throws Exception{
		synchronized (this) {
			log.debug(INVALIDANDO_SESSAO.format(INVALIDANDO_SESSAO, SessionId));
			if (ActivePool.remove(SessionId) != null){
				--TotalSession;
				con.logOff(SessionId);
				log.debug(LOGOFF_SESSAO+SessionId);
				if (TotalInvalidate >= MaxInvalidate) {
					log.info(REINICIANDO_POR_EXCESSO_INVALIDACOES+Chave.getNomeChave());
					log.debug(SiebelLoginSpool.idClass + "ANTES DE REINICIAR:  TotalSession: "+String.valueOf(TotalSession)+"  |  MaxSession: "+String.valueOf(MaxSession)+"  |  Idle: "+String.valueOf(IdlePool.size())+"  |  Active: "+String.valueOf(ActivePool.size()));
					TotalInvalidate = 0;
					destroy();					
					log.debug(SiebelLoginSpool.idClass + "DEPOIS DE REINCIAR:  TotalSession: "+String.valueOf(TotalSession)+"  |  MaxSession: "+String.valueOf(MaxSession)+"  |  Idle: "+String.valueOf(IdlePool.size())+"  |  Active: "+String.valueOf(ActivePool.size()));
				}
				else {
					++TotalInvalidate;
				}				
			}	
		}
	}
	
	protected void destroy() throws Exception{
		synchronized(this){
			log.info(DESTRUINDO_POOL+Chave.getNomeChave());
			for (Iterator iter = IdlePool.iterator(); iter.hasNext();) {
				SiebelSession element = (SiebelSession) iter.next();				
				con.logOff(element.getSession());
				log.debug(LOGOFF_SESSAO+element.getSession());				
			}
			TotalSession -= IdlePool.size();
			IdlePool.clear();
		}
	}
	
	private int liberaExpirados() throws Exception{
		Calendar now = Calendar.getInstance();
		int total = 0;
		HashMap cl = (HashMap) ActivePool.clone();
		for (Iterator iter = cl.values().iterator(); iter.hasNext();) {
			SiebelSession element = (SiebelSession) iter.next();
			if (now.after(element.getLive())){
				log.debug(EXPIROU_SESSAO.format(EXPIROU_SESSAO, element.getSession()));
				freeSession(element.getSession());
				++total;
			}	
		}		
		return total;
	}
	
	
	
	protected String getContadores(){
		String retorno = String.format("%s,%d,%d,%d,%d",Chave.getNomeChave(),TotalSession,MaxSession,IdlePool.size(),ActivePool.size());
		return retorno;
	}
	
	protected String[] getStatusSessoes(){
		String[] retorno = new String[TotalSession];
		int count = 0;
		synchronized(this){			
			for (Iterator iter = ActivePool.values().iterator(); iter.hasNext();) {
				SiebelSession element = (SiebelSession) iter.next();
				retorno[count++] = String.format("%s,ATIVA",element.Session);
			}
			for (Iterator iter = IdlePool.iterator(); iter.hasNext();) {
				SiebelSession element = (SiebelSession) iter.next();
				retorno[count++] = String.format("%s,DISPONIVEL",element.Session);
			}
		}
		
		return retorno;
	}
	
	/*
	 * Classe que representa um sess�o.
	 * Ela serve para agregar um tempo de vida na sess�o.
	 * Para que a mesma possa ser recuperada.
	 */
	private class SiebelSession{
		private String Session = null;
		private Calendar Live = null;		
		
		public void setLive(Calendar live) {
			Live = live;
		}

		public SiebelSession(String session){
			Session = session;
		}

		public Calendar getLive() {
			return Live;
		}

		public String getSession() {
			return Session;
		}
	}	
}
