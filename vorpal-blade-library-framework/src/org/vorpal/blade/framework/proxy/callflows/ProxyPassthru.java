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

package org.vorpal.blade.framework.proxy.callflows;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.vorpal.blade.framework.callflow.Callflow;

public class ProxyPassthru extends Callflow {
	private static final long serialVersionUID = 1L;
	private SipServletRequest aliceRequest;
//	private B2buaServlet b2buaListener;

//	public ProxyPassthru(B2buaServlet b2buaListener) {
//		this.b2buaListener = b2buaListener;
//	}

	@Override
	public void process(SipServletRequest request) throws ServletException, IOException {
		SipServletRequest bobRequest;

		aliceRequest = request;
		SipSession sipSession = getLinkedSession(aliceRequest.getSession());

		if (sipSession != null) {
			bobRequest = sipSession.createRequest(request.getMethod());
		} else {
			bobRequest = sipFactory.createRequest(request.getApplicationSession(), request.getMethod(), request.getFrom(),
					request.getTo());
			bobRequest.setRequestURI(request.getRequestURI());
			linkSessions(request.getSession(), bobRequest.getSession());
		}

		copyContentAndHeaders(aliceRequest, bobRequest);
//		b2buaListener.callEvent(bobRequest);

		sendRequest(bobRequest, (bobResponse) -> {
			SipServletResponse aliceResponse = aliceRequest.createResponse(bobResponse.getStatus());
			copyContentAndHeaders(bobResponse, aliceResponse);
//			b2buaListener.callEvent(aliceResponse);
			sendResponse(aliceResponse);
		});

	}

}
