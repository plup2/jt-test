package goc.webtemplate.jsf.samplebeans;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import goc.webtemplate.SessionTimeout;
import goc.webtemplate.component.jsf.DefaultTemplateBean;

public class SessionTimeoutSample extends DefaultTemplateBean {

	@Override
	public void setSessionTimeoutConfigurations() {
		SessionTimeout configs = new SessionTimeout();
		
		configs.setInActivity(30000);
		configs.setReactionTime(10000);
		configs.setSessionAlive(30000);
		configs.setLogoutUrl("SessionTimeoutLogout.xhtml");            
		configs.setRefreshCallbackUrl("SessionTimeoutValidity.xhtml");
		configs.setRefreshOnClick(false);
		configs.setRefreshLimit(3);
		configs.setMethod("");
		configs.setAdditionalData("");
		
		this.sessionTimeoutConfigurations = configs;
	}
	
	@Override
	public void setSessionTimeoutEnabled() { this.sessionTimeoutEnabled = true; }
	
	public String getLogoutAction() {
		try {
			HttpServletRequest currentReq = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			HttpSession session = currentReq.getSession(false);
			if (session != null) session.invalidate();
			((HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse()).sendRedirect("AddJSandCSSFilesSample.xhtml");
		}
		catch (Exception ex) {}
		return "";
	}
	
	public String getSessionValidity() {
		String sessionValid = "true";
		try {
			HttpServletRequest currentReq = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			HttpSession session = currentReq.getSession(false);
			sessionValid = (session == null ? "false" : "true");
		}
		catch (Exception ex) {}
		return sessionValid;
	}
}
