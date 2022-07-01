package org.vorpal.blade.services.loadbalancer.proxy;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.vorpal.blade.framework.callflow.Callflow;
import org.vorpal.blade.services.loadbalancer.proxy.ProxyTier.Mode;

public class ProxyInitialInvite extends Callflow implements ProxyListener {
	private ProxyRule proxyRule;
	private SipServletRequest inboundRequest;

	ProxyInitialInvite(ProxyListener proxyListener, ProxyRule proxyRule) {
		this.proxyRule = new ProxyRule(proxyRule);

	}

	@Override
	public void process(SipServletRequest request) throws ServletException, IOException {
		this.inboundRequest = request;

		if (proxyRule.getTiers().size() > 0) {
			ProxyTier proxyTier = proxyRule.getTiers().remove(0);

			if (proxyTier.getMode().equals(Mode.serial)) {
				ProxySerial proxySerial = new ProxySerial(this, request, proxyTier);
			} else {
				ProxyParallel proxyParallel = new ProxyParallel(this, request, proxyTier);
			}

		}

	}

	@Override
	public ProxyRule proxyRequest(SipServletRequest outboundRequest) throws ServletException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void proxyResponse(SipServletResponse bobResponse, List<SipServletResponse> proxyResponses)
			throws ServletException, IOException {

		if (proxyRule.getTiers().size() > 0) {
			this.process(inboundRequest);
		} else {

			SipServletResponse aliceResponse = this.createResponse(inboundRequest, bobResponse, true);

			sendResponse(aliceResponse, (aliceAck) -> {
				if (aliceAck.getMethod().equals(PRACK)) {
					SipServletRequest bobPrack = copyContentAndHeaders(aliceAck, bobResponse.createPrack());
					sendRequest(bobPrack, (prackResponse) -> {
						sendResponse(aliceAck.createResponse(prackResponse.getStatus()));
					});
				} else if (aliceAck.getMethod().equals(ACK)) {
					SipServletRequest bobAck = copyContentAndHeaders(aliceAck, bobResponse.createAck());
					sendRequest(bobAck);
				} else {
					// implement GLARE here?
				}
			});

		}

	}

}
