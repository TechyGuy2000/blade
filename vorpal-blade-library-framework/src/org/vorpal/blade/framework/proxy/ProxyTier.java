package org.vorpal.blade.framework.proxy;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.sip.URI;

public class ProxyTier implements Serializable {
	public enum Mode {
		parallel, serial
	}

	String id = null;
	Mode mode = Mode.serial;
	Integer timeout = 0;
	ArrayList<URI> endpoints = new ArrayList<>();

	public ProxyTier() {
	}

	public ProxyTier(URI endpoint) {
		this.endpoints.add(endpoint);
	}

	public ProxyTier(ProxyTier that) {
		this.mode = that.mode;
		this.timeout = that.timeout;
		this.endpoints = new ArrayList<URI>(that.getEndpoints());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public ArrayList<URI> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(ArrayList<URI> endpoints) {
		this.endpoints = endpoints;
	}

	public void addEndpoint(URI endpoint) {
		this.endpoints.add(endpoint);
	}

}
