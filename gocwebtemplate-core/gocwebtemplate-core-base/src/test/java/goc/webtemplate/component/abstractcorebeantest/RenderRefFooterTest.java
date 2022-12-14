package goc.webtemplate.component.abstractcorebeantest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RenderRefFooterTest {

    @Test
    public void testRenderWithoutSecureSite() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        assertTrue(!sut.getRenderRefFooter().contains("\"exitScript\":false,\"displayModal\":false"),
        		"RefFooter rendering: Not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }
    
    @Test
    public void testRenderWithSecureSite() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        sut.getLeavingSecureSiteWarning().setEnabled(true);
        
        assertTrue(sut.getRenderRefFooter().contains("\"exitSecureSite\":{\"exitScript\":true,\"displayModal\":true,\"displayModalForNewWindow\":true,\"exitMsg\":\"\""),
        		"RefFooter rendering: LeavingSecureSite not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }
    
    @Test
    public void testRenderWithSecureSiteAndYesCancelMessages() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        sut.getLeavingSecureSiteWarning().setEnabled(true);
        sut.getLeavingSecureSiteWarning().setCancelMessage("Test Cancel Message");
        sut.getLeavingSecureSiteWarning().setYesMessage("Test Yes Message");
        sut.getLeavingSecureSiteWarning().setTargetWarning("Test Target Warning");
        
        assertTrue(sut.getRenderRefFooter().contains("\"cancelMsg\":\"Test Cancel Message\",\"yesMsg\":\"Test Yes Message\",\"targetWarning\":\"Test Target Warning\""),
        		"RefFooter rendering: LeavingSecureSite not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }
    
    @Test
    public void testWebAnalyticsRenders() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        sut.getWebAnalytics().setActive(false);
        assertTrue(sut.getRenderRefFooter().contains("\"webAnalytics\":false"), 
        		"RefFooter rendering: WebAnalytics not rendered as expected.");
    }
    
    @Test
    public void testWebAnalyticsOnlyIfSupportedInEnv() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();
        
        sut.getWebAnalytics().setActive(true);
        sut.setCDNEnvironment("PROD_SSL"); //this environment doesn't suppport analytics
        
        try {
            sut.getRenderRefFooter(); //will throw exception
            fail("Expected RuntimeException thrown.");
        }
        catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("WebAnalytics feature is not supported"), 
            		"Unexpected exception message.");
        }
    }

    @Test
    public void testDisplayModalNewWinTrueWhenExitscriptDisabled() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();

        sut.getLeavingSecureSiteWarning().setEnabled(false);

        assertTrue(!sut.getRenderRefFooter().contains("\"displayModalForNewWindow\""),
        		"RefFooter rendering: LeavingSecureSite not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }
    
    @Test
    public void testDisplayModalNewWinFalse() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();

        sut.getLeavingSecureSiteWarning().setEnabled(true);
        sut.getLeavingSecureSiteWarning().setDisplayModalForNewWindow(false);

        assertTrue(sut.getRenderRefFooter().contains("\"displayModalForNewWindow\":false"),
        		"RefFooter rendering: LeavingSecureSite not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }
    
    @Test
    public void testMsgBoxHeader() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();

        sut.getLeavingSecureSiteWarning().setEnabled(true);
        sut.getLeavingSecureSiteWarning().setMsgBoxHeader("Warning, you are leaving a secure site!");

        assertTrue(sut.getRenderRefFooter().contains("\"msgBoxHeader\":\"Warning, you are leaving a secure site!\""),
        		"RefFooter rendering: LeavingSecureSite not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }

    @Test
    public void testIsApplicationFalse() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();

        assertTrue(sut.getRenderRefFooter().contains("\"isApplication\":false"),
        		"RefFooter rendering: Not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }

    @Test
    public void testIsApplicationTrue() {
        AbstractCoreBeanImpl sut = new AbstractCoreBeanImpl();

        assertTrue(sut.getRenderRefFooterForApp().contains("\"isApplication\":true"),
        		"RefFooter rendering: Not rendered as expected. (" + sut.getRenderRefFooter() + ")");
    }
}
