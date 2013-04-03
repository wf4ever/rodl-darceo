package pl.psnc.dl.wf4ever.darceo.client;

import java.io.IOException;
import java.io.InputStream;
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
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

    //TODO replace or deprecated method on ... ?

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(TestDArceoClient.class);
    /** Singleton instance. */
    protected static DArceoClient instance;
    /** Repository URL. */
    private static URI repositoryUri;
    /** Jersey client. */
    private Client client;
    private String clientKeystore;
    private char[] clientPassphrase;
    private String serverKeystore;
    private char[] serverPassphrase;


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


    protected void loadProperties()
            throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("connection.properties"));
        repositoryUri = URI.create(properties.getProperty("repository_url"));
        clientKeystore = properties.getProperty("client_keystore");
        clientPassphrase = properties.getProperty("client_passphrase").toCharArray();
        serverKeystore = properties.getProperty("server_keystore");
        serverPassphrase = properties.getProperty("server_passphrase").toCharArray();
    }


    protected void setSSL(String clientKeystore, char[] clientPassphrase, String serverKeystore, char[] serverPassphrase)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        KeyStore serverStore = KeyStore.getInstance("JKS");

        clientStore.load(getClass().getClassLoader().getResourceAsStream(clientKeystore), clientPassphrase);
        serverStore.load(getClass().getClassLoader().getResourceAsStream(serverKeystore), serverPassphrase);

        KeyManagerFactory clientKeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        clientKeyManagerFactory.init(clientStore, clientPassphrase);
        TrustManagerFactory serverKeyManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
                .getDefaultAlgorithm());
        serverKeyManagerFactory.init(serverStore);

        SSLContext sslContextd = SSLContext.getInstance("SSL");
        sslContextd.init(clientKeyManagerFactory.getKeyManagers(), serverKeyManagerFactory.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContextd.getSocketFactory());
    }


    /**
     * Get the instance of RepositoryClient.
     * 
     * @return RepositoryClient instance.
     * @throws IOException
     * @throws DArceoException
     */
    public static DArceoClient getInstance()
            throws DArceoException, IOException {
        if (instance == null) {
            return new DArceoClient();
        }
        return instance;
    }


    @Override
    public InputStream get(URI id) {
        String idEncoded = null;
        try {
            //URLEncoder encodes " " as "+" but it's no problem if we give it a URI which is always encoded
            idEncoded = URLEncoder.encode(id.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("Unsupported encoding for " + id, e1);
        }
        WebResource webResource = client.resource(repositoryUri).path(idEncoded);
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() == 200) {
            return response.getEntityInputStream();
        } else if (response.getStatus() == 202) {
            webResource = client.resource(response.getLocation().toString());
            response = webResource.get(ClientResponse.class);
            while (response.getStatus() == 200) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.warn("Sleep interrupted", e);
                }
                response = webResource.get(ClientResponse.class);
            }
            if (response.getStatus() == 303) {
                webResource = client.resource(response.getLocation().toString());
                return webResource.get(ClientResponse.class).getEntityInputStream();
            }
        }
        //FIXME shouldn't we log the response if it was unexpected? Or throw an exception instead of returning null?
        return null;
    }


    @Override
    public URI post(ResearchObjectSerializable researchObject) {
        WebResource webResource = client.resource(repositoryUri);
        ClientResponse response = webResource.type("application/zip").post(ClientResponse.class,
            IO.toZipInputStream(researchObject));
        return response.getLocation();
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
        //        throw new DArceoException("Unexpected return code: " + response.getClientResponseStatus());
        return null;
    }


    @Override
    public URI postWait(URI status) {
        WebResource webResource = client.resource(status.toString());
        ClientResponse response = webResource.get(ClientResponse.class);

        while (response.getStatus() == 200) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn("Sleep interrupted", e);
            }
            webResource = client.resource(status.toString());
            response = webResource.get(ClientResponse.class);
        }
        //POST Test
        if (response.getStatus() == 303) {
            webResource = client.resource(response.getLocation().toString());
            return URI.create(webResource.get(String.class));
        }
        return null;
    }


    @Override
    //FIXME get is synchronous, delete is asynchronous but deleteWait is synchronous. I think the convention would be to call them getBlocking, delete and deleteBlocking
    public boolean deleteWait(URI status) {
        WebResource webResource = client.resource(status);
        ClientResponse response = webResource.get(ClientResponse.class);
        while (response.getStatus() == 200) {
            //FIXME Thread.sleep()
            response = webResource.get(ClientResponse.class);
        }
        if (response.getStatus() == 303) {
            webResource = client.resource(response.getLocation());
            response = webResource.get(ClientResponse.class);
            if (response.getStatus() == 200) {
                return true;
            }

        }
        //FIXME what does "false" mean? better throw an exception
        return false;
    }
}
