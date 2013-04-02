package pl.psnc.dl.wf4ever.darceo.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

/**
 * Research Object stored and retrieved in dArceo.
 * 
 * @author pejot
 * 
 */
public class ResearchObject implements ResearchObjectSerializable {

    /** List of aggreagted components. */
    private Map<URI, ResearchObjectComponentSerializable> serializables;
    /** RO uri. */
    private URI uri;


    /**
     * Constructor.
     * 
     * @param uri
     *            RO uri
     * 
     */
    public ResearchObject(URI uri) {
        this.uri = uri;
        serializables = new HashMap<>();
    }


    @Override
    public Map<URI, ResearchObjectComponentSerializable> getSerializables() {
        return this.serializables;
    }


    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public URI getUri() {
        return this.uri;
    }


    /**
     * Aggregate new research object component.
     * 
     * @param component
     *            Research object component.
     */
    public void addSerializable(ResearchObjectComponentSerializable component) {
        this.serializables.put(component.getUri(), component);
    }

}
