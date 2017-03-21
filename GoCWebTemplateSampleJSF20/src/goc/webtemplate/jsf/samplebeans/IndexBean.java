package goc.webtemplate.jsf.samplebeans;

import java.lang.StringBuilder;
import java.util.ResourceBundle;

import goc.webtemplate.component.jsf.DefaultTemplateBean;

public class IndexBean extends DefaultTemplateBean {

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
		sb.append("<strong>default switch language url</strong> = " + this.getDefaultLangLinkUrl() + "<br />");
		sb.append("<strong>default leave secure site redirect url</strong> = " + this.getDefaultLeaveSecureSiteRedirectUrl());
		
		return sb.toString();
	}
}
