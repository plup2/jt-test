package goc.webtemplate.component.abstractcorebeantest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import goc.webtemplate.WebAnalyticsInfo;

public class RenderRefTopTest {

    @Test
    public void testWebAnalyticsRenders() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        sut.setWebAnalytics(new WebAnalyticsInfo(true, WebAnalyticsInfo.EnvironmentOption.STAGING, 1));
        
        assertTrue(sut.getRenderRefTop().contains("\"webAnalytics\":[{\"environment\":\"staging\",\"version\":1}]"),
        		"RefFooter rendering: WebAnalytics not rendered as expected (" + sut.getRenderRefTop() + ").");
        
        assertTrue(!sut.getRenderRefTop().contains("custom"),
        		"RefFooter rendering: WebAnalytics not rendered as expected (" + sut.getRenderRefTop() + ").");
    }
    
    @Test
    public void testWebAnalyticsVersion3Renders() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        sut.setWebAnalytics(new WebAnalyticsInfo(true, "launch-EN0cf6c2810a2b48f8a4c36502a1b09541.min.js"));
        
        assertTrue(sut.getRenderRefTop().contains("\"custom\":\"launch-EN0cf6c2810a2b48f8a4c36502a1b09541.min.js\"}]"),
        		"RefFooter rendering: WebAnalytics not rendered as expected (" + sut.getRenderRefTop() + ").");
    }
    
    @Test
    public void testWebAnalyticsFromConfig() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        //(Test test cdn.properties files default WebAnalytics to true)
        assertTrue(sut.getRenderRefTop().contains("\"webAnalytics\":"),
        		"RefFooter rendering: WebAnalytics not rendered as expected (" + sut.getRenderRefTop() + ").");
    }
}
