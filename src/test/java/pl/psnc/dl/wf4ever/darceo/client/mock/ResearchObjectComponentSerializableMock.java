package pl.psnc.dl.wf4ever.darceo.client.mock;

import java.io.InputStream;
import java.net.URI;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;

public class ResearchObjectComponentSerializableMock implements ResearchObjectComponentSerializable {

    private static final Logger LOGGER = Logger.getLogger(ResearchObjectComponentSerializableMock.class);

    private InputStream serialization;
    private URI uri;


    public ResearchObjectComponentSerializableMock(String resourcePath, String driPath) {
        this.serialization = getClass().getClassLoader().getResourceAsStream(resourcePath);
        this.uri = URI.create(resourcePath.split(driPath)[1]);
    }


    @Override
    public URI getUri() {
        return uri;
    }


    @Override
    public URI getUri(RDFFormat format) {
        return null;
    }


    @Override
    public ResourceMetadata getStats() {
        return null;
    }


    @Override
    public InputStream getSerialization() {
        return serialization;
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
