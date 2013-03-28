package pl.psnc.dl.wf4ever.darceo.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;
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
    private static final Logger LOGGER = Logger.getLogger(TestDArceoClient.class);
    /** Singleton instance. */
    protected static RepositoryClient instance;
    /** Repository url. */
    private static URI repositoryUri;
    /** Jersay client. */
    private Client client;


    /**
     * Constructor.
     */
    protected DArceoClient() {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("connection.properties"));
        } catch (IOException e) {
            LOGGER.error("Can't read rodl-darceo properties file", e);
        }
        try {

            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            KeyStore serverStore = KeyStore.getInstance("JKS");

            clientStore.load(
                getClass().getClassLoader().getResourceAsStream(properties.getProperty("client_keystore")), properties
                        .getProperty("client_passphrase").toCharArray());
            serverStore.load(
                getClass().getClassLoader().getResourceAsStream(properties.getProperty("server_keystore")), properties
                        .getProperty("server_passphrase").toCharArray());

            KeyManagerFactory clientKeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            clientKeyManagerFactory.init(clientStore, properties.getProperty("client_passphrase").toCharArray());
            TrustManagerFactory serverKeyManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            serverKeyManagerFactory.init(serverStore);

            SSLContext sslContextd = SSLContext.getInstance("SSL");
            sslContextd
                    .init(clientKeyManagerFactory.getKeyManagers(), serverKeyManagerFactory.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContextd.getSocketFactory());

            repositoryUri = URI.create(properties.getProperty("repository_url"));

            client = Client.create();
            client.setFollowRedirects(false);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
                | UnrecoverableKeyException | KeyManagementException e) {
            LOGGER.error(e);
        }
    }


    /**
     * Get the instance of RepositoryClient.
     * 
     * @return RepositoryClient instance.
     */
    public static RepositoryClient getInstance() {
        if (instance == null) {
            return new DArceoClient();
        }
        return instance;
    }


    @Override
    public InputStream get(URI id) {
        WebResource webResource = client.resource(repositoryUri).path(URLEncoder.encode(id.toString()));
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() == 200) {
            return response.getEntityInputStream();
        } else if (response.getStatus() == 202) {
            webResource = client.resource(response.getLocation().toString()
                    .replace("http://zmd.wrdz.synat.psnc.pl/", "http:%2F%2Fzmd.wrdz.synat.psnc.pl%2F"));
            response = webResource.get(ClientResponse.class);
            while (response.getStatus() == 200) {
                response = webResource.get(ClientResponse.class);
            }
            if (response.getStatus() == 303) {
                webResource = client.resource(response.getLocation().toString()
                        .replace("http://zmd.wrdz.synat.psnc.pl/", "http:%2F%2Fzmd.wrdz.synat.psnc.pl%2F"));
                return webResource.get(ClientResponse.class).getEntityInputStream();
            }
        }
        return null;
    }


    @Override
    public URI post(ResearchObjectSerializable researchObject) {
        WebResource webResource = client.resource(repositoryUri.toString());
        try {
            FileOutputStream f = new FileOutputStream(new File("/home/pejot/cos.zip"));
            IOUtils.copy(IO.toZipInputStream(researchObject), f);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ClientResponse response = webResource.type("application/zip").post(ClientResponse.class,
            IO.toZipInputStream(researchObject));
        return response.getLocation();
    }


    @Override
    public URI delete(URI id) {
        WebResource webResource = client.resource(repositoryUri).path(URLEncoder.encode(id.toString()));
        ClientResponse response = webResource.delete(ClientResponse.class);
        if (response.getStatus() == 202) {
            webResource = client.resource(response.getLocation().toString()
                    .replace("http://zmd.wrdz.synat.psnc.pl/", "http:%2F%2Fzmd.wrdz.synat.psnc.pl%2F"));
            return webResource.getURI();
        }
        return null;

    }


    @Override
    public URI postWait(URI status) {
        WebResource webResource = client.resource(status.toString());
        ClientResponse response = webResource.get(ClientResponse.class);

        while (response.getStatus() == 200) {
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
    public Boolean deleteWait(URI status) {
        WebResource webResource = client.resource(status);
        ClientResponse response = webResource.get(ClientResponse.class);
        while (response.getStatus() == 200) {
            response = webResource.get(ClientResponse.class);
        }
        if (response.getStatus() == 303) {
            webResource = client.resource(response.getLocation().toString()
                    .replace("http://zmd.wrdz.synat.psnc.pl/", "http:%2F%2Fzmd.wrdz.synat.psnc.pl%2F"));
            response = webResource.get(ClientResponse.class);
            if (response.getStatus() == 200) {
                return true;
            }

        }
        return false;
    }
}
