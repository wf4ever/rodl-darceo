package pl.psnc.dl.wf4ever.darceo.client.mock;

import java.io.InputStream;
import java.net.URI;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;

public class ResearchObjectComponentSerializableMock implements ResearchObjectComponentSerializable {

    public ResearchObjectComponentSerializableMock(String resourcePath) {

    }


    @Override
    public URI getUri() {
        //IMPLEMENT ME!
        return null;
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
        // IMPLEMENT ME!
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
