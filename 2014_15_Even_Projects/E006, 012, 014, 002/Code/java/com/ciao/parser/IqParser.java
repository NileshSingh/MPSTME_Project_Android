package com.ciao.parser;

import android.util.Log;

import com.ciao.Config;
import com.ciao.entities.Account;
import com.ciao.entities.Contact;
import com.ciao.services.XmppConnectionService;
import com.ciao.utils.Xmlns;
import com.ciao.xml.Element;
import com.ciao.xmpp.OnIqPacketReceived;
import com.ciao.xmpp.OnUpdateBlocklist;
import com.ciao.xmpp.jid.Jid;
import com.ciao.xmpp.stanzas.IqPacket;

import java.util.ArrayList;
import java.util.Collection;

public class IqParser extends AbstractParser implements OnIqPacketReceived {

	public IqParser(final XmppConnectionService service) {
		super(service);
	}

	private void rosterItems(final Account account, final Element query) {
		final String version = query.getAttribute("ver");
		if (version != null) {
			account.getRoster().setVersion(version);
		}
		for (final Element item : query.getChildren()) {
			if (item.getName().equals("item")) {
				final Jid jid = item.getAttributeAsJid("jid");
				if (jid == null) {
					continue;
				}
				final String name = item.getAttribute("name");
				final String subscription = item.getAttribute("subscription");
				final Contact contact = account.getRoster().getContact(jid);
				if (!contact.getOption(Contact.Options.DIRTY_PUSH)) {
					contact.setServerName(name);
					contact.parseGroupsFromElement(item);
				}
				if (subscription != null) {
					if (subscription.equals("remove")) {
						contact.resetOption(Contact.Options.IN_ROSTER);
						contact.resetOption(Contact.Options.DIRTY_DELETE);
						contact.resetOption(Contact.Options.PREEMPTIVE_GRANT);
					} else {
						contact.setOption(Contact.Options.IN_ROSTER);
						contact.resetOption(Contact.Options.DIRTY_PUSH);
						contact.parseSubscriptionFromElement(item);
					}
				}
				mXmppConnectionService.getAvatarService().clear(contact);
			}
		}
		mXmppConnectionService.updateConversationUi();
		mXmppConnectionService.updateRosterUi();
	}

	public String avatarData(final IqPacket packet) {
		final Element pubsub = packet.findChild("pubsub",
				"http://jabber.org/protocol/pubsub");
		if (pubsub == null) {
			return null;
		}
		final Element items = pubsub.findChild("items");
		if (items == null) {
			return null;
		}
		return super.avatarData(items);
	}

	@Override
	public void onIqPacketReceived(final Account account, final IqPacket packet) {
		if (packet.hasChild("query", Xmlns.ROSTER) && packet.fromServer(account)) {
			final Element query = packet.findChild("query");
			// If this is in response to a query for the whole roster:
			if (packet.getType() == IqPacket.TYPE.RESULT) {
				account.getRoster().markAllAsNotInRoster();
			}
			this.rosterItems(account, query);
		} else if ((packet.hasChild("block", Xmlns.BLOCKING) || packet.hasChild("blocklist", Xmlns.BLOCKING)) &&
				packet.fromServer(account)) {
			// Block list or block push.
			Log.d(Config.LOGTAG, "Received blocklist update from server");
			final Element blocklist = packet.findChild("blocklist", Xmlns.BLOCKING);
			final Element block = packet.findChild("block", Xmlns.BLOCKING);
			final Collection<Element> items = blocklist != null ? blocklist.getChildren() :
				(block != null ? block.getChildren() : null);
			// If this is a response to a blocklist query, clear the block list and replace with the new one.
			// Otherwise, just update the existing blocklist.
			if (packet.getType() == IqPacket.TYPE.RESULT) {
				account.clearBlocklist();
				account.getXmppConnection().getFeatures().setBlockListRequested(true);
			}
			if (items != null) {
				final Collection<Jid> jids = new ArrayList<>(items.size());
				// Create a collection of Jids from the packet
				for (final Element item : items) {
					if (item.getName().equals("item")) {
						final Jid jid = item.getAttributeAsJid("jid");
						if (jid != null) {
							jids.add(jid);
						}
					}
				}
				account.getBlocklist().addAll(jids);
			}
			// Update the UI
			mXmppConnectionService.updateBlocklistUi(OnUpdateBlocklist.Status.BLOCKED);
		} else if (packet.hasChild("unblock", Xmlns.BLOCKING) &&
				packet.fromServer(account) && packet.getType() == IqPacket.TYPE.SET) {
			Log.d(Config.LOGTAG, "Received unblock update from server");
			final Collection<Element> items = packet.findChild("unblock", Xmlns.BLOCKING).getChildren();
			if (items.size() == 0) {
				// No children to unblock == unblock all
				account.getBlocklist().clear();
			} else {
				final Collection<Jid> jids = new ArrayList<>(items.size());
				for (final Element item : items) {
					if (item.getName().equals("item")) {
						final Jid jid = item.getAttributeAsJid("jid");
						if (jid != null) {
							jids.add(jid);
						}
					}
				}
				account.getBlocklist().removeAll(jids);
			}
			mXmppConnectionService.updateBlocklistUi(OnUpdateBlocklist.Status.UNBLOCKED);
		} else if (packet.hasChild("open", "http://jabber.org/protocol/ibb")
				|| packet.hasChild("data", "http://jabber.org/protocol/ibb")) {
			mXmppConnectionService.getJingleConnectionManager()
				.deliverIbbPacket(account, packet);
		} else if (packet.hasChild("query", "http://jabber.org/protocol/disco#info")) {
			final IqPacket response = mXmppConnectionService.getIqGenerator().discoResponse(packet);
			mXmppConnectionService.sendIqPacket(account, response, null);
		} else if (packet.hasChild("query","jabber:iq:version")) {
			final IqPacket response = mXmppConnectionService.getIqGenerator().versionResponse(packet);
			mXmppConnectionService.sendIqPacket(account,response,null);
		} else if (packet.hasChild("ping", "urn:xmpp:ping")) {
			final IqPacket response = packet.generateResponse(IqPacket.TYPE.RESULT);
			mXmppConnectionService.sendIqPacket(account, response, null);
		} else {
			if ((packet.getType() == IqPacket.TYPE.GET)
					|| (packet.getType() == IqPacket.TYPE.SET)) {
				final IqPacket response = packet.generateResponse(IqPacket.TYPE.ERROR);
				final Element error = response.addChild("error");
				error.setAttribute("type", "cancel");
				error.addChild("feature-not-implemented",
						"urn:ietf:params:xml:ns:xmpp-stanzas");
				account.getXmppConnection().sendIqPacket(response, null);
					}
		}
	}

}
