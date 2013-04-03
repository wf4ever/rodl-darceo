package pl.psnc.dl.wf4ever.darceo.model.mock;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

public class ResearchObjectComponentSerializableMock implements ResearchObjectComponentSerializable {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ResearchObjectComponentSerializableMock.class);

    private InputStream serialization;

    private String resourcePath;


    public ResearchObjectComponentSerializableMock(String resourceLocationPath, String resourcePath) {
        this.resourcePath = resourcePath;
        this.serialization = getClass().getClassLoader().getResourceAsStream(resourceLocationPath);
    }


    @Override
    public URI getUri() {
        return UriBuilder.fromPath(resourcePath).build();
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


    @Override
    public String getName() {
        String[] s = getPath().split("/");
        return s[s.length - 1];
    }


    @Override
    public ResearchObjectSerializable getResearchObject() {
        return null;
    }


    @Override
    public boolean isInternal() {
        return true;
    }


    @Override
    public String getPath() {
        return resourcePath;
    }

}
