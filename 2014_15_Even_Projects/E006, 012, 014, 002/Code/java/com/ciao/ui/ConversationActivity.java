package com.ciao.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import com.ciao.R;
import com.ciao.entities.Blockable;
import com.ciao.entities.Contact;
import com.ciao.entities.Conversation;
import com.ciao.entities.Message;
import com.ciao.services.XmppConnectionService.OnAccountUpdate;
import com.ciao.services.XmppConnectionService.OnConversationUpdate;
import com.ciao.services.XmppConnectionService.OnRosterUpdate;
import com.ciao.ui.adapter.ConversationAdapter;
import com.ciao.utils.ExceptionHelper;
import com.ciao.xmpp.OnUpdateBlocklist;

import net.java.otr4j.session.SessionStatus;

import java.util.ArrayList;
import java.util.List;


public class ConversationActivity extends XmppActivity
	implements OnAccountUpdate, OnConversationUpdate, OnRosterUpdate, OnUpdateBlocklist {

	public static final String ACTION_DOWNLOAD = "eu.siacs.conversations.action.DOWNLOAD";

	public static final String VIEW_CONVERSATION = "viewConversation";
	public static final String CONVERSATION = "conversationUuid";
	public static final String MESSAGE = "messageUuid";
	public static final String TEXT = "text";
	public static final String NICK = "nick";

	public static final int REQUEST_SEND_MESSAGE = 0x0201;
	public static final int REQUEST_DECRYPT_PGP = 0x0202;
	public static final int REQUEST_ENCRYPT_MESSAGE = 0x0207;
	private static final int ATTACHMENT_CHOICE_CHOOSE_IMAGE = 0x0301;
	private static final int ATTACHMENT_CHOICE_TAKE_PHOTO = 0x0302;
	private static final int ATTACHMENT_CHOICE_CHOOSE_FILE = 0x0303;
	private static final int ATTACHMENT_CHOICE_RECORD_VOICE = 0x0304;
	private static final int ATTACHMENT_CHOICE_LOCATION = 0x0305;
	private static final String STATE_OPEN_CONVERSATION = "state_open_conversation";
	private static final String STATE_PANEL_OPEN = "state_panel_open";
	private static final String STATE_PENDING_URI = "state_pending_uri";

	private String mOpenConverstaion = null;
	private boolean mPanelOpen = true;
	private Uri mPendingImageUri = null;
	private Uri mPendingFileUri = null;
	private Uri mPendingGeoUri = null;

	private View mContentView;

	private List<Conversation> conversationList = new ArrayList<>();
	private Conversation mSelectedConversation = null;
	private ListView listView;
	private ConversationFragment mConversationFragment;

	private ArrayAdapter<Conversation> listAdapter;

	private Toast prepareFileToast;

	private boolean mActivityPaused = false;
	private boolean mRedirected = true;

	public Conversation getSelectedConversation() {
		return this.mSelectedConversation;
	}

	public void setSelectedConversation(Conversation conversation) {
		this.mSelectedConversation = conversation;
	}

	public void showConversationsOverview() {
		if (mContentView instanceof SlidingPaneLayout) {
			SlidingPaneLayout mSlidingPaneLayout = (SlidingPaneLayout) mContentView;
			mSlidingPaneLayout.openPane();
		}
	}

	@Override
	protected String getShareableUri() {
		Conversation conversation = getSelectedConversation();
		if (conversation != null) {
			return conversation.getAccount().getShareableUri();
		} else {
			return "";
		}
	}

	public void hideConversationsOverview() {
		if (mContentView instanceof SlidingPaneLayout) {
			SlidingPaneLayout mSlidingPaneLayout = (SlidingPaneLayout) mContentView;
			mSlidingPaneLayout.closePane();
		}
	}

	public boolean isConversationsOverviewHideable() {
		if (mContentView instanceof SlidingPaneLayout) {
			SlidingPaneLayout mSlidingPaneLayout = (SlidingPaneLayout) mContentView;
			return mSlidingPaneLayout.isSlideable();
		} else {
			return false;
		}
	}

	public boolean isConversationsOverviewVisable() {
		if (mContentView instanceof SlidingPaneLayout) {
			SlidingPaneLayout mSlidingPaneLayout = (SlidingPaneLayout) mContentView;
			return mSlidingPaneLayout.isOpen();
		} else {
			return true;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {mOpenConverstaion = savedInstanceState.getString(
				STATE_OPEN_CONVERSATION, null);

            Log.d("Entering OnCreate", "CONV ACT");
		mPanelOpen = savedInstanceState.getBoolean(STATE_PANEL_OPEN, true);
		String pending = savedInstanceState.getString(STATE_PENDING_URI, null);
		if (pending != null) {
			mPendingImageUri = Uri.parse(pending);
		}
		}

		setContentView(R.layout.fragment_conversations_overview);

		this.mConversationFragment = new ConversationFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.selected_conversation, this.mConversationFragment, "conversation");
		transaction.commit();

		listView = (ListView) findViewById(R.id.list);
		this.listAdapter = new ConversationAdapter(this, conversationList);
		listView.setAdapter(this.listAdapter);

		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
		}

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View clickedView,
					int position, long arg3) {
				if (getSelectedConversation() != conversationList.get(position)) {
					setSelectedConversation(conversationList.get(position));
					ConversationActivity.this.mConversationFragment.reInit(getSelectedConversation());
				}
				hideConversationsOverview();
				openConversation();
			}
		});
		mContentView = findViewById(R.id.content_view_spl);
		if (mContentView == null) {
			mContentView = findViewById(R.id.content_view_ll);
		}
		if (mContentView instanceof SlidingPaneLayout) {
			SlidingPaneLayout mSlidingPaneLayout = (SlidingPaneLayout) mContentView;
			mSlidingPaneLayout.setParallaxDistance(150);
			mSlidingPaneLayout
				.setShadowResource(R.drawable.es_slidingpane_shadow);
			mSlidingPaneLayout.setSliderFadeColor(0);
			mSlidingPaneLayout.setPanelSlideListener(new PanelSlideListener() {

				@Override
				public void onPanelOpened(View arg0) {
					updateActionBarTitle();
					invalidateOptionsMenu();
					hideKeyboard();
					if (xmppConnectionServiceBound) {
						xmppConnectionService.getNotificationService()
							.setOpenConversation(null);
					}
					closeContextMenu();
				}

				@Override
				public void onPanelClosed(View arg0) {
					openConversation();
				}

				@Override
				public void onPanelSlide(View arg0, float arg1) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	@Override
	public void switchToConversation(Conversation conversation) {
		setSelectedConversation(conversation);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ConversationActivity.this.mConversationFragment.reInit(getSelectedConversation());
				openConversation();
			}
		});
	}

	private void updateActionBarTitle() {
		updateActionBarTitle(isConversationsOverviewHideable() && !isConversationsOverviewVisable());
	}

	private void updateActionBarTitle(boolean titleShouldBeName) {
		final ActionBar ab = getActionBar();
		final Conversation conversation = getSelectedConversation();
		if (ab != null) {
			if (titleShouldBeName && conversation != null) {
				ab.setDisplayHomeAsUpEnabled(true);
				ab.setHomeButtonEnabled(true);
				if (conversation.getMode() == Conversation.MODE_SINGLE || useSubjectToIdentifyConference()) {
					ab.setTitle(conversation.getName());
				} else {
					ab.setTitle(conversation.getJid().toBareJid().toString());
				}
			} else {
				ab.setDisplayHomeAsUpEnabled(false);
				ab.setHomeButtonEnabled(false);
				ab.setTitle(R.string.app_name);
			}
		}
	}

	private void openConversation() {
		this.updateActionBarTitle();
		this.invalidateOptionsMenu();
		if (xmppConnectionServiceBound) {
			final Conversation conversation = getSelectedConversation();
			xmppConnectionService.getNotificationService().setOpenConversation(conversation);
			sendReadMarkerIfNecessary(conversation);
		}
		listAdapter.notifyDataSetChanged();
	}

	public void sendReadMarkerIfNecessary(final Conversation conversation) {
		if (!mActivityPaused && conversation != null) {
			if (!conversation.isRead()) {
				xmppConnectionService.sendReadMarker(conversation);
			} else {
				xmppConnectionService.markRead(conversation);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.conversations, menu);
		final MenuItem menuSecure = menu.findItem(R.id.action_security);
		final MenuItem menuArchive = menu.findItem(R.id.action_archive);
		final MenuItem menuMucDetails = menu.findItem(R.id.action_muc_details);
		final MenuItem menuContactDetails = menu.findItem(R.id.action_contact_details);
		final MenuItem menuAttach = menu.findItem(R.id.action_attach_file);
		final MenuItem menuClearHistory = menu.findItem(R.id.action_clear_history);
		final MenuItem menuAdd = menu.findItem(R.id.action_add);
		final MenuItem menuInviteContact = menu.findItem(R.id.action_invite);
		final MenuItem menuMute = menu.findItem(R.id.action_mute);
		final MenuItem menuUnmute = menu.findItem(R.id.action_unmute);

		if (isConversationsOverviewVisable() && isConversationsOverviewHideable()) {
			menuArchive.setVisible(false);
			menuMucDetails.setVisible(false);
			menuContactDetails.setVisible(false);
			menuSecure.setVisible(false);
			menuInviteContact.setVisible(false);
			menuAttach.setVisible(false);
			menuClearHistory.setVisible(false);
			menuMute.setVisible(false);
			menuUnmute.setVisible(false);
		} else {
			menuAdd.setVisible(!isConversationsOverviewHideable());
			if (this.getSelectedConversation() != null) {
				if (this.getSelectedConversation().getLatestMessage()
						.getEncryption() != Message.ENCRYPTION_NONE) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						menuSecure.setIcon(R.drawable.ic_lock_outline_white_48dp);
					} else {
						menuSecure.setIcon(R.drawable.ic_action_secure);
					}
				}
				if (this.getSelectedConversation().getMode() == Conversation.MODE_MULTI) {
					menuContactDetails.setVisible(false);
					menuAttach.setVisible(false);
					menuInviteContact.setVisible(getSelectedConversation().getMucOptions().canInvite());
				} else {
					menuMucDetails.setVisible(false);
				}
				if (this.getSelectedConversation().isMuted()) {
					menuMute.setVisible(false);
				} else {
					menuUnmute.setVisible(false);
				}
			}
		}
		return true;
	}

	private void selectPresenceToAttachFile(final int attachmentChoice, final int encryption) {
		if (attachmentChoice == ATTACHMENT_CHOICE_LOCATION && encryption != Message.ENCRYPTION_OTR) {
			getSelectedConversation().setNextCounterpart(null);
			Intent intent = new Intent("eu.siacs.conversations.location.request");
			startActivityForResult(intent,attachmentChoice);
		} else {
			selectPresence(getSelectedConversation(), new OnPresenceSelected() {

				@Override
				public void onPresenceSelected() {
					Intent intent = new Intent();
					boolean chooser = false;
					switch (attachmentChoice) {
						case ATTACHMENT_CHOICE_CHOOSE_IMAGE:
							intent.setAction(Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							chooser = true;
							break;
						case ATTACHMENT_CHOICE_TAKE_PHOTO:
							mPendingImageUri = xmppConnectionService.getFileBackend().getTakePhotoUri();
							intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, mPendingImageUri);
							break;
						case ATTACHMENT_CHOICE_CHOOSE_FILE:
							chooser = true;
							intent.setType("*/*");
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setAction(Intent.ACTION_GET_CONTENT);
							break;
						case ATTACHMENT_CHOICE_RECORD_VOICE:
							intent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
							break;
						case ATTACHMENT_CHOICE_LOCATION:
							intent.setAction("eu.siacs.conversations.location.request");
							break;
					}
					if (intent.resolveActivity(getPackageManager()) != null) {
						if (chooser) {
							startActivityForResult(
									Intent.createChooser(intent, getString(R.string.perform_action_with)),
									attachmentChoice);
						} else {
							startActivityForResult(intent, attachmentChoice);
						}
					}
				}
			});
		}
	}

	private void attachFile(final int attachmentChoice) {
		final Conversation conversation = getSelectedConversation();
		final int encryption = conversation.getNextEncryption(forceEncryption());
		if (encryption == Message.ENCRYPTION_PGP) {
			if (hasPgp()) {
				if (conversation.getContact().getPgpKeyId() != 0) {
					xmppConnectionService.getPgpEngine().hasKey(
							conversation.getContact(),
							new UiCallback<Contact>() {

								@Override
								public void userInputRequried(PendingIntent pi,
										Contact contact) {
									ConversationActivity.this.runIntent(pi,attachmentChoice);
								}

								@Override
								public void success(Contact contact) {
									selectPresenceToAttachFile(attachmentChoice,encryption);
								}

								@Override
								public void error(int error, Contact contact) {
									displayErrorDialog(error);
								}
							});
				} else {
					final ConversationFragment fragment = (ConversationFragment) getFragmentManager()
						.findFragmentByTag("conversation");
					if (fragment != null) {
						fragment.showNoPGPKeyDialog(false,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										conversation
											.setNextEncryption(Message.ENCRYPTION_NONE);
										xmppConnectionService.databaseBackend
											.updateConversation(conversation);
										selectPresenceToAttachFile(attachmentChoice,Message.ENCRYPTION_NONE);
									}
								});
					}
				}
			} else {
				showInstallPgpDialog();
			}
		} else {
			selectPresenceToAttachFile(attachmentChoice,encryption);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			showConversationsOverview();
			return true;
		} else if (item.getItemId() == R.id.action_add) {
			startActivity(new Intent(this, StartConversationActivity.class));
			return true;
		} else if (getSelectedConversation() != null) {
			switch (item.getItemId()) {
				case R.id.action_attach_file:
					attachFileDialog();
					break;
				case R.id.action_archive:
					this.endConversation(getSelectedConversation());
					break;
				case R.id.action_contact_details:
					switchToContactDetails(getSelectedConversation().getContact());
					break;
				case R.id.action_muc_details:
					Intent intent = new Intent(this,
							ConferenceDetailsActivity.class);
					intent.setAction(ConferenceDetailsActivity.ACTION_VIEW_MUC);
					intent.putExtra("uuid", getSelectedConversation().getUuid());
					startActivity(intent);
					break;
				case R.id.action_invite:
					inviteToConversation(getSelectedConversation());
					break;
				case R.id.action_security:
					selectEncryptionDialog(getSelectedConversation());
					break;
				case R.id.action_clear_history:
					clearHistoryDialog(getSelectedConversation());
					break;
				case R.id.action_mute:
					muteConversationDialog(getSelectedConversation());
					break;
				case R.id.action_unmute:
					unmuteConversation(getSelectedConversation());
					break;
				case R.id.action_block:
					BlockContactDialog.show(this, xmppConnectionService, getSelectedConversation());
					break;
				case R.id.action_unblock:
					BlockContactDialog.show(this, xmppConnectionService, getSelectedConversation());
					break;
				default:
					break;
			}
			return super.onOptionsItemSelected(item);
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void endConversation(Conversation conversation) {
		showConversationsOverview();
		xmppConnectionService.archiveConversation(conversation);
		if (conversationList.size() > 0) {
			setSelectedConversation(conversationList.get(0));
			this.mConversationFragment.reInit(getSelectedConversation());
		} else {
			setSelectedConversation(null);
		}
	}

	@SuppressLint("InflateParams")
	protected void clearHistoryDialog(final Conversation conversation) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.clear_conversation_history));
		View dialogView = getLayoutInflater().inflate(
				R.layout.dialog_clear_history, null);
		final CheckBox endConversationCheckBox = (CheckBox) dialogView
			.findViewById(R.id.end_conversation_checkbox);
		builder.setView(dialogView);
		builder.setNegativeButton(getString(R.string.cancel), null);
		builder.setPositiveButton(getString(R.string.delete_messages),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ConversationActivity.this.xmppConnectionService.clearConversationHistory(conversation);
						if (endConversationCheckBox.isChecked()) {
							endConversation(conversation);
						} else {
							updateConversationList();
							ConversationActivity.this.mConversationFragment.updateMessages();
						}
					}
				});
		builder.create().show();
	}

	protected void attachFileDialog() {
		View menuAttachFile = findViewById(R.id.action_attach_file);
		if (menuAttachFile == null) {
			return;
		}
		PopupMenu attachFilePopup = new PopupMenu(this, menuAttachFile);
		attachFilePopup.inflate(R.menu.attachment_choices);
		if (new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION).resolveActivity(getPackageManager()) == null) {
			attachFilePopup.getMenu().findItem(R.id.attach_record_voice).setVisible(false);
		}
		if (new Intent("eu.siacs.conversations.location.request").resolveActivity(getPackageManager()) == null) {
			attachFilePopup.getMenu().findItem(R.id.attach_location).setVisible(false);
            Log.d("Entering OnCreate", "CONV ACT");
		}
		attachFilePopup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.attach_choose_picture:
						attachFile(ATTACHMENT_CHOICE_CHOOSE_IMAGE);
						break;
					case R.id.attach_take_picture:
						attachFile(ATTACHMENT_CHOICE_TAKE_PHOTO);
						break;
					case R.id.attach_choose_file:
						attachFile(ATTACHMENT_CHOICE_CHOOSE_FILE);
						break;
					case R.id.attach_record_voice:
						attachFile(ATTACHMENT_CHOICE_RECORD_VOICE);
						break;
					case R.id.attach_location:
						attachFile(ATTACHMENT_CHOICE_LOCATION);
                        Log.d("Location: ","Selected!");
                        break;
				}
				return false;
			}
		});
		attachFilePopup.show();
	}

	public void verifyOtrSessionDialog(final Conversation conversation, View view) {
		if (!conversation.hasValidOtrSession() || conversation.getOtrSession().getSessionStatus() != SessionStatus.ENCRYPTED) {
			Toast.makeText(this, R.string.otr_session_not_started, Toast.LENGTH_LONG).show();
			return;
		}
		if (view == null) {
			return;
		}
		PopupMenu popup = new PopupMenu(this, view);
		popup.inflate(R.menu.verification_choices);
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				Intent intent = new Intent(ConversationActivity.this, VerifyOTRActivity.class);
				intent.setAction(VerifyOTRActivity.ACTION_VERIFY_CONTACT);
				intent.putExtra("contact", conversation.getContact().getJid().toBareJid().toString());
				intent.putExtra("account", conversation.getAccount().getJid().toBareJid().toString());
				switch (menuItem.getItemId()) {
					case R.id.scan_fingerprint:
						intent.putExtra("mode",VerifyOTRActivity.MODE_SCAN_FINGERPRINT);
						break;
					case R.id.ask_question:
						intent.putExtra("mode",VerifyOTRActivity.MODE_ASK_QUESTION);
						break;
					case R.id.manual_verification:
						intent.putExtra("mode",VerifyOTRActivity.MODE_MANUAL_VERIFICATION);
						break;
				}
				startActivity(intent);
				return true;
			}
		});
		popup.show();
	}

	protected void selectEncryptionDialog(final Conversation conversation) {
		View menuItemView = findViewById(R.id.action_security);
		if (menuItemView == null) {
			return;
		}
		PopupMenu popup = new PopupMenu(this, menuItemView);
		final ConversationFragment fragment = (ConversationFragment) getFragmentManager()
			.findFragmentByTag("conversation");
		if (fragment != null) {
			popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
						case R.id.encryption_choice_none:
							conversation.setNextEncryption(Message.ENCRYPTION_NONE);
							item.setChecked(true);
							break;

						default:
							conversation.setNextEncryption(Message.ENCRYPTION_NONE);
							break;
					}
					xmppConnectionService.databaseBackend
						.updateConversation(conversation);
					fragment.updateChatMsgHint();
					return true;
				}
			});
			popup.inflate(R.menu.encryption_choices);

			MenuItem none = popup.getMenu().findItem(
					R.id.encryption_choice_none);

			switch (conversation.getNextEncryption(forceEncryption())) {
				case Message.ENCRYPTION_NONE:
					none.setChecked(true);
					break;

				default:
					popup.getMenu().findItem(R.id.encryption_choice_none)
						.setChecked(true);
					break;
			}
			popup.show();
		}
	}

	protected void muteConversationDialog(final Conversation conversation) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.disable_notifications);
		final int[] durations = getResources().getIntArray(
				R.array.mute_options_durations);
		builder.setItems(R.array.mute_options_descriptions,
				new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						final long till;
						if (durations[which] == -1) {
							till = Long.MAX_VALUE;
						} else {
							till = System.currentTimeMillis() + (durations[which] * 1000);
						}
						conversation.setMutedTill(till);
						ConversationActivity.this.xmppConnectionService.databaseBackend
							.updateConversation(conversation);
						updateConversationList();
						ConversationActivity.this.mConversationFragment.updateMessages();
						invalidateOptionsMenu();
					}
				});
		builder.create().show();
	}

	public void unmuteConversation(final Conversation conversation) {
		conversation.setMutedTill(0);
		this.xmppConnectionService.databaseBackend.updateConversation(conversation);
		updateConversationList();
		ConversationActivity.this.mConversationFragment.updateMessages();
		invalidateOptionsMenu();
	}

	@Override
	public void onBackPressed() {
		if (!isConversationsOverviewVisable()) {
			showConversationsOverview();
		} else {
			moveTaskToBack(true);
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		if (xmppConnectionServiceBound) {
			if (intent != null && VIEW_CONVERSATION.equals(intent.getType())) {
				handleViewConversationIntent(intent);
			}
		} else {
			setIntent(intent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		this.mRedirected = false;
		if (this.xmppConnectionServiceBound) {
			this.onBackendConnected();
		}
		if (conversationList.size() >= 1) {
			this.onConversationUpdate();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		this.mActivityPaused = true;
		if (this.xmppConnectionServiceBound) {
			this.xmppConnectionService.getNotificationService().setIsInForeground(false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final int theme = findTheme();
		final boolean usingEnterKey = usingEnterKey();
		if (this.mTheme != theme || usingEnterKey != mUsingEnterKey) {
			recreate();
		}
		this.mActivityPaused = false;
		if (this.xmppConnectionServiceBound) {
			this.xmppConnectionService.getNotificationService().setIsInForeground(true);
		}

		if (!isConversationsOverviewVisable() || !isConversationsOverviewHideable()) {
			sendReadMarkerIfNecessary(getSelectedConversation());
		}

	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState) {
		Conversation conversation = getSelectedConversation();
		if (conversation != null) {
			savedInstanceState.putString(STATE_OPEN_CONVERSATION,
					conversation.getUuid());
		}
		savedInstanceState.putBoolean(STATE_PANEL_OPEN,
				isConversationsOverviewVisable());
		if (this.mPendingImageUri != null) {
			savedInstanceState.putString(STATE_PENDING_URI, this.mPendingImageUri.toString());
		}
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	void onBackendConnected() {
		this.xmppConnectionService.getNotificationService().setIsInForeground(true);
		updateConversationList();
		if (xmppConnectionService.getAccounts().size() == 0) {
			if (!mRedirected) {
				this.mRedirected = true;
				startActivity(new Intent(this, EditAccountActivity.class));
				finish();
			}
		} else if (conversationList.size() <= 0) {
			if (!mRedirected) {
				this.mRedirected = true;
				Intent intent = new Intent(this, StartConversationActivity.class);
				intent.putExtra("init",true);
				startActivity(intent);
				finish();
			}
		} else if (getIntent() != null && VIEW_CONVERSATION.equals(getIntent().getType())) {
			handleViewConversationIntent(getIntent());
		} else if (selectConversationByUuid(mOpenConverstaion)) {
			if (mPanelOpen) {
				showConversationsOverview();
			} else {
				if (isConversationsOverviewHideable()) {
					openConversation();
				}
			}
			this.mConversationFragment.reInit(getSelectedConversation());
			mOpenConverstaion = null;
		} else if (getSelectedConversation() != null) {
			this.mConversationFragment.reInit(getSelectedConversation());
		} else {
			showConversationsOverview();
			mPendingImageUri = null;
			mPendingFileUri = null;
			mPendingGeoUri = null;
			setSelectedConversation(conversationList.get(0));
			this.mConversationFragment.reInit(getSelectedConversation());
		}

		if (mPendingImageUri != null) {
			attachImageToConversation(getSelectedConversation(),mPendingImageUri);
			mPendingImageUri = null;
		} else if (mPendingFileUri != null) {
			attachFileToConversation(getSelectedConversation(),mPendingFileUri);
			mPendingFileUri = null;
		} else if (mPendingGeoUri != null) {
			attachLocationToConversation(getSelectedConversation(),mPendingGeoUri);
			mPendingGeoUri = null;
		}
		ExceptionHelper.checkForCrash(this, this.xmppConnectionService);
		setIntent(new Intent());
	}

	private void handleViewConversationIntent(final Intent intent) {
		final String uuid = (String) intent.getExtras().get(CONVERSATION);
		final String downloadUuid = (String) intent.getExtras().get(MESSAGE);
		final String text = intent.getExtras().getString(TEXT, "");
		final String nick = intent.getExtras().getString(NICK, null);
		if (selectConversationByUuid(uuid)) {
			this.mConversationFragment.reInit(getSelectedConversation());
			if (nick != null) {
				this.mConversationFragment.highlightInConference(nick);
			} else {
				this.mConversationFragment.appendText(text);
			}
			hideConversationsOverview();
			openConversation();
			if (mContentView instanceof SlidingPaneLayout) {
				updateActionBarTitle(true); //fixes bug where slp isn't properly closed yet
			}
			if (downloadUuid != null) {
				final Message message = mSelectedConversation.findMessageWithFileAndUuid(downloadUuid);
				if (message != null) {
					mConversationFragment.messageListAdapter.startDownloadable(message);
				}
			}
		}
	}

	private boolean selectConversationByUuid(String uuid) {
		if (uuid == null) {
			return false;
		}
		for (Conversation aConversationList : conversationList) {
			if (aConversationList.getUuid().equals(uuid)) {
				setSelectedConversation(aConversationList);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void unregisterListeners() {
		super.unregisterListeners();
		xmppConnectionService.getNotificationService().setOpenConversation(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_DECRYPT_PGP) {
				mConversationFragment.hideSnackbar();
				mConversationFragment.updateMessages();
			} else if (requestCode == ATTACHMENT_CHOICE_CHOOSE_IMAGE) {
				mPendingImageUri = data.getData();
				if (xmppConnectionServiceBound) {
					attachImageToConversation(getSelectedConversation(),mPendingImageUri);
					mPendingImageUri = null;
				}
			} else if (requestCode == ATTACHMENT_CHOICE_CHOOSE_FILE || requestCode == ATTACHMENT_CHOICE_RECORD_VOICE) {
				mPendingFileUri = data.getData();
				if (xmppConnectionServiceBound) {
					attachFileToConversation(getSelectedConversation(),mPendingFileUri);
					mPendingFileUri = null;
				}
			} else if (requestCode == ATTACHMENT_CHOICE_TAKE_PHOTO && mPendingImageUri != null) {
				if (xmppConnectionServiceBound) {
					attachImageToConversation(getSelectedConversation(),mPendingImageUri);
					mPendingImageUri = null;
				}
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(mPendingImageUri);
				sendBroadcast(intent);
			}
























            else if (requestCode == ATTACHMENT_CHOICE_LOCATION) {
                Log.d("Location: ","Entering!");
                double latitude = data.getDoubleExtra("latitude",0);
				double longitude = data.getDoubleExtra("longitude",0);
				this.mPendingGeoUri = Uri.parse("geo:"+String.valueOf(latitude)+","+String.valueOf(longitude));
				if (xmppConnectionServiceBound) {
					attachLocationToConversation(getSelectedConversation(), mPendingGeoUri);
					this.mPendingGeoUri = null;
				}
			}
		} else {
			if (requestCode == ATTACHMENT_CHOICE_TAKE_PHOTO) {
				mPendingImageUri = null;
			}
		}
	}

	private void attachLocationToConversation(Conversation conversation, Uri uri) {
		xmppConnectionService.attachLocationToConversation(conversation,uri, new UiCallback<Message>() {

			@Override
			public void success(Message message) {
				xmppConnectionService.sendMessage(message);
			}

			@Override
			public void error(int errorCode, Message object) {

			}

			@Override
			public void userInputRequried(PendingIntent pi, Message object) {

			}
		});
	}

	private void attachFileToConversation(Conversation conversation, Uri uri) {
		prepareFileToast = Toast.makeText(getApplicationContext(),
				getText(R.string.preparing_file), Toast.LENGTH_LONG);
		prepareFileToast.show();
		xmppConnectionService.attachFileToConversation(conversation,uri, new UiCallback<Message>() {
			@Override
			public void success(Message message) {
				hidePrepareFileToast();
				xmppConnectionService.sendMessage(message);
			}

			@Override
			public void error(int errorCode, Message message) {
				displayErrorDialog(errorCode);
			}

			@Override
			public void userInputRequried(PendingIntent pi, Message message) {

			}
		});
	}

	private void attachImageToConversation(Conversation conversation, Uri uri) {
		prepareFileToast = Toast.makeText(getApplicationContext(),
				getText(R.string.preparing_image), Toast.LENGTH_LONG);
		prepareFileToast.show();
		xmppConnectionService.attachImageToConversation(conversation, uri,
				new UiCallback<Message>() {

					@Override
					public void userInputRequried(PendingIntent pi,
							Message object) {
						hidePrepareFileToast();
					}

					@Override
					public void success(Message message) {
						xmppConnectionService.sendMessage(message);
					}

					@Override
					public void error(int error, Message message) {
						hidePrepareFileToast();
						displayErrorDialog(error);
					}
				});
	}

	private void hidePrepareFileToast() {
		if (prepareFileToast != null) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					prepareFileToast.cancel();
				}
			});
		}
	}

	public void updateConversationList() {
		xmppConnectionService
			.populateWithOrderedConversations(conversationList);
		listAdapter.notifyDataSetChanged();
	}

	public void runIntent(PendingIntent pi, int requestCode) {
		try {
			this.startIntentSenderForResult(pi.getIntentSender(), requestCode,
					null, 0, 0, 0);
		} catch (final SendIntentException ignored) {
		}
	}

	public void encryptTextMessage(Message message) {
		xmppConnectionService.getPgpEngine().encrypt(message,
				new UiCallback<Message>() {

					@Override
					public void userInputRequried(PendingIntent pi,
							Message message) {
						ConversationActivity.this.runIntent(pi,
								ConversationActivity.REQUEST_SEND_MESSAGE);
					}

					@Override
					public void success(Message message) {
						message.setEncryption(Message.ENCRYPTION_DECRYPTED);
						xmppConnectionService.sendMessage(message);
					}

					@Override
					public void error(int error, Message message) {

					}
				});
	}

	public boolean forceEncryption() {
		return getPreferences().getBoolean("force_encryption", false);
	}

	public boolean useSendButtonToIndicateStatus() {
		return getPreferences().getBoolean("send_button_status", false);
	}

	public boolean indicateReceived() {
		return getPreferences().getBoolean("indicate_received", false);
	}

	@Override
	protected void refreshUiReal() {
		updateConversationList();
		if (xmppConnectionService != null && xmppConnectionService.getAccounts().size() == 0) {
			if (!mRedirected) {
				this.mRedirected = true;
				startActivity(new Intent(this, EditAccountActivity.class));
				finish();
			}
		} else if (conversationList.size() == 0) {
			if (!mRedirected) {
				this.mRedirected = true;
				Intent intent = new Intent(this, StartConversationActivity.class);
				intent.putExtra("init",true);
				startActivity(intent);
				finish();
			}
		} else {
			ConversationActivity.this.mConversationFragment.updateMessages();
			updateActionBarTitle();
		}
	}

	@Override
	public void onAccountUpdate() {
		this.refreshUi();
	}

	@Override
	public void onConversationUpdate() {
		this.refreshUi();
	}

	@Override
	public void onRosterUpdate() {
		this.refreshUi();
	}

	@Override
	public void OnUpdateBlocklist(Status status) {
		this.refreshUi();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				invalidateOptionsMenu();
			}
		});
	}

	public void unblockConversation(final Blockable conversation) {
		xmppConnectionService.sendUnblockRequest(conversation);
	}

	public boolean enterIsSend() {
		return getPreferences().getBoolean("enter_is_send",false);
	}
}
