package pl.psnc.dl.wf4ever.darceo.model;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

public class TestReseachObject {

    private URI roUri = URI.create("http://www.example.org/ROs/my_ro/");


    @Test
    public void testConstructor() {
        ResearchObject ro = new ResearchObject(roUri);
        Assert.assertNotNull(ro.getUri() != null);
        Assert.assertNotNull(ro.getSerializables() != null);
    }


    @Test
    public void testAddSerialziables() {
        ResearchObject ro = new ResearchObject(roUri);
        ro.addSerializable(new ResearchObjectComponent(roUri.resolve("component1"), getClass().getClassLoader()
                .getResourceAsStream("mock/1.txt")));
        ro.addSerializable(new ResearchObjectComponent(roUri.resolve("component2"), getClass().getClassLoader()
                .getResourceAsStream("mock/2.txt")));
        Assert.assertEquals(2, ro.getSerializables().size());
    }


    @Test
    public void testAddSerialziablesUnique() {
        ResearchObject ro = new ResearchObject(roUri);
        ro.addSerializable(new ResearchObjectComponent(roUri.resolve("component1"), getClass().getClassLoader()
                .getResourceAsStream("mock/1.txt")));
        ro.addSerializable(new ResearchObjectComponent(roUri.resolve("component2"), getClass().getClassLoader()
                .getResourceAsStream("mock/2.txt")));
        ro.addSerializable(new ResearchObjectComponent(roUri.resolve("component1"), getClass().getClassLoader()
                .getResourceAsStream("mock/1.txt")));
        ro.addSerializable(new ResearchObjectComponent(roUri.resolve("component2"), getClass().getClassLoader()
                .getResourceAsStream("mock/2.txt")));

        Assert.assertEquals(2, ro.getSerializables().size());

    }
}
