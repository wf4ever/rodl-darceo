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
    private String zipContent1 = "content/1.txt";
    private String zipContent2 = "content/2.txt";


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
    public void testCRUD()
            throws IOException {
        List<String> roContent = new ArrayList<String>();
        roContent.add("1.txt");
        roContent.add("2.txt");
        ResearchObjectSerializable ro = new ResearchObjectSerializableMock(roContent);
        crud(ro);
    }


    //TODO write more tests with the strange URIs to define expected exceptions in case of mistakes in URIs parameters. OK ;) ?

    private void crud(ResearchObjectSerializable ro)
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
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        int counter = 0;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(zipContent1) || entry.getName().equals(zipContent2)) {
                counter++;
            }
        }
        tmpFile.delete();
        //GET Test
        Assert.assertNull(DArceoClient.getInstance().get(id.resolve("wrong-id")));
        Assert.assertEquals(2, counter);
        //DELETE
        //DELETE Test
        Assert.assertNull(DArceoClient.getInstance().delete(id.resolve("wrong-id")));
        Assert.assertTrue(DArceoClient.getInstance().deleteWait(DArceoClient.getInstance().delete(id)));
    }
}
