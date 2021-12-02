package org.vorpal.blade.test.uas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.vorpal.blade.framework.b2bua.B2buaListener;
import org.vorpal.blade.framework.b2bua.B2buaServlet;
import org.vorpal.blade.framework.callflow.Callflow;
import org.vorpal.blade.framework.config.SettingsManager;
import org.vorpal.blade.test.uas.callflows.UasCallflow;
import org.vorpal.blade.test.uas.config.TestUasConfig;

@WebListener
@javax.servlet.sip.annotation.SipApplication(distributable = true)
@javax.servlet.sip.annotation.SipServlet(loadOnStartup = 1)
@javax.servlet.sip.annotation.SipListener
public class UasServlet extends B2buaServlet implements B2buaListener {

	public static SettingsManager<TestUasConfig> settingsManager;

	@Override
	protected Callflow chooseCallflow(SipServletRequest request) throws ServletException, IOException {
		Callflow callflow = null;

		Integer status;

		String strStatus = request.getRequestURI().getParameter("status");
		if (strStatus != null) {
			status = Integer.parseInt(strStatus);
		} else {
			String user = ((SipURI) (request.getTo().getURI())).getUser();
			sipLogger.fine("user: "+user);
			status = settingsManager.getCurrent().getErrorMap().get(user);
			sipLogger.fine("status: "+status);
		}

		if (request.isInitial() && status != null) {
			callflow = new UasCallflow(status);
		} else {
			callflow = super.chooseCallflow(request);
		}

		return callflow;
	}

	@Override
	protected void servletCreated(SipServletContextEvent event) {
		settingsManager = new SettingsManager<TestUasConfig>(event, TestUasConfig.class);
		sipLogger.logConfiguration(settingsManager.getCurrent());

	}

	@Override
	protected void servletDestroyed(SipServletContextEvent event) {
		// do nothing;
	}

	@Override
	public void callStarted(SipServletRequest request) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callAnswered(SipServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callConnected(SipServletRequest request) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callCompleted(SipServletRequest request) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callDeclined(SipServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callAbandoned(SipServletRequest request) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
