/**
 *  MIT License
 *  
 *  Copyright (c) 2021 Vorpal Networks, LLC
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package org.vorpal.blade.framework.b2bua;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.vorpal.blade.framework.callflow.Callflow;

public class Reinvite extends Callflow {
	static final long serialVersionUID = 1L;
	private SipServletRequest aliceRequest;
//	private Callback<SipServletRequest> loopOnPrack;
//	private Callback<SipServletResponse> bobCallback = null;
	private B2buaListener sipServlet;

	public Reinvite(B2buaListener sipServlet) {
		this.sipServlet = sipServlet;
	}

	@Override
	public void process(SipServletRequest request) throws Exception {

		aliceRequest = request;
		SipSession sipSession = getLinkedSession(aliceRequest.getSession());
		SipServletRequest bobRequest = sipSession.createRequest(INVITE);
		copyContentAndHeaders(aliceRequest, bobRequest);

		callEvents(aliceRequest, bobRequest);
//		sendRequest(bobRequest, bobCallback = (bobResponse) -> {
		sendRequest(bobRequest, (bobResponse) -> {
			SipServletResponse aliceResponse = aliceRequest.createResponse(bobResponse.getStatus());
			copyContentAndHeaders(bobResponse, aliceResponse);
			callEvents(aliceResponse, bobResponse);
			sendResponse(aliceResponse, (aliceAckOrPrack) -> {

//				if (aliceAckOrPrack.getMethod().equals(PRACK)) {
//					SipServletRequest bobPrack = bobResponse.createPrack();
//					copyContentAndHeaders(aliceAckOrPrack, bobPrack);
//					callEvents(aliceAckOrPrack, bobPrack);
//					sendRequest(bobPrack, this.bobCallback);
//				} else {

				SipServletRequest bobAck = bobResponse.createAck();
				copyContentAndHeaders(aliceAckOrPrack, bobAck);
				callEvents(aliceAckOrPrack, bobAck);
				sendRequest(bobAck);

//				}
			});
		});

	}

	private void callEvents(SipServletMessage alice, SipServletMessage bob) throws Exception {
//		if (((String) bob.getSession().getAttribute("USER_TYPE")).equals("CALLEE")) {
//			this.sipServlet.calleeEvent(bob);
//			this.sipServlet.callerEvent(alice);
//		} else {
//			this.sipServlet.calleeEvent(alice);
//			this.sipServlet.callerEvent(bob);
//		}
	}
}
