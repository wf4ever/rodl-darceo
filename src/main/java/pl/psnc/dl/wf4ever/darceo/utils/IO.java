package pl.psnc.dl.wf4ever.darceo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.darceo.model.ResearchObject;
import pl.psnc.dl.wf4ever.darceo.model.ResearchObjectComponent;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Aggregates functions used to process data exchanged by dArceo and rodl.
 * 
 * @author pejot
 * 
 */
public final class IO {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(IO.class);
    /** metadata id file path. */
    private static final String METADATA_ID_FILE_PATH = "metadata/id.mets";
    /** path for template for metadata id file. */
    private static final String METADATA_TEMPLATE_ID_FILE_PATH = "templates/metadata/id.mets";
    /** content directory path. */
    private static final String CONTENT_PATH = "content/";
    /** manifest path . */
    private static final String MANIFEST_PATH = ".ro/manifest.rdf";


    /**
     * Hiddden constructor.
     */
    private IO() {
        //nope
    }


    /**
     * Prepare a Research Object to be sent as a package in a zip format to dArceo.
     * 
     * @param researchObject
     *            Research Object
     * @return input stream with zipped ro.
     * @throws IOException
     */
    public static InputStream toZipInputStream(ResearchObjectSerializable researchObject) {
        File tmpFile = null;
        List<ZipEntry> entries = new ArrayList<ZipEntry>();
        try {
            tmpFile = File.createTempFile("dArcoArtefact", ".zip");
            tmpFile.deleteOnExit();
            ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(tmpFile));
            for (ResearchObjectComponentSerializable component : researchObject.getSerializables()) {
                putEntryAndDirectoriesPath(zipOutput,
                    URI.create(CONTENT_PATH).resolve(researchObject.getUri().relativize(component.getUri())),
                    component.getSerialization(), entries);
            }
            //add metadata
            putMetadataId(zipOutput, entries, researchObject.getUri());
            zipOutput.flush();
            zipOutput.close();
            InputStream result = new FileInputStream(tmpFile);
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


    /**
     * Add to the package metadata file containing RO id.
     * 
     * @param zipOutput
     *            zip output
     * @param entriesGroup
     *            exited entries
     * @param id
     *            Research Object id
     * @throws IOException .
     */
    private static void putMetadataId(ZipOutputStream zipOutput, List<ZipEntry> entriesGroup, URI id)
            throws IOException {
        String template = IOUtils.toString(IO.class.getClassLoader()
                .getResourceAsStream(METADATA_TEMPLATE_ID_FILE_PATH));
        InputStream input = IOUtils.toInputStream(template.replace("{{object-id}}", id.toString()));
        putEntryAndDirectoriesPath(zipOutput, URI.create(METADATA_ID_FILE_PATH), input, entriesGroup);
    }


    /**
     * Add a new entry and the directories(as an empty entry) if they are not in the group yet.
     * 
     * @param zipOutput
     *            zip output stream
     * @param path
     *            entry path
     * @param input
     *            input stream (entry content)
     * @param entriesGroup
     *            group of entries of this serialziation
     * @throws IOException .
     */
    private static void putEntryAndDirectoriesPath(ZipOutputStream zipOutput, URI path, InputStream input,
            List<ZipEntry> entriesGroup)
            throws IOException {
        //first append all directories 
        String[] directoriesList = path.toString().split("/");
        for (int i = 0; i < directoriesList.length - 1; i++) {
            String entryName = "";
            for (int j = 0; j <= i; j++) {
                entryName += directoriesList[j] + "/";
                if (!inEntries(entriesGroup, entryName)) {
                    putEntry(zipOutput, new ZipEntry(entryName), null, entriesGroup);
                }
            }
        }
        if (!inEntries(entriesGroup, path.toString())) {
            putEntry(zipOutput, new ZipEntry(path.toString()), input, entriesGroup);
        }
    }


    /**
     * Add new entry to the output stream and entries group.
     * 
     * @param zipOutput
     *            stream
     * @param entry
     *            zip entry
     * @param input
     *            input stream (entry content, may be null)
     * @param entriesGroup
     *            the group of currently existed entries
     * @throws IOException .
     */
    private static void putEntry(ZipOutputStream zipOutput, ZipEntry entry, InputStream input,
            List<ZipEntry> entriesGroup)
            throws IOException {
        zipOutput.putNextEntry(entry);
        if (input != null) {
            IOUtils.copy(input, zipOutput);
        }
        zipOutput.closeEntry();
        zipOutput.flush();
        entriesGroup.add(entry);
    }


    /**
     * Check if current entry belongs to the entries group.
     * 
     * @param entriesGroup
     *            entries group
     * @param entryName
     *            entry name
     * @return true if current entry belong to the entries group, false otherwise
     */
    private static boolean inEntries(List<ZipEntry> entriesGroup, String entryName) {
        for (ZipEntry e : entriesGroup) {
            if (e.getName().equals(entryName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Create a ResearchObject from given zip as input stream.
     * 
     * @param id
     *            Research Object id
     * @param input
     *            The content of the zip aggreagated ResearchObject
     * @return a instance of ResearchObject
     */
    @SuppressWarnings("resource")
    public static ResearchObjectSerializable zipInputStreamToResearchObject(URI id, InputStream input) {
        File tmpZipFile = null;
        ZipFile zipFile = null;
        try {
            tmpZipFile = File.createTempFile("zipInput", ".zip");
            IOUtils.copy(input, new FileOutputStream(tmpZipFile));
            zipFile = new ZipFile(tmpZipFile);
        } catch (IOException e) {
            LOGGER.error("Can't careate a tmpFile for a RO " + id + " given from dArceo", e);
            return null;
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        //first get Manifest build Jena and parese it
        ResearchObject researchObject = new ResearchObject(id);
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals("content/simple/.ro/manifest.rdf")) {
                OntModel model = ModelFactory.createOntologyModel();
                try {
                    model.read(zipFile.getInputStream(entry), id.toString() + ".ro/");
                    model.write(new FileOutputStream(new File("/home/pejot/m.txt")), "TTL");
                    Individual roIndividual = model.getResource(id.toString()).as(Individual.class);
                    for (RDFNode node : roIndividual.listPropertyValues(ORE.aggregates).toList()) {
                        if (node.isURIResource()) {
                            URI resourceUri = URI.create(node.asResource().getURI());
                            URI entryName = URI.create("content/").resolve(
                                id.relativize(URI.create(node.asResource().getURI())).toString());
                            InputStream entryInput = zipFile.getInputStream(new ZipEntry(entryName.toString()));
                            researchObject.addSerializable(new ResearchObjectComponent(resourceUri, entryInput));
                        }
                        zipFile.close();
                        break;
                    }
                } catch (IOException e) {
                    LOGGER.error("can't load the manifest from zip for RO " + id, e);
                    return researchObject;
                }
            }
        }
        tmpZipFile.delete();
        return researchObject;
    }
}
