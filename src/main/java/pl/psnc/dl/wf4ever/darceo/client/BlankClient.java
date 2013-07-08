package pl.psnc.dl.wf4ever.darceo.client;

import java.net.URI;

import pl.psnc.dl.wf4ever.preservation.client.RepositoryClient;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

/**
 * Blank empty client. Used when there is no dArco configuration (rodl instalation without dArceo)
 * 
 * @author pejot
 * 
 */
public class BlankClient implements RepositoryClient {

    @Override
    public URI delete(URI arg0) {
        return null;
    }


    @Override
    public boolean deleteBlocking(URI arg0) {
        return false;
    }


    @Override
    public ResearchObjectSerializable getBlocking(URI arg0) {
        return null;
    }


    @Override
    public URI post(ResearchObjectSerializable arg0) {
        return null;
    }


    @Override
    public URI postORUpdateBlocking(URI arg0) {
        return null;
    }


    @Override
    public URI update(ResearchObjectSerializable arg0) {
        return null;
    }


    @Override
    public URI getServiceUri() {
        return null;
    }

}
