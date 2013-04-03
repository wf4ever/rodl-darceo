package pl.psnc.dl.wf4ever.darceo.model.mock;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

public class ResearchObjectSerializableMock implements ResearchObjectSerializable {

    private URI uri;
    private List<String> resources;
    Map<URI, ResearchObjectComponentSerializable> serializables;


    public ResearchObjectSerializableMock(List<String> resources, String dirPath) {
        this(resources, dirPath, null);
    }


    public ResearchObjectSerializableMock(List<String> resources, String dirPath, URI id) {
        this.resources = resources;
        this.serializables = new HashMap<>();
        for (String resource : this.resources) {
            ResearchObjectComponentSerializable component = new ResearchObjectComponentSerializableMock(resource,
                    resource.substring(dirPath.length()));
            serializables.put(component.getUri(), component);
        }
        if (id == null) {
            id = URI.create(UUID.randomUUID().toString());
        }
        uri = id;
    }


    @Override
    public Map<URI, ResearchObjectComponentSerializable> getSerializables() {
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
