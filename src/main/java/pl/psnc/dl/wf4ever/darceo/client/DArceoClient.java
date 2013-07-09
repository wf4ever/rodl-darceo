package pl.psnc.dl.wf4ever.darceo.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.darceo.utils.IO;
import pl.psnc.dl.wf4ever.preservation.client.RepositoryClient;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Provides methods accessing to Https clients that can authenticate via mutual SSL.
 */
public class DArceoClient implements RepositoryClient {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(DArceoClient.class);
    /** Singleton instance. */
    protected static RepositoryClient instance;
    /** Repository URL. */
    private static URI repositoryUri;
    /** Jersey client. */
    private Client client;
    private static String clientKeystore;
    private static char[] clientPassphrase;
    private static String serverKeystore;
    private static char[] serverPassphrase;


    /**
     * Constructor.
     * 
     * @throws DArceoException
     * @throws IOException
     */
    protected DArceoClient()
            throws DArceoException, IOException {
        loadProperties();
        try {
            setSSL(clientKeystore, clientPassphrase, serverKeystore, serverPassphrase);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
                | UnrecoverableKeyException | KeyManagementException e) {
            throw new DArceoException("Can't set SSL", e);
        }

        client = Client.create();
        client.setFollowRedirects(false);
    }


    protected static void loadProperties()
            throws IOException {
        Properties properties = new Properties();
        properties.load(DArceoClient.class.getClassLoader().getResourceAsStream("connection.properties"));
        repositoryUri = URI.create(properties.getProperty("repository_url"));
        loadSSLProperties();

    }


    protected static void loadSSLProperties()
            throws IOException {
        Properties properties = new Properties();
        properties.load(DArceoClient.class.getClassLoader().getResourceAsStream("connection.properties"));
        clientKeystore = properties.getProperty("client_keystore");
        clientPassphrase = properties.getProperty("client_passphrase").toCharArray();
        serverKeystore = properties.getProperty("server_keystore");
        serverPassphrase = properties.getProperty("server_passphrase").toCharArray();
    }


    /**
     * Check if dArceo configuration exists
     * 
     * @return
     * @throws IOException .
     */
    private static boolean propertiesExists()
            throws IOException {
        Properties properties = new Properties();
        properties.load(DArceoClient.class.getClassLoader().getResourceAsStream("connection.properties"));
        if ((properties.getProperty("repository_url") == null) || !sslPropertiesExists()) {
            return false;
        }
        return true;
    }


    /**
     * Check if ssl configuration exists
     * 
     * @return
     * @throws IOException .
     */
    private static boolean sslPropertiesExists()
            throws IOException {
        Properties properties = new Properties();
        properties.load(DArceoClient.class.getClassLoader().getResourceAsStream("connection.properties"));
        if ((properties.getProperty("client_keystore") == null)
                || (properties.getProperty("client_passphrase").toCharArray() == null)
                || (properties.getProperty("server_keystore") == null)
                || (properties.getProperty("server_passphrase").toCharArray() == null)) {
            return false;
        }
        return true;
    }


    protected static void setSSL(String clientKeystore, char[] clientPassphrase, String serverKeystore,
            char[] serverPassphrase)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        KeyStore serverStore = KeyStore.getInstance("JKS");

        clientStore.load(DArceoClient.class.getClassLoader().getResourceAsStream(clientKeystore), clientPassphrase);
        serverStore.load(DArceoClient.class.getClassLoader().getResourceAsStream(serverKeystore), serverPassphrase);

        KeyManagerFactory clientKeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        clientKeyManagerFactory.init(clientStore, clientPassphrase);
        TrustManagerFactory serverKeyManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
                .getDefaultAlgorithm());
        serverKeyManagerFactory.init(serverStore);

        SSLContext sslContextd = SSLContext.getInstance("SSL");
        sslContextd.init(clientKeyManagerFactory.getKeyManagers(), serverKeyManagerFactory.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContextd.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }


    /**
     * Get the instance of RepositoryClient.
     * 
     * @return RepositoryClient instance.
     * @throws IOException
     * @throws DArceoException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws UnrecoverableKeyException
     */
    public static RepositoryClient getInstance()
            throws DArceoException, IOException {
        if (instance == null) {
            if (!propertiesExists()) {
                LOGGER.warn("There is no preservations properties. Repository  will not be preserved");
                try {
                    if (sslPropertiesExists()) {
                        loadSSLProperties();
                        setSSL(clientKeystore, clientPassphrase, serverKeystore, serverPassphrase);

                    }
                } catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException
                        | NoSuchAlgorithmException | CertificateException | IOException e) {
                    throw new DArceoException(e.getMessage(), e);
                }
                instance = new BlankClient();
            } else {
                instance = new DArceoClient();
            }
        }
        return instance;
    }


    @Override
    public ResearchObjectSerializable getBlocking(URI id) {
        String idEncoded = null;
        try {
            //URLEncoder encodes " " as "+" but it's no problem if we give it a URI which is always encoded
            idEncoded = URLEncoder.encode(id.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("Unsupported encoding for " + id, e1);
        }
        WebResource webResource = client.resource(repositoryUri).path(idEncoded);
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() == HttpStatus.SC_OK) {
            return IO.toResearchObject(id, response.getEntityInputStream());
        } else if (response.getStatus() == HttpStatus.SC_ACCEPTED) {
            webResource = client.resource(response.getLocation().toString());
            response = webResource.get(ClientResponse.class);
            while (response.getStatus() == HttpStatus.SC_OK) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.warn("Sleep interrupted", e);
                }
                response = webResource.get(ClientResponse.class);
            }
            if (response.getStatus() == HttpStatus.SC_SEE_OTHER) {
                webResource = client.resource(response.getLocation().toString());
                return IO.toResearchObject(id, webResource.get(ClientResponse.class).getEntityInputStream());
            }
        } else if (response.getStatus() == HttpStatus.SC_NOT_FOUND) {
            return null;
        }
        throw new RuntimeException(new DArceoException("Unexpected return code: " + response.getClientResponseStatus()));
    }


    @Override
    public URI delete(URI id) {
        String idEncoded = null;
        try {
            //URLEncoder encodes " " as "+" but it's no problem if we give it a URI which is always encoded
            idEncoded = URLEncoder.encode(id.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("Unsupported encoding for " + id, e1);
        }
        WebResource webResource = client.resource(repositoryUri).path(idEncoded);
        ClientResponse response = webResource.delete(ClientResponse.class);
        if (response.getStatus() == HttpStatus.SC_ACCEPTED) {
            webResource = client.resource(response.getLocation().toString());
            return webResource.getURI();
        } else if (response.getStatus() == HttpStatus.SC_NOT_FOUND) {
            return null;
        }
        throw new RuntimeException(new DArceoException("Unexpected return code: " + response.getClientResponseStatus()));
    }


    @Override
    public URI postORUpdateBlocking(URI status) {
        WebResource webResource = client.resource(status.toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        while (response.getStatus() == HttpStatus.SC_OK) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn("Sleep interrupted", e);
            }
            webResource = client.resource(status.toString());
            response = webResource.get(ClientResponse.class);
        }
        //POST Test
        if (response.getStatus() == HttpStatus.SC_SEE_OTHER) {
            webResource = client.resource(response.getLocation().toString());
            return URI.create(webResource.get(String.class));
        }
        throw new RuntimeException(new DArceoException("Unexpected return code: " + response.getClientResponseStatus()));
    }


    @Override
    //FIXME get is synchronous, delete is asynchronous but deleteWait is synchronous. I think the convention would be to call them getBlocking, delete and deleteBlocking
    public boolean deleteBlocking(URI status) {
        WebResource webResource = client.resource(status);
        ClientResponse response = webResource.get(ClientResponse.class);
        while (response.getStatus() == HttpStatus.SC_OK) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn("Sleep interrupted", e);
            }
            response = webResource.get(ClientResponse.class);
        }
        if (response.getStatus() == HttpStatus.SC_SEE_OTHER) {
            webResource = client.resource(response.getLocation());
            response = webResource.get(ClientResponse.class);
            if (response.getStatus() == HttpStatus.SC_OK) {
                return true;
            }
        } else if (response.getStatus() == HttpStatus.SC_NOT_FOUND) {
            return false;
        }
        throw new RuntimeException(new DArceoException("Unexpected return code: " + response.getClientResponseStatus()));
    }


    @Override
    public URI post(ResearchObjectSerializable researchObject) {
        WebResource webResource = client.resource(repositoryUri);
        ClientResponse response = webResource.type("application/zip").post(ClientResponse.class,
            IO.toZipInputStream(researchObject));
        return response.getLocation();
    }


    @Override
    public URI update(ResearchObjectSerializable researchObject) {
        WebResource webResource;
        try {
            webResource = client.resource(repositoryUri.resolve(URLEncoder.encode(researchObject.getUri().toString(),
                "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Can't encode " + researchObject.getUri().toString() + " to update ro in dArce", e);
            return null;
        }
        ClientResponse response = webResource.type("application/zip").post(ClientResponse.class,
            IO.toZipInputStream(researchObject));
        return response.getLocation();
    }


    @Override
    public URI getServiceUri() {
        return repositoryUri;
    }
}
