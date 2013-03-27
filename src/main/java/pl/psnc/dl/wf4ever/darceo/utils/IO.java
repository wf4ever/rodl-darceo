package pl.psnc.dl.wf4ever.darceo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

/**
 * Aggregates functions used to process data exchanged by dArceo and rodl.
 * 
 * @author pejot
 * 
 */
public class IO {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(IO.class);


    /**
     * Prepare a Research Object to be sent as a package in a zip format to dArceo.
     * 
     * @param researchObject
     *            Research Object
     * @return input stream with zipped ro.
     * @throws IOException
     */
    ZipInputStream toZipInputStream(ResearchObjectSerializable researchObject) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("dArcoArtefact", ".zip");
            tmpFile.deleteOnExit();
            ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(tmpFile));
            for (ResearchObjectComponentSerializable component : researchObject.getSerializables()) {
                URI path = URI.create("content/").resolve(researchObject.getUri().relativize(component.getUri()));
                ZipEntry entry = new ZipEntry(path.toString());
                zipOutput.putNextEntry(entry);
                IOUtils.copy(component.getSerialization(), zipOutput);
                zipOutput.closeEntry();
            }
            zipOutput.flush();
            zipOutput.close();
            ZipInputStream result = new ZipInputStream(new FileInputStream(tmpFile));
            tmpFile.delete();
            return result;
        } catch (IOException e) {
            LOGGER.error("Can't prepare a RO " + researchObject.getUri() + " for dArceo", e);
            if (tmpFile != null) {
                tmpFile.delete();
            }
            return null;
        }

    }
}
