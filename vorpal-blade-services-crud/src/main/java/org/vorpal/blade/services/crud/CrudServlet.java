package org.vorpal.blade.services.crud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.vorpal.blade.framework.b2bua.B2buaServlet;
import org.vorpal.blade.framework.b2bua.Bye;
import org.vorpal.blade.framework.b2bua.Cancel;
import org.vorpal.blade.framework.b2bua.InitialInvite;
import org.vorpal.blade.framework.b2bua.Passthru;
import org.vorpal.blade.framework.b2bua.Reinvite;
import org.vorpal.blade.framework.callflow.Callflow;
import org.vorpal.blade.framework.config.SettingsManager;
import org.vorpal.blade.framework.config.Translation;

@WebListener
@javax.servlet.sip.annotation.SipApplication(distributable = true)
@javax.servlet.sip.annotation.SipServlet(loadOnStartup = 1)
@javax.servlet.sip.annotation.SipListener
public class CrudServlet extends B2buaServlet {

	public SettingsManager<CrudConfiguration> settingsManager;

	@Override
	protected void servletCreated(SipServletContextEvent event) throws ServletException, IOException {
		settingsManager = new SettingsManager<>(event, CrudConfiguration.class, new CrudConfigurationSample());

	}

	@Override
	protected void servletDestroyed(SipServletContextEvent event) throws ServletException, IOException {
		try {
			settingsManager.unregister();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected Callflow chooseCallflow(SipServletRequest inboundRequest) throws ServletException, IOException {
		Callflow callflow = null;

		CrudConfiguration settings = settingsManager.getCurrent();

		if (inboundRequest.getMethod().equals("INVITE") && inboundRequest.isInitial()) {
			Translation t = settings.findTranslation(inboundRequest);

			sipLogger.severe(inboundRequest, "is t null " + t);

			if (t != null) {
				sipLogger.severe(inboundRequest, "found t! ");

				String ruleSetId = (String) t.getAttribute("ruleSet");
				sipLogger.severe(inboundRequest, "ruleSet.id " + ruleSetId);

				if (ruleSetId != null) {

					RuleSet ruleSet = settings.ruleSets.get(ruleSetId);

					if (ruleSet != null) {

						sipLogger.severe(inboundRequest, "ruleSet: " + ruleSet);

						if (ruleSet != null) {
							sipLogger.severe(inboundRequest, "found ruleSet.");

							ruleSet.process(inboundRequest);

							sipLogger.severe(inboundRequest, "done processing ruleset");

							sipLogger.severe(inboundRequest, "To: " + ruleSet.output.get("To"));
							sipLogger.severe(inboundRequest, "Request-URI: " + ruleSet.output.get("Request-URI"));

							callflow = new CrudInitialInvite(null, ruleSet.output);
						} else {
							sipLogger.severe(inboundRequest, "No ruleSet found.");

						}

					}

				}

			}
		}

		if (callflow == null) {
		//	callflow = super.chooseCallflow(inboundRequest);
			
			
			if (inboundRequest.getMethod().equals("INVITE")) {
				if (inboundRequest.isInitial()) {
					callflow = new InitialInvite(null);
				} else {
					callflow = new Reinvite(null);
				}
			} else if (inboundRequest.getMethod().equals("BYE")) {
				callflow = new Bye(null);
			} else if (inboundRequest.getMethod().equals("CANCEL")) {
				callflow = new Cancel(null);
			} else {
				callflow = new Passthru(null);
			}
			
		}

		return callflow;
	}

	@Override
	public void callStarted(SipServletRequest outboundRequest) throws ServletException, IOException {
		SettingsManager.sipLogger.warning(outboundRequest, "Sending INVITE...\n" + outboundRequest.toString());
	}

	@Override
	public void callAnswered(SipServletResponse outboundResponse) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callConnected(SipServletRequest outboundRequest) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callCompleted(SipServletRequest outboundRequest) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callDeclined(SipServletResponse outboundResponse) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callAbandoned(SipServletRequest outboundRequest) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
