/*
 * Copyright (c) 2012 Data Harmonisation Panel
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     HUMBOLDT EU Integrated Project #030962
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */

package eu.esdihumboldt.hale.ui.util.proxy;

import java.net.Authenticator;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fhg.igd.slf4jplus.ALogger;
import de.fhg.igd.slf4jplus.ALoggerFactory;
import eu.esdihumboldt.hale.common.core.HalePlatform;
import eu.esdihumboldt.hale.ui.util.internal.UIUtilitiesPlugin;
import eu.esdihumboldt.hale.ui.util.proxy.preferences.PreferenceConstants;
import eu.esdihumboldt.util.http.ProxyUtil;

/**
 * Manages and applies the proxy settings
 * 
 * @author Simon Templer
 */
public class ProxySettings {

	private static ALogger log = ALoggerFactory.getLogger(ProxySettings.class);

	private static final IProxyService PROXY_SERVICE = HalePlatform.getService(IProxyService.class);
	private static final List<String> HTTP_PROXY_TYPES = Arrays.asList(IProxyData.HTTP_PROXY_TYPE,
			IProxyData.HTTPS_PROXY_TYPE);

	/**
	 * Install the proxy settings for them to be initialized when a proxy is
	 * used from {@link ProxyUtil}
	 */
	public static void install() {
		// try to setup now, if it fails, set it up to do it later
		try {
			applyCurrentSettings();
		} catch (Throwable e) {
			log.info("Proxy settings not applied, scheduling it for doing it later on demand", e); //$NON-NLS-1$
			ProxyUtil.addInitializer(new Runnable() {

				@Override
				public void run() {
					try {
						applyCurrentSettings();
					} catch (CoreException e) {
						log.error("Proxy settings could not be applied", e); //$NON-NLS-1$
					}
				}
			});
		}
	}

	/**
	 * Apply the current proxy settings to the system
	 *
	 * @throws CoreException if the proxy settings cannot be applied
	 */
	public static void applyCurrentSettings() throws CoreException {
		// update proxy system properties
		IPreferenceStore prefs = UIUtilitiesPlugin.getDefault().getPreferenceStore();
		String host = prefs.getString(PreferenceConstants.CONNECTION_PROXY_HOST);
		int port = prefs.getInt(PreferenceConstants.CONNECTION_PROXY_PORT);
		String nonProxyHosts = prefs.getString(PreferenceConstants.CONNECTION_NON_PROXY_HOSTS);

		if (nonProxyHosts != null) {
			// support additional delimiters for nonProxyHosts: comma and
			// semicolon
			// the java mechanism needs the pipe as delimiter
			// see also ProxyPreferencePage.performOk
			nonProxyHosts = nonProxyHosts.replaceAll(",", "|"); //$NON-NLS-1$ //$NON-NLS-2$
			nonProxyHosts = nonProxyHosts.replaceAll(";", "|"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		List<IProxyData> proxyData = Arrays.asList(PROXY_SERVICE.getProxyData());
		if (host == null || host.isEmpty()) {
			disableProxy();
		}
		else {
			forEachHttpProxyType(proxyData, p -> {
				p.setHost(host);
				p.setPort(port);
			});

			if (nonProxyHosts == null || nonProxyHosts.isEmpty()) {
				PROXY_SERVICE.setNonProxiedHosts(new String[] {});
			}
			else {
				PROXY_SERVICE.setNonProxiedHosts(nonProxyHosts.split("\\|"));
			}

			// only check user/password if host is set
			String proxyUser = prefs.getString(PreferenceConstants.CONNECTION_PROXY_USER);
			if (proxyUser != null && !proxyUser.isEmpty()) {
				forEachHttpProxyType(proxyData, p -> p.setUserid(proxyUser));

				try {
					String password = SecurePreferencesFactory.getDefault()
							.node(PreferenceConstants.SECURE_NODE_NAME)
							.get(PreferenceConstants.CONNECTION_PROXY_PASSWORD, null);

					if (password != null) {
						forEachHttpProxyType(proxyData, p -> p.setPassword(password));

						Authenticator.setDefault(new HttpAuth(proxyUser, password));
					}
					else {
						forEachHttpProxyType(proxyData, p -> p.setPassword(""));
					}
				} catch (StorageException e) {
					log.error("Error accessing secure preferences for proxy password"); //$NON-NLS-1$
				}
			}
			else {
				forEachHttpProxyType(proxyData, p -> {
					p.setUserid("");
					p.setPassword("");
				});
			}

			applyProxyData(proxyData);
		}
	}

	private static void forEachHttpProxyType(List<IProxyData> proxyData,
			Consumer<? super IProxyData> action) {
		proxyData.stream().filter(p -> HTTP_PROXY_TYPES.contains(p.getType())).forEach(action);
	}

	private static void applyProxyData(List<IProxyData> proxyData) throws CoreException {
		PROXY_SERVICE.setProxyData((IProxyData[]) proxyData.toArray());
		PROXY_SERVICE.setProxiesEnabled(true);
		PROXY_SERVICE.setSystemProxiesEnabled(false);
	}

	private static void disableProxy() {
		PROXY_SERVICE.setProxiesEnabled(false);
		PROXY_SERVICE.setSystemProxiesEnabled(false);
	}
}
