package pl.psnc.dl.wf4ever.darceo.model;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

public class TestResearchObjectComponent {

    private URI componentURI = URI.create("http://www.example.com/ROS/example_ro/component");


    @Test
    public void testConstructor() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("mock/1.txt");
        ResearchObjectComponent component = new ResearchObjectComponent(componentURI, input);
        Assert.assertEquals(componentURI, component.getUri());
        Assert.assertEquals(input, component.getSerialization());
    }
}
