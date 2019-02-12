package com.example.myfirstapp;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MainActivityTest
{
    @Test
    public void testGetPossibleVendors() {

        MainActivity testedInstance = new MainActivity();

        List<String> result = testedInstance.getPossibleVendors("first");
        assertEquals(1, result.size());
        assertEquals("first", result.get(0));

        result = testedInstance.getPossibleVendors("first second");
        assertEquals(3, result.size());
        assertTrue(result.contains("first"));
        assertTrue(result.contains("second"));
        assertTrue(result.contains("first second"));

        result = testedInstance.getPossibleVendors("first second third");
        assertEquals(6, result.size());
        assertTrue(result.contains("first"));
        assertTrue(result.contains("second"));
        assertTrue(result.contains("third"));
        assertTrue(result.contains("first second"));
        assertTrue(result.contains("second third"));
        assertTrue(result.contains("first second third"));
    }

/*    @Test
    public void testGetRating() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(getTestData().getBytes("UTF-8"));
        Document doc = builder.parse(input);


        NodeList shit = doc.getElementsByTagName("img");
        NamedNodeMap map = shit.item(0).getAttributes();
        String val = map.getNamedItem("src").getNodeValue();

        System.out.println(val);
    }

    private String getTestData(){
        return "<div class=\"kodex tabzelle\" title=\"cssbody=[tooltip_body_gruen] cssheader=[tooltip_header_gruen] header=[Unternehmenspolitik gegen Kinderarbeit:] \"><img src=\"/wp-content/blogs.dir/2/files/agk-webdesign/gruen.gif\" width=\"16\" height=\"16\"></img></div>";
    }*/
}