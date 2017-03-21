<h1>GoC Web Template Samples - Session Timeout</h1>
<p><a href="http://www.gcpedia.gc.ca/wiki/Content_Delivery_Network/GoC_.NET_template_guide">Web Template Documentation (GCPedia)</a></p>
<p>This sample helps web page owners by providing session timeout and inactivity timeout functionality and is based on the <a href="http://wet-boew.github.io/wet-boew/demos/session-timeout/session-timeout-en.html">WET Session Timeout plugin</a>. When a user requests a page with this plugin implemented their session will begin. After the specified session period, they will be notified that their session is about to timeout. At this point, they will have the option to remain logged in by clicking "Continue session", or signing out by clicking "End session now".</p>
<h2>Pre-requisite</h2>
<p>To override the Default GoC Web Template look &amp; feel, you will have to create a custom bean class that extends the <code class="wb-prettify">goc.webtemplate.component.jsp.BaseBean</code> class, and then override the various methods made available to alter the look &amp; feel of the web page.</p>
<p>For this particular sample page, we are using the <code class="wb-prettify">goc.webtemplate.jsp.samplebeans.SessionTimeoutSampleBean</code> bean class.</p>
<p>The bean must be included and initialized in a jsp page as part of the <strong>beaninit</strong> attribute that is defined by the master template tiles definition outline in the tiles.xml configuration file:</p>
<p>The custom bean name must be <strong>clientbean</strong> and the <strong>request</strong> param must be also be present as it is.</p>
<div class="wb-prettify all-pre lang-vb linenums">
	<pre>
&lt;s:bean name="goc.webtemplate.jsp.samplebeans.SessionTimeoutSampleBean" var="clientbean"&gt;
	&lt;s:param name="request" value="#request.servletrequest" /&gt;
&lt;/s:bean&gt;
	</pre>
</div>
<h2>Session Timeout Configuration</h2>
<p>At any time during the session, if the user remains idle for a specified amount of time, they will be notified that they're session is about to timeout. In either case, if the user does not respond to the timeout notification within a specified amount of time, once they click either "Continue session" or "End session now" they will be automatically redirected to the sign out page.</p>
<h4>To Enable the session timeout:</h4>
<ul>
    <li>Set the key <code class="wb-prettify">"session.timeout.enabled"</code> in the cdn.properties file to "true".</li>
    <li>or set the property programmatically via overriding the <code class="wb-prettify">"setSessionTimeoutEnabled"</code> method in your custom bean class</li>
</ul>
<p>The rest of the configuration are set in the cdn.properties resource bundle or programmatically via the <code class="wb-prettify">setSessionTimeoutConfigurations</code> method using the goc.webtemplate.SessionTimeout object: </p>
<ul>
    <li><strong>session.inactivity.value</strong>: inactivity period of time after which the modal dialog will appear (default 20 minutes)</li>
    <li><strong>session.reactiontime.value</strong>: period of time the user has to perform an action once the modal dialog is displayed (default 3 minutes)</li>
    <li><strong>session.sessionalive.value</strong>: period of time for the session to stay alive until the modal dialog appears (default 20 minutes)</li>
    <li><strong>session.logouturl</strong>: URL that users are sent to when the session has expired</li>
    <li><strong>session.refreshcallbackurl</strong>: URL used to perform an ajax request to determine the validity of the session</li>
    <li><strong>session.refreshonclick</strong>: Determines if clicking on the document should reset the inactivity timeout and perform an ajax request (if a refreshCallbackUrl has been specified)</li>
    <li><strong>session.refreshlimit.value</strong>: Sets the amount of time that must pass before an ajax request can be made</li>
    <li><strong>session.method</strong>: Sets the request method used for ajax requests. Recommended: GET or POST</li>
    <li><strong>session.additionaldata</strong>: Additional data to send with the request</li>
</ul>
<h4>Notes:</h4>
<ul>
    <li>The <code class="wb-prettify">inactivity, reactionTime and sessionalive</code> parameters are set in milliseconds. For help with the time values, use this time converter.</li>
    <li>Your <code class="wb-prettify">sessionalive and inactivity</code> parameters should be equal to your web server session alive time minus the reactionTime time. If you set your <code class="wb-prettify">sessionalive time and inactivity time</code> to the same as your web server without taking into consideration the <code class="wb-prettify">reactionTime time</code> then the session will have ended by the server as soon as the popup appears to extend the session.</li>
    <li>The server response needs to contain a message body. Don't use a request method (e.g. HEAD) that disallows a message body in the response.</li>
</ul>	    
<div class="wb-prettify all-pre lang-vb linenums">
	<h4>Custom Bean Sample Code</h4>
    <pre>
@Override
public void setSessionTimeoutEnabled() { this.sessionTimeoutEnabled = true; }

@Override
public void setSessionTimeoutConfigurations() { 
	SessionTimeout configs = new SessionTimeout();
	
	configs.setInActivity(30000);
	configs.setReactionTime(10000);
	configs.setSessionAlive(30000);
	configs.setLogoutUrl("sessiontimeoutlogout.jsp");            
	configs.setRefreshCallbackUrl("sessiontimeoutvalidity.jsp");
	configs.setRefreshOnClick(false);
	configs.setRefreshLimit(3);
	configs.setMethod("");
	configs.setAdditionalData("");
	
	this.sessionTimeoutConfigurations = configs;
}
    </pre>
</div>
<div class="wb-prettify all-pre lang-vb linenums">
   	<h4>sessiontimeoutlogout.jsp</h4>
    <pre>
&lt;%@ taglib uri="/struts-tags" prefix="s" %&gt;
&lt;s:bean name="goc.webtemplate.jsp.samplebeans.SessionTimeoutSampleBean" var="timeoutbean"&gt;&lt;/s:bean&gt;
&lt;s:property value="#timeoutbean.logoutAction"/&gt;

// In the session timeout bean class, the getLogoutAction property that is 
// invoked simply does the Session cleanup code as required and performs a server redirect.
public String getLogoutAction() {
	try {
		HttpServletRequest currentReq = ServletActionContext.getRequest();
		HttpSession session = currentReq.getSession(false);
		if (session != null) session.invalidate();
		ServletActionContext.getResponse().sendRedirect("addjsandcssfilessample.action");
	}
	catch (Exception ex) {}
	return "";
}
    </pre>
</div>
<div class="wb-prettify all-pre lang-vb linenums">
	<h4>sessiontimeoutvalidity.jsp</h4>
    <pre>
<%@ taglib uri="/struts-tags" prefix="s" %>
&lt;s:bean name="goc.webtemplate.jsp.samplebeans.SessionTimeoutSampleBean" var="timeoutbean"&gt;&lt;/s:bean&gt;
&lt;s:property value="#timeoutbean.sessionValidity"/&gt;

// In the session timeout bean class, the getSessionValidity property simply returns the 
// string "true" or "false" depending on the current session state
public String getSessionValidity() {
	String sessionValid = "true";
	try {
		HttpServletRequest currentReq = ServletActionContext.getRequest();
		HttpSession session = currentReq.getSession(false);
		sessionValid = (session == null ? "false" : "true");
	}
	catch (Exception ex) {}
	return sessionValid;
}
    </pre>
</div>
<div>
    <h3>Other Web Template Samples</h3>
    <ul>
    	<li><a href="splashpagesample.action">Splash Page</a></li>
        <li><a href="addjsandcssfilessample.action">Adding CSS or JS</a></li>
        <li><a href="basesettingssample.action">Basic Settings</a></li>
        <li><a href="breadcrumbsample.action">Breadcrumbs</a></li>
        <li><a href="errorsample.action">Errors</a></li>
        <li><a href="extendedbasepagesample.action">Extended Base Page</a></li>
        <li><a href="feedbackandsharethispagesample.action">Feedback and Share This Page Links</a></li>
        <li><a href="footerlinkssample.action">Footer Links</a></li>
        <li><a href="leavingsecureSitesample.action">Leaving Secure Site Warning</a></li>
        <li><a href="leftsidemenusample.action">Left Side Menu</a></li>
        <li><a href="nestedmasterpagesample.action">Nested Master Page</a></li>
        <li><a href="sessiontimeoutsample.action">Session Timeout</a></li>
        <li><a href="transactionalsample.action">Transactional Page</a></li>
    </ul>
</div>