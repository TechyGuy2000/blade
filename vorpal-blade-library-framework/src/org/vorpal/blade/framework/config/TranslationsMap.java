package org.vorpal.blade.framework.config;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;

import org.vorpal.blade.framework.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ //
		@JsonSubTypes.Type(value = ConfigAddressMap.class, name = "address"),
		@JsonSubTypes.Type(value = ConfigPrefixMap.class, name = "prefix"),
		@JsonSubTypes.Type(value = ConfigHashMap.class, name = "hash"),
		@JsonSubTypes.Type(value = ConfigLinkedHashMap.class, name = "linked"),
		@JsonSubTypes.Type(value = ConfigTreeMap.class, name = "tree") })
public abstract class TranslationsMap {
	public String id;
	public String description;
	public List<Selector> selectors = new LinkedList<>();

	public abstract Translation createTranslation(String key);

	protected abstract Translation lookup(SipServletRequest request);

	public Translation applyTranslations(SipServletRequest request) {
		Logger sipLogger = SettingsManager.getSipLogger();

		String strRequestUri;
		URI uri;
		Translation translation = null;
		RegExRoute regexRoute = null;

		try {

			for (Selector selector : selectors) {
				sipLogger.fine(request, "Using Selector (id): " + selector.getId());

				regexRoute = selector.findKey(request);
				if (regexRoute != null) {
					sipLogger.fine(request, "Selector found RegexRoute key: " + regexRoute.key);
				} else {
					sipLogger.fine(request, "Selector found no RegexRoute key.");

				}

				if (regexRoute != null) {
					translation = this.lookup(request);
					if (translation != null) {
						sipLogger.fine(request,
								this.getClass().getName() + " found Translation (id): " + translation.getId());
					} else {
						sipLogger.fine(request, this.getClass().getName() + " found no Translation.");
					}

					if (translation != null) {

						if (translation.getRequestUri() != null) {
							strRequestUri = regexRoute.matcher.replaceAll(translation.getRequestUri());

							sipLogger.fine(request, "Translation found RequestURI: " + strRequestUri);

							uri = SettingsManager.getSipFactory().createURI(strRequestUri);

							// copy all SIP URI parameters (if not present in new request uri)
							for (String name : request.getRequestURI().getParameterNameSet()) {
								if (uri.getParameter(name) == null) {
									uri.setParameter(name, uri.getParameter(name));
								}
							}

							// jwm - use proxy instead
							// request.setRequestURI(uri);

						}

						// now check for additional translations
						if (translation.getList() != null) {
							Translation t = null;
							for (TranslationsMap map : translation.getList()) {
								sipLogger.fine(request, "Checking further TranslationMaps (id): " + map.getId());
								t = map.applyTranslations(request);
								if (t != null) {
									break;
								}
							}
							if (t != null) {
								translation = t;
							}
						}

					}
				}

				if (translation != null)
					break;
			}

		} catch (Exception e) {
			if (SettingsManager.getSipLogger() != null) {
				SettingsManager.getSipLogger().logStackTrace(request, e);
			} else {
				e.printStackTrace();
			}
		}

		if (translation != null) {
			sipLogger.fine(request, "The final Translation is: " + translation.getId());
		} else {
			sipLogger.fine(request, "The final Translation is null. No match! ");
		}

		return translation;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Selector> getSelectors() {
		return selectors;
	}

	public void setSelectors(List<Selector> selectors) {
		this.selectors = selectors;
	}

	public Selector addSelector(Selector selector) {
		this.selectors.add(selector);
		return selector;
	}

}