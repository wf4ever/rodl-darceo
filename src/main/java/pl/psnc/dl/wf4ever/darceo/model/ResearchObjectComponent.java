package pl.psnc.dl.wf4ever.darceo.model;

import java.io.InputStream;
import java.net.URI;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;

/**
 * Stored and retrieved in dArceo Research Object Component. Aggregated by ResearchObjectSerialziable;
 * 
 */
public class ResearchObjectComponent implements ResearchObjectComponentSerializable {

    /** Component uri. */
    URI uri;

    /** Serialization. */
    InputStream serialziation;


    /**
     * Constructor.
     * 
     * @param uri
     *            uri
     * @param serialization
     *            serialization (null in case of external resources)
     */
    public ResearchObjectComponent(URI uri, InputStream serialization) {
        this.uri = uri;
        this.serialziation = serialization;
    }


    @Override
    public URI getUri() {
        return this.uri;
    }


    @Override
    public InputStream getSerialization() {
        return this.serialziation;
    }


    //TODO
    //should they throw not implement?
    //should be moved?

    @Override
    public URI getUri(RDFFormat format) {
        return null;
    }


    @Override
    public ResourceMetadata getStats() {
        return null;
    }


    @Override
    public InputStream getGraphAsInputStream(RDFFormat syntax) {
        return null;
    }


    @Override
    public boolean isNamedGraph() {
        return false;
    }


    @Override
    public InputStream getPublicGraphAsInputStream(RDFFormat syntax) {
        return null;
    }

}
