package com.ciao.generator;

import com.ciao.entities.Account;
import com.ciao.entities.Conversation;
import com.ciao.entities.Message;
import com.ciao.services.XmppConnectionService;
import com.ciao.xml.Element;
import com.ciao.xmpp.chatstate.ChatState;
import com.ciao.xmpp.jid.Jid;
import com.ciao.xmpp.stanzas.MessagePacket;

import net.java.otr4j.OtrException;
import net.java.otr4j.session.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessageGenerator extends AbstractGenerator {
	public MessageGenerator(XmppConnectionService service) {
		super(service);
	}

	private MessagePacket preparePacket(Message message, boolean addDelay) {
		Conversation conversation = message.getConversation();
		Account account = conversation.getAccount();
		MessagePacket packet = new MessagePacket();
		if (conversation.getMode() == Conversation.MODE_SINGLE) {
			packet.setTo(message.getCounterpart());
			packet.setType(MessagePacket.TYPE_CHAT);
			packet.addChild("markable", "urn:xmpp:chat-markers:0");
			if (this.mXmppConnectionService.indicateReceived()) {
				packet.addChild("request", "urn:xmpp:receipts");
			}
		} else if (message.getType() == Message.TYPE_PRIVATE) {
			packet.setTo(message.getCounterpart());
			packet.setType(MessagePacket.TYPE_CHAT);
			if (this.mXmppConnectionService.indicateReceived()) {
				packet.addChild("request", "urn:xmpp:receipts");
			}
		} else {
			packet.setTo(message.getCounterpart().toBareJid());
			packet.setType(MessagePacket.TYPE_GROUPCHAT);
		}
		packet.setFrom(account.getJid());
		packet.setId(message.getUuid());
		if (addDelay) {
			addDelay(packet, message.getTimeSent());
		}
		return packet;
	}

	private void addDelay(MessagePacket packet, long timestamp) {
		final SimpleDateFormat mDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
		mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Element delay = packet.addChild("delay", "urn:xmpp:delay");
		Date date = new Date(timestamp);
		delay.setAttribute("stamp", mDateFormat.format(date));
	}

	public MessagePacket generateOtrChat(Message message) {
		return generateOtrChat(message, false);
	}

	public MessagePacket generateOtrChat(Message message, boolean addDelay) {
		Session otrSession = message.getConversation().getOtrSession();
		if (otrSession == null) {
			return null;
		}
		MessagePacket packet = preparePacket(message, addDelay);
		packet.addChild("private", "urn:xmpp:carbons:2");
		packet.addChild("no-copy", "urn:xmpp:hints");
		try {
			packet.setBody(otrSession.transformSending(message.getBody())[0]);
			return packet;
		} catch (OtrException e) {
			return null;
		}
	}

	public MessagePacket generateChat(Message message) {
		return generateChat(message, false);
	}

	public MessagePacket generateChat(Message message, boolean addDelay) {
		MessagePacket packet = preparePacket(message, addDelay);
		packet.setBody(message.getBody());
		return packet;
	}

	public MessagePacket generatePgpChat(Message message) {
		return generatePgpChat(message, false);
	}

	public MessagePacket generatePgpChat(Message message, boolean addDelay) {
		MessagePacket packet = preparePacket(message, addDelay);
		packet.setBody("This is an XEP-0027 encryted message");
		if (message.getEncryption() == Message.ENCRYPTION_DECRYPTED) {
			packet.addChild("x", "jabber:x:encrypted").setContent(
					message.getEncryptedBody());
		} else if (message.getEncryption() == Message.ENCRYPTION_PGP) {
			packet.addChild("x", "jabber:x:encrypted").setContent(
					message.getBody());
		}
		return packet;
	}

	public MessagePacket generateChatState(Conversation conversation) {
		final Account account = conversation.getAccount();
		MessagePacket packet = new MessagePacket();
		packet.setTo(conversation.getJid().toBareJid());
		packet.setFrom(account.getJid());
		packet.addChild(ChatState.toElement(conversation.getOutgoingChatState()));
		return packet;
	}

	public MessagePacket confirm(final Account account, final Jid to, final String id) {
		MessagePacket packet = new MessagePacket();
		packet.setType(MessagePacket.TYPE_NORMAL);
		packet.setTo(to);
		packet.setFrom(account.getJid());
		Element received = packet.addChild("displayed",
				"urn:xmpp:chat-markers:0");
		received.setAttribute("id", id);
		return packet;
	}

	public MessagePacket conferenceSubject(Conversation conversation,
			String subject) {
		MessagePacket packet = new MessagePacket();
		packet.setType(MessagePacket.TYPE_GROUPCHAT);
		packet.setTo(conversation.getJid().toBareJid());
		Element subjectChild = new Element("subject");
		subjectChild.setContent(subject);
		packet.addChild(subjectChild);
		packet.setFrom(conversation.getAccount().getJid().toBareJid());
		return packet;
	}

	public MessagePacket directInvite(final Conversation conversation, final Jid contact) {
		MessagePacket packet = new MessagePacket();
		packet.setType(MessagePacket.TYPE_NORMAL);
		packet.setTo(contact);
		packet.setFrom(conversation.getAccount().getJid());
		Element x = packet.addChild("x", "jabber:x:conference");
		x.setAttribute("jid", conversation.getJid().toBareJid().toString());
		return packet;
	}

	public MessagePacket invite(Conversation conversation, Jid contact) {
		MessagePacket packet = new MessagePacket();
		packet.setTo(conversation.getJid().toBareJid());
		packet.setFrom(conversation.getAccount().getJid());
		Element x = new Element("x");
		x.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
		Element invite = new Element("invite");
		invite.setAttribute("to", contact.toBareJid().toString());
		x.addChild(invite);
		packet.addChild(x);
		return packet;
	}

	public MessagePacket received(Account account,
			MessagePacket originalMessage, String namespace) {
		MessagePacket receivedPacket = new MessagePacket();
		receivedPacket.setType(MessagePacket.TYPE_NORMAL);
		receivedPacket.setTo(originalMessage.getFrom());
		receivedPacket.setFrom(account.getJid());
		Element received = receivedPacket.addChild("received", namespace);
		received.setAttribute("id", originalMessage.getId());
		return receivedPacket;
	}
}
