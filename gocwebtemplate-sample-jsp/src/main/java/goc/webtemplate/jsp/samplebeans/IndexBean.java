package goc.webtemplate.jsp.samplebeans;

import goc.webtemplate.component.jsp.DefaultTemplateCoreBean;

import java.util.ResourceBundle;

public class IndexBean extends DefaultTemplateCoreBean {

	public String getBundleValues() {
		String elem = "";
		StringBuilder sb = new StringBuilder();
		ResourceBundle bundle = this.getResourceBundle();
		java.util.Enumeration<String> enumeration = bundle.getKeys();
		while (enumeration.hasMoreElements()) {
			elem = enumeration.nextElement();
			sb.append("<strong>" + elem + "</strong> = " + bundle.getString(elem) + "<br />");
		}
		sb.append("<br />");
		sb.append("<strong>default switch language url</strong> = " + this.getDefaultLanguageLinkUrl() + "<br />");
		sb.append("<strong>default leave secure site redirect url</strong> = " + this.getDefaultLeaveSecureSiteRedirectUrl());
		
		return sb.toString();
	}
}
