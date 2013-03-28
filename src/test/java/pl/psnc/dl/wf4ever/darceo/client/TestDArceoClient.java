package pl.psnc.dl.wf4ever.darceo.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.psnc.dl.wf4ever.darceo.client.mock.ResearchObjectSerializableMock;
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
    public void testCRUDObject()
            throws IOException {
        List<String> roContent = new ArrayList<String>();
        String resourcePath1 = "mock/1.txt";
        String resourcePath2 = "mock/2.txt";
        roContent.add(resourcePath1);
        roContent.add(resourcePath2);
        List<String> expectedResources = new ArrayList<String>();
        expectedResources.add(resourcePath1);
        expectedResources.add(resourcePath2);
        ResearchObjectSerializable ro = new ResearchObjectSerializableMock(roContent);
        crud(ro, expectedResources);
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
        ResearchObjectSerializable ro = new ResearchObjectSerializableMock(roContent);
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
        File tmpFile = File.createTempFile("darceo", "zip");
        FileOutputStream out = new FileOutputStream(tmpFile);
        IOUtils.copy(DArceoClient.getInstance().get(id), out);
        out.flush();
        out.close();
        ZipFile zipFile = new ZipFile(tmpFile);

        for (String expectedResource : expectedResources) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            boolean hasEntry = false;
            while (entries.hasMoreElements()) {
                if (("content/" + expectedResource.toString()).equals(entries.nextElement().getName())) {
                    hasEntry = true;
                    break;
                }
            }

            Assert.assertTrue("expected entry: " + expectedResource + " is not in the returned structure", hasEntry);
        }
        tmpFile.delete();
        //GET Test
        Assert.assertNull(DArceoClient.getInstance().get(id.resolve("wrong-id")));

        //DELETE
        //DELETE Test
        Assert.assertNull(DArceoClient.getInstance().delete(id.resolve("wrong-id")));
        Assert.assertTrue(DArceoClient.getInstance().deleteWait(DArceoClient.getInstance().delete(id)));
    }
}
