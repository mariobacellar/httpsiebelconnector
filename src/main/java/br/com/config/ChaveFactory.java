package br.com.config;

import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChaveFactory {

	private static final String READ_TIMEOUT = "ReadTimeout";
	private static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
	private static final String PASSWORD = "Password";
	private static final String USER_NAME = "UserName";
	private static final String TIMEOUT = "Timeout";
	private static final String MAX_SESSIONS = "MaxSessions";
	private static final String MAX_INVALIDATE = "MaxInvalidate";
	private static final String SWEEXT_SOURCE = "SWEExtSource";
	private static final String PORT = "Port";
	private static final String SERVER = "Server";
	private static final String URL = "URL";
	private static final String NOME_CHAVE = "NomeChave";
	private static final String CHAVE_SIEBEL = "ChaveSiebel";
	private String endArquivo = null;
	private long ultimamodificacao = 0L;
	private long ultimacarga = 0L;

	public ChaveFactory(String Arquivo) {
		this.endArquivo = Arquivo;
	}

	public boolean isNewer() {
		if (this.ultimacarga > System.currentTimeMillis()) {
			return false;
		}
		this.ultimacarga = (System.currentTimeMillis() + 120000L);
		File file = new File(this.endArquivo);
		return file.lastModified() > this.ultimamodificacao;
	}

	public HashMap<String, ChaveSiebel> retornaChaves() throws Exception {
		HashMap<String, ChaveSiebel> retorno = carregaXMLCfg(this.endArquivo);
		return retorno;
	}

	private HashMap<String, ChaveSiebel> carregaXMLCfg(String arq) throws Exception {
		
		HashMap<String, ChaveSiebel> retorno = new HashMap<String, ChaveSiebel>();
		
		File file = new File(arq);
		this.ultimamodificacao = file.lastModified();
		
		DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder			db = dbf.newDocumentBuilder();
		
		Document
		doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		NodeList nodeLst = doc.getElementsByTagName("ChaveSiebel");
		for (int s = 0; s < nodeLst.getLength(); s++) {

			Node fstNode = nodeLst.item(s);
			
			if (fstNode.getNodeType() == 1) {
			
				
				Element		fstElmnt		= (Element) fstNode;
				NodeList	fstNmElmntLst	= fstElmnt.getElementsByTagName("NomeChave");
				Element		fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				NodeList	fstNm			= fstNmElmnt.getChildNodes();
				
				ChaveSiebel 
				chave = new ChaveSiebel();
				chave.setNomeChave(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("URL");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setURL(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("Server");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setServer(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("Port");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setPort(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("SWEExtSource");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setSWEExtSource(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("MaxSessions");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setMaxSessions(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("Timeout");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setTimeout(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("MaxInvalidate");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setMaxInvalidates(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("UserName");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setUserName(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("Password");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setPassword(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("ConnectionTimeout");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setConnectionTimeout(fstNm.item(0).getNodeValue());

				fstNmElmntLst	= fstElmnt.getElementsByTagName("ReadTimeout");
				fstNmElmnt		= (Element) fstNmElmntLst.item(0);
				fstNm			= fstNmElmnt.getChildNodes();
				chave.setReadTimeout(fstNm.item(0).getNodeValue());

				retorno.put(chave.getNomeChave(), chave);
			}
		}
		this.ultimacarga = (System.currentTimeMillis() + 120000L);
		return retorno;
	}
}
