package pl.psnc.dl.wf4ever.darceo.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import pl.psnc.dl.wf4ever.darceo.model.mock.ResearchObjectSerializableMock;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

import com.sun.jersey.api.client.Client;

public class TestDArceoClient {

    private static Client client = Client.create();


    @BeforeClass
    public static void setUpClass() {
        client = Client.create();
        client.setFollowRedirects(false);
    }


    @Test
    public void testSingleton() {
        Assert.assertNotNull(DArceoClient.getInstance());
    }


    @Test
    public void testCRUDRO()
            throws IOException {
        List<String> roContent = new ArrayList<String>();
        String path1 = "mock/simple/content/simple/1.txt";
        String path2 = "mock/simple/content/simple/2.txt";
        String path3 = "mock/simple/content/simple/.ro/manifest.rdf";
        String path4 = "mock/simple/content/simple/.ro/evo_info.ttl";
        roContent.add(path1);
        roContent.add(path2);
        roContent.add(path3);
        roContent.add(path4);
        List<String> expectedResources = new ArrayList<String>();
        expectedResources.add(path1);
        expectedResources.add(path2);
        expectedResources.add(path3);
        expectedResources.add(path4);
        ResearchObjectSerializable ro = new ResearchObjectSerializableMock(roContent, "mock/simple/content/simple/",
                URI.create("http://www.example.com/ROs/ro" + UUID.randomUUID().toString() + "/"));
        crud(ro, expectedResources);
    }


    //TODO write more tests with the strange URIs to define expected exceptions in case of mistakes in URIs parameters. OK ;) ?

    private void crud(ResearchObjectSerializable ro, List<String> expectedResources)
            throws IOException {

        URI statusURI = DArceoClient.getInstance().post(ro);
        Assert.assertNotNull(statusURI);
        URI id = DArceoClient.getInstance().postWait(statusURI);
        Assert.assertNotNull(id);

        //GET 
        ResearchObjectSerializable returnedRO = DArceoClient.getInstance().get(id);
        //@TODO check if the returned ro has all components;
        Assert.assertNotNull("RO couldn't be reterived", returnedRO);
        //GET Test
        Assert.assertNull(DArceoClient.getInstance().get(id.resolve("wrong-id")));

        //DELETE
        //DELETE Test
        Assert.assertNull(DArceoClient.getInstance().delete(id.resolve("wrong-id")));
        Assert.assertTrue(DArceoClient.getInstance().deleteWait(DArceoClient.getInstance().delete(id)));
    }
}
