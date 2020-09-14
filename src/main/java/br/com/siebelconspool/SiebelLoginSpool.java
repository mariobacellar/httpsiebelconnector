package br.com.siebelconspool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import br.com.config.ChaveFactory;
import br.com.config.ChaveSiebel;

public class SiebelLoginSpool {
	
	private static final String ERRO_CARREGANDO_ARQUIVO_CONFIGURACAO = "Erro carregando arquivo de configuração";
	private static final String CRIACAO_DO_SIEBEL_LOGIN_SPOOL        = "-------- Criação do SiebelLoginSpool --------";

//	private static final String LOG4JXML                             = "/webaplic/resources/SiebelLoginSpool/SiebelLoginSpoollog4j.xml";
	private static final String LOG4JXML                             = "./src/main/resources/SiebelLoginSpoollog4j.xml";
//	private static final String CONFIGFILEPATH                       = "/webaplic/resources/SiebelLoginSpool/SiebelLoginSpool.xml";
	private static final String CONFIGFILEPATH                       = "./src/main/resources/SiebelLoginSpool.xml";

	public  static       String idClass;
	private static       String CHAVE_SIEBEL_NAO_ENCONTRADA = null;
	private static       String CRIOU_POOL_PELA_CHAVE		= null;
	private static       String PEGOU_POOL_PELA_CHAVE		= null;
	
	private static SiebelLoginSpool instance = new SiebelLoginSpool();		
	public  static SiebelLoginSpool getInstance() { return instance; }
	
	
	private Logger log = Logger.getLogger(getClass());

	private HashMap	chavesSiebel;
	private HashMap	siebelPool 	= new HashMap();	

	private ChaveFactory	chavefactory	= null;
	 
	
	/**
	 * 
	 */
	public SiebelLoginSpool(){
		
		//Iniciando a Configuracao do LOG4J
		DOMConfigurator.configureAndWatch(LOG4JXML);
		
		//Criando um número randomico para representar instancia do gerenciador de Pool
		Random randomid = new Random(System.currentTimeMillis());  
		idClass = "(" + String.valueOf(randomid.nextInt(999999999)) + ") - ";
		log.info("idClass = [" +idClass+CRIACAO_DO_SIEBEL_LOGIN_SPOOL+"]");
		
		//Inicializando as variaveis estaticas que possuem os textos utilizados no log
		CHAVE_SIEBEL_NAO_ENCONTRADA = idClass + "Chave Siebel informada, nao foi encontrada";
		CRIOU_POOL_PELA_CHAVE       = idClass + "Criou pool pela chave: %s";
		PEGOU_POOL_PELA_CHAVE       = idClass + "Pegou pool pela chave: %s";
		
		//Instanciando a classe que recupera a configuracao dos servidores Siebel
		chavefactory = new ChaveFactory(CONFIGFILEPATH);
		carregaConfig();
	}
	
	@SuppressWarnings("unchecked")
	private SiebelConnectorPool getPool(String nomeChave) throws Exception{
		
		SiebelConnectorPool retorno = null;
		
		synchronized (siebelPool) {
			
			// Cria novo pool para chave inexistente
			if (siebelPool.containsKey(nomeChave)){ 
				
				log.debug(PEGOU_POOL_PELA_CHAVE.format(PEGOU_POOL_PELA_CHAVE,nomeChave));
				retorno = (SiebelConnectorPool) siebelPool.get(nomeChave);
				
			}else{
				
				if (chavesSiebel.containsKey(nomeChave)){
					log.debug(CRIOU_POOL_PELA_CHAVE.format(CRIOU_POOL_PELA_CHAVE, nomeChave));
					ChaveSiebel cs = (ChaveSiebel) chavesSiebel.get(nomeChave);
					SiebelConnectorPool scp = new SiebelConnectorPool(cs);
					siebelPool.put(nomeChave, scp);
					retorno = scp;
				}
			}
		}
		return retorno;
	}
	
	private void carregaConfig(){
		/*
		 * Recarrega as chaves do arquivo de configuracao se o arquivo foi atualizado.
		 * Essa operação destroi todos os pools existentes.
		 */
		if (chavefactory.isNewer()){
			try {
				chavesSiebel = chavefactory.retornaChaves();
				HashMap cl = (HashMap) siebelPool;
				for (Iterator iter = cl.values().iterator(); iter.hasNext();) {
					SiebelConnectorPool element = (SiebelConnectorPool) iter.next();
					element.destroy();
				}
				siebelPool = new HashMap();				
			} catch (Exception e) {
				log.error(ERRO_CARREGANDO_ARQUIVO_CONFIGURACAO);
			}			
		}
	}
	
	/*
	 * Metodo que retorna uma sessao Siebel
	 */
	protected String getSession(String nomeChave, boolean force) throws Exception{
		String session = null;
		carregaConfig(); //Mando carregar ou recarregar as chaves do arquivo xml
		if (!chavesSiebel.containsKey(nomeChave)){
			log.error(CHAVE_SIEBEL_NAO_ENCONTRADA+" : "+nomeChave);
			throw new Exception(CHAVE_SIEBEL_NAO_ENCONTRADA);
		}
		else {
			session = getPool(nomeChave).getSession(force);
		}
		return session;
	}
	
	/*
	 * Metodo que libera uma sessï¿½o siebel alocada.  
	 * Agora ela pode ser reutilizada pelo metodo getSession
	 */
	protected void setFreeSession(String nomeChave, String SessionId) throws Exception{
		if (!chavesSiebel.containsKey(nomeChave)){
			log.error(CHAVE_SIEBEL_NAO_ENCONTRADA+" : "+nomeChave);
		}
		else {
			getPool(nomeChave).freeSession(SessionId);
		}

	}
	
	/*
	 * Metodo que recupera a URL do siebel para a chave passada.  
	 * 
	 */
	protected String getURLSession(String nomeChave) throws Exception{
		if (!chavesSiebel.containsKey(nomeChave)){
			log.error(CHAVE_SIEBEL_NAO_ENCONTRADA+" : "+nomeChave);
		}else {
			return getPool(nomeChave).getURL();
		}
		return null;
	}
	
	/*
	 * Metodo que invalida uma sessao.  A sessao ï¿½ removida do pool e descartada.
	 */
	protected void setInvalidateSession(String nomeChave, String SessionId) throws Exception{
		if (!chavesSiebel.containsKey(nomeChave)){
			log.error(CHAVE_SIEBEL_NAO_ENCONTRADA+" : "+nomeChave);
		}
		else {
			getPool(nomeChave).invalidateSession(SessionId);
		}
	}
	
	protected String[] getActualSessions(String nomeChave) throws Exception{
		if (!chavesSiebel.containsKey(nomeChave)){
			log.error(CHAVE_SIEBEL_NAO_ENCONTRADA+" : "+nomeChave);			
		}
		return getPool(nomeChave).getStatusSessoes();
	}
	
	protected String[] getContadores() throws Exception{
		carregaConfig(); //Mando carregar ou recarregar as chaves do arquivo xml
		String retorno[] = new String[siebelPool.size()];
		int c=0;
		for (Iterator iter = siebelPool.values().iterator(); iter.hasNext();) {
			SiebelConnectorPool pool = (SiebelConnectorPool) iter.next();
			retorno[c++]= pool.getContadores();
		}		
		return retorno;
	}

	public static void		InvalidateSession	(String nomeChave, String SessionId)	throws Exception{ getInstance().setInvalidateSession(nomeChave,SessionId);}
	public static void		FreeSession      	(String nomeChave, String SessionId)	throws Exception{ getInstance().setFreeSession      (nomeChave, SessionId);}
	
	public static String	AllocSession		(String nomeChave)						throws Exception{ return getInstance().getSession(nomeChave, false);}
	public static String	ForceAllocSession	(String nomeChave)                      throws Exception{ return getInstance().getSession(nomeChave, true);	}
	public static String[]	ActualSessions		(String nomeChave)	                    throws Exception{ return getInstance().getActualSessions(nomeChave);	}
	public static String[]	Contadores			() 					                    throws Exception{ return getInstance().getContadores();	}
	public static String	getUrl				(String nomeChave)                      throws Exception{ return getInstance().getURLSession(nomeChave); }

	
	public static void main(String[] args) {
		try {
			
			String		nomeChave 			= "ConfigSiebelExposicaoServicos";
			String		allocSession		= SiebelLoginSpool.AllocSession(nomeChave);
			String		forceAllocSession	= SiebelLoginSpool.ForceAllocSession(nomeChave);
			String[]	actualSessions		= SiebelLoginSpool.ActualSessions(nomeChave);
			String[]	contadores			= SiebelLoginSpool.Contadores();
			String		url					= SiebelLoginSpool.getUrl(nomeChave);

			System.out.println("nomeChave...........= "+nomeChave			+"]");
			System.out.println("allocSession........= "+allocSession		+"]");
			System.out.println("forceAllocSession...= "+forceAllocSession	+"]");
			System.out.println("url.................= "+url					+"]");

			for (String string : contadores) {
				System.out.println("contadores..........= "+string			+"]");
			}

			for (String string : actualSessions) {
				System.out.println("actualSessions......= "+string		+"]");					
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

