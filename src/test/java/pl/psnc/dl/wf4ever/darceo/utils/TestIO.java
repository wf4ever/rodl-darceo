package pl.psnc.dl.wf4ever.darceo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import pl.psnc.dl.wf4ever.darceo.model.mock.ResearchObjectSerializableMock;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

public class TestIO {

    @Test
    public void testResearchObjectToZipInputStream()
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
        InputStream input = IO.toZipInputStream(ro);
        File tmpFile = File.createTempFile("testIOtoZipInputStream", ".zip");
        IOUtils.copy(input, new FileOutputStream(tmpFile));
        try (ZipFile zipFile = new ZipFile(tmpFile)) {
            //check resources
            Assert.assertTrue("Zip doesn't contain entry: content/1.txt", hasEntry(zipFile.entries(), "content/1.txt"));
            Assert.assertTrue("Zip doesn't contain entry: content/2.txt", hasEntry(zipFile.entries(), "content/2.txt"));
            Assert.assertTrue("Zip doesn't contain entry: content/.ro/manifest.rdf",
                hasEntry(zipFile.entries(), "content/.ro/manifest.rdf"));
            Assert.assertTrue("Zip doesn't contain entry: content/.ro/evo_info.ttl",
                hasEntry(zipFile.entries(), "content/.ro/evo_info.ttl"));

            //check metadata
            Assert.assertTrue("Zip doesn't contain entry: metadata/id.mets",
                hasEntry(zipFile.entries(), "metadata/id.mets"));
            //check folders
            Assert.assertTrue("Zip doesn't contain entry: " + "content/", hasEntry(zipFile.entries(), "content/"));
            Assert.assertTrue("Zip doesn't contain entry: " + "content/.ro/",
                hasEntry(zipFile.entries(), "content/.ro/"));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
            }
        }
    }


    @Test
    public void testZipInputStreamToResearchObject() {
        URI id = URI.create("http://www.example.com/ROs/simple/");
        ResearchObjectSerializable ro = IO.toResearchObject(id,
            getClass().getClassLoader().getResourceAsStream("simple.zip"));
        Assert.assertNotNull(ro.getSerializables().get(id.resolve("1.txt")));
        Assert.assertNotNull(ro.getSerializables().get(id.resolve("2.txt")));
        Assert.assertNotNull(ro.getSerializables().get(id.resolve(".ro/manifest.rdf")));
        Assert.assertNotNull(ro.getSerializables().get(id.resolve(".ro/evo_info.ttl")));
    }


    private boolean hasEntry(Enumeration<? extends ZipEntry> entries, String name) {
        boolean hasEntry = false;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(name)) {
                hasEntry = true;
                break;
            }
        }
        return hasEntry;
    }
}
