package extension;

import org.testng.annotations.Test;

@Test
public class AspectExampleTest {

    @Test
    public void shouldInsertDocument() throws Exception {
        JConfig config = new JConfig();
        config.setUser("admin");
        config.setPassword("admin");
        config.setCmisPath("http://127.0.0.1:8081/alfresco/api/-default-/public/cmis/versions/1.0/atom");

        JDocument doc = new JDocument();
        doc.setFolderPath("/wk");
        doc.setContentName("Hello-World-III.pdf");
        doc.setDescription("This is sample document called ...");
        doc.setTitle("Have a good day");

        AspectExample example = new AspectExample(config, doc);
        example.doExample();
    }
}
