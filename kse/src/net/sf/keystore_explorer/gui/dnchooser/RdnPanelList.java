/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2016 Kai Kramer
 *
 * This file is part of KeyStore Explorer.
 *
 * KeyStore Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyStore Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyStore Explorer.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.keystore_explorer.gui.dnchooser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;

import net.miginfocom.swing.MigLayout;
import net.sf.keystore_explorer.crypto.x509.KseX500NameStyle;
import net.sf.keystore_explorer.utilities.StringUtils;

public class RdnPanelList extends JPanel {

	private static final long serialVersionUID = 1L;

	private List<RdnPanel> entries = new ArrayList<RdnPanel>();

	private boolean editable;

	private static final String[] comboBoxEntries = OidDisplayNameMapping.getDisplayNames();

	public RdnPanelList(X500Name x500Name, boolean editable) {
		setLayout(new MigLayout("insets dialog, flowy", "[right]", "[]rel[]"));

		for (RDN rdn : x500Name.getRDNs()) {
			this.editable = editable;
			for (AttributeTypeAndValue atav : rdn.getTypesAndValues()) {
				String type = OidDisplayNameMapping.getDisplayNameForOid(atav.getType().getId());
				String value = atav.getValue().toString();
				addItem(new RdnPanel(new JComboBox<Object>(comboBoxEntries), type, value, this, editable));
			}
		}
	}

	public void cloneEntry(RdnPanel entry) {
		Object selected = entry.getComboBox().getSelectedItem();
		RdnPanel clone = new RdnPanel(new JComboBox<Object>(comboBoxEntries), selected.toString(), "", this, editable);

		addItemAfter(clone, entry);
	}

	private void addItem(RdnPanel entry) {
		entries.add(entry);
		add(entry);
		refresh();
	}

	private void addItemAfter(RdnPanel entryToAdd, RdnPanel afterThisEntry) {
		entries.add(entries.indexOf(afterThisEntry) + 1, entryToAdd);
		removeAll();
		for (RdnPanel entry : entries) {
			add(entry);
		}
		refresh();
	}

	public void removeItem(RdnPanel entry) {
		entries.remove(entry);
		remove(entry);
		refresh();
	}

	public List<RDN> getRdns(boolean noEmptyRdns) {
		List<RDN> rdns = new ArrayList<RDN>();
		for (RdnPanel rdnPanel : entries) {
			ASN1ObjectIdentifier attrType = OidDisplayNameMapping.getOidForDisplayName(rdnPanel.getAttributeName());
			if (noEmptyRdns && StringUtils.trimAndConvertEmptyToNull(rdnPanel.getAttributeValue()) == null) {
				continue;
			}
			ASN1Encodable attrValue = KseX500NameStyle.INSTANCE.stringToValue(attrType, rdnPanel.getAttributeValue());
			rdns.add(new RDN(new AttributeTypeAndValue(attrType, attrValue)));
		}
		return rdns;
	}

	private void refresh() {
		revalidate();
		repaint(50L);

		if (entries.size() == 1) {
			entries.get(0).enableMinus(false);
		} else {
			for (RdnPanel e : entries) {
				e.enableMinus(true);
			}
		}
	}

}