package de.holisticon.jdk9.http2.util;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Create an {@link SSLContext} and keep it as a singleton object.
 * 
 * @author janweinschenker
 *
 */
public class SSLContextCreator {

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(SSLContextCreator.class.getName());
    public static final String KEYSTORE_PASSWORD = "pass4711";
    public static final String SSL_VERSION = "SSL";
    public static final String ALGORITHM = "SunX509";
    public static final String KEYSTORE_TYPE = "JKS";
    public static final String KEYSTORE_FILE = "src/main/resources/http2_demo.jks";

    private static SSLContext theContext = null;

	public static void main(String[] args) {
		getContextInstance();
	}

	/**
	 * Return the instance of the {@link SSLContext}
	 * 
	 * @return
	 */
	public static SSLContext getContextInstance() {
		if (theContext == null) {
			theContext = createContext();
		}
		return theContext;
	}

	private static SSLContext createContext() {
		LOG.info("Testing socket factory with SSLContext:");
		try {
			// SSLContext protocols: TLS, SSL, SSLv3
			SSLContext sc = SSLContext.getInstance(SSL_VERSION);
			LOG.info("SSLContext class: " + sc.getClass());
			LOG.info("   Protocol: " + sc.getProtocol());
			LOG.info("   Provider: " + sc.getProvider());

			// SSLContext algorithms: SunX509
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
			LOG.info("KeyManagerFactory class: " + kmf.getClass());
			LOG.info("   Algorithm: " + kmf.getAlgorithm());
			LOG.info("   Provider: " + kmf.getProvider());

			// KeyStore types: JKS
			char ksPass[] = KEYSTORE_PASSWORD.toCharArray();
			char ctPass[] = KEYSTORE_PASSWORD.toCharArray();
			KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
			ks.load(getFileInputStream(), ksPass);
			LOG.info("KeyStore class: " + ks.getClass());
			LOG.info("   Type: " + ks.getType());
			LOG.info("   Provider: " + ks.getProvider());
			LOG.info("   Size: " + ks.size());

			// Generating KeyManager list
			kmf.init(ks, ctPass);
			KeyManager[] kmList = kmf.getKeyManagers();
			LOG.info("KeyManager class: " + kmList[0].getClass());
			LOG.info("   # of key manager: " + kmList.length);

			// Generating SSLServerSocketFactory
			sc.init(kmList, null, null);
			return sc;
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		return null;
	}

	private static InputStream getFileInputStream(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(KEYSTORE_FILE);
        return resourceAsStream;
    }

}
