package extension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

public class AspectExample {

    private static final String CM_TITLE = "cm:title";
    private static final String CM_DESCRIPTION = "cm:description";

    private JConfig config;
    private JDocument document;

    private Session session = null;

    public AspectExample(JConfig config, JDocument document) throws Exception {
        this.document = document;
        this.config = config;
    }

    public void doExample() {
        createTestDoc(document.getFolderPath(), document.getContentName());
        return;
    }

    public Session getSession() {
        if (this.session == null) {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // user credentials
            parameter.put(SessionParameter.USER, this.config.getUser());
            parameter.put(SessionParameter.PASSWORD, this.config.getPassword());

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, config.getCmisPath());

            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

            // Set the alfresco object factory
            // Used when using the CMIS extension for Alfresco for working with aspects
            parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            List<Repository> repositories = factory.getRepositories(parameter);

            this.session = repositories.get(0).createSession();
        }
        return this.session;
    }

    public Folder getTestFolder(String folderPath) throws CmisObjectNotFoundException {
        Session session = getSession();

        // Grab a reference to the folder where we want to create content
        Folder folder = null;
        try {
            folder = (Folder) session.getObjectByPath(folderPath);
            System.out.println("Found folder: " + folder.getName() + "(" + folder.getId() + ")");
        } catch (CmisObjectNotFoundException confe) {
            Folder targetBaseFolder = null;
            String baseFolderPath = folderPath.substring(0, folderPath.lastIndexOf('/') + 1);
            String folderName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

            //if this one is not found, we'll let the exception bubble up
            targetBaseFolder = (Folder) session.getObjectByPath(baseFolderPath);

            // Create a Map of objects with the props we want to set
            Map<String, Object> properties = new HashMap<String, Object>();

            // Following sets the content type and adds the webable and productRelated aspects
            // This works because we are using the OpenCMIS extension for Alfresco
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder, P:cm:titled");
            properties.put(PropertyIds.NAME, folderName);

            properties.put(CM_DESCRIPTION, this.document.getDescription());
            properties.put(CM_TITLE, this.document.getTitle());

            folder = targetBaseFolder.createFolder(properties);
            System.out.println("Created folder: " + folder.getName() + " (" + folder.getId() + ")");
        }

        return folder;
    }

    public Document createTestDoc(String folderPath, String docName) {
        // Grab a reference to the folder where we want to create content
        Folder folder = getTestFolder(folderPath);

        // Set up a name for the test document
        String timeStamp = new Long(System.currentTimeMillis()).toString();
        String fileName = docName + " (" + timeStamp + ")";

        // Create a Map of objects with the props we want to set
        Map<String, Object> properties = new HashMap<String, Object>();

        // Following sets the content type and adds the webable and productRelated aspects
        // This works because we are using the OpenCMIS extension for Alfresco
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document, P:cm:titled");
        properties.put(PropertyIds.NAME, fileName);
        properties.put(CM_DESCRIPTION, this.document.getDescription());
        properties.put(CM_TITLE, this.document.getTitle());

        // Set the content text
        String docText = "This is a sample document called " + docName;
        byte[] content = docText.getBytes();
        InputStream stream = new ByteArrayInputStream(content);
        ContentStream contentStream = new ContentStreamImpl(fileName, BigInteger.valueOf(content.length), "text/plain", stream);

        // Create the document
        Document doc = folder.createDocument(
                properties,
                contentStream,
                VersioningState.MAJOR);
        System.out.println("Created content: " + doc.getName() + "(" + doc.getId() + ")");
        System.out.println("Content Length: " + doc.getContentStreamLength());

        return doc;
    }

    public static void doUsage(String message) {
        System.out.println(message);
        System.exit(0);
    }
}