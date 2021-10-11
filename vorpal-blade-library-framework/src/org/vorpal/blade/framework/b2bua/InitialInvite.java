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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;

import org.vorpal.blade.framework.callflow.Callback;
import org.vorpal.blade.framework.callflow.Callflow;

public class InitialInvite extends Callflow {
	static final long serialVersionUID = 1L;
	private SipServletRequest aliceRequest;
	private Callback<SipServletRequest> loopOnPrack;
	private B2buaServlet b2buaListener;

	public InitialInvite(B2buaServlet b2buaListener) {
		this.b2buaListener = b2buaListener;
	}

	@Override
	public void process(SipServletRequest request) throws ServletException, IOException {
		aliceRequest = request;

		SipApplicationSession appSession = aliceRequest.getApplicationSession();

		Address to = aliceRequest.getTo();
		Address from = aliceRequest.getFrom();

		SipServletRequest bobRequest = sipFactory.createRequest(appSession, INVITE, from, to);
		bobRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, aliceRequest);
		copyContentAndHeaders(aliceRequest, bobRequest);
		bobRequest.setRequestURI(aliceRequest.getRequestURI());

		aliceRequest.getSession().setAttribute("USER_TYPE", "CALLER");
		bobRequest.getSession().setAttribute("USER_TYPE", "CALLEE");
		linkSessions(aliceRequest.getSession(), bobRequest.getSession());

		b2buaListener.callStarted(bobRequest);

//		Need to add support for PRACK
//		loopOnPrack = s -> sendRequest(bobRequest, (bobResponse) -> {

		sendRequest(bobRequest, (bobResponse) -> {

			if (bobResponse.getStatus() != 487) { // container handles responses to canceled invites
				// if (aliceRequest!=null && aliceRequest.getSession()!=null &&
				// aliceRequest.getSession().isValid()) {

				SipServletResponse aliceResponse = aliceRequest.createResponse(bobResponse.getStatus());
				copyContentAndHeaders(bobResponse, aliceResponse);

				if (successful(bobResponse)) {
					b2buaListener.callAnswered(aliceResponse);
				} else if (failure(bobResponse)) {
					b2buaListener.callDeclined(aliceResponse);
				}

//			loopOnPrack = s -> sendResponse(aliceResponse, (aliceAck) -> {

				sendResponse(aliceResponse, (aliceAck) -> {
					if (aliceAck.getMethod().equals(PRACK)) {
						SipServletRequest bobPrack = copyContentAndHeaders(aliceAck, bobResponse.createPrack());
						b2buaListener.callEvent(bobPrack);
						sendRequest(bobPrack);
					} else if (aliceAck.getMethod().equals(ACK)) {
						SipServletRequest bobAck = copyContentAndHeaders(aliceAck, bobResponse.createAck());
						b2buaListener.callConnected(bobAck);
						sendRequest(bobAck);
					} else if (aliceAck.getMethod().equals(CANCEL)) {
						SipServletRequest bobCancel = bobRequest.createCancel();
						copyContentAndHeaders(aliceAck, bobCancel);
						b2buaListener.callAbandoned(bobCancel);
						sendRequest(bobCancel, (bobCancelResponse) -> {
							SipServletResponse aliceCancelResponse = createResponse(aliceAck, bobCancelResponse, true);
							sendResponse(aliceCancelResponse);
						});
					}
					// implement GLARE here
				});
				// loopOnPrack.accept(bobRequest);

			}
		});

	}

}
