package pl.psnc.dl.wf4ever.darceo.model.mock;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

public class ResearchObjectSerializableMock implements ResearchObjectSerializable {

    private URI uri;
    private List<String> resources;
    Set<ResearchObjectComponentSerializable> serializables;


    public ResearchObjectSerializableMock(List<String> resources, String dirPath) {
        this(resources, dirPath, null);
    }


    public ResearchObjectSerializableMock(List<String> resources, String dirPath, URI id) {
        this.resources = resources;
        this.serializables = new HashSet<>();
        for (String resource : this.resources) {
            serializables.add(new ResearchObjectComponentSerializableMock(resource, dirPath));
        }
        if (id == null) {
            id = URI.create(UUID.randomUUID().toString());
        }
        uri = id;
    }


    @Override
    public Set<ResearchObjectComponentSerializable> getSerializables() {
        return serializables;
    }


    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public URI getUri() {
        return uri;
    }

}
