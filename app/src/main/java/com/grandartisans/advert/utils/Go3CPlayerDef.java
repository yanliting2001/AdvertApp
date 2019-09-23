package com.grandartisans.advert.utils;

public class Go3CPlayerDef {

	public final static int PROGRESS_CHANGED = 0;
	public final static int HIDE_CONTROLER = 1;
	public final static int DLNA_CONTROLER = 2;
	public final static int PLAYER_VIDEO_ERROR = 3;
	public final static int UPDATE_TIME = 9;
	public final static int SUBTITLE_UPDATE = 10;
	public final static int PLAYINFO_ERROR = 5;
	public final static int SHOW_LOADING = 4;
	public final static int HIDE_LOADING = 8;
	public final static int PLAY_VIDEO = 7;
	public final static int SEEKBAR_TIME = 11;
	public final static int UPDATE_VOLUME = 6;
	public final static int DLNA_STOP_MESSAGE = 12;
	public final static int SHOW_CONTROLER = 13;
	public final static int CHANLIST_HIDE = 14;
	public final static int CHANLIST_SHOW = 15;
	public final static int HIDE_TITLE = 16;

	public final static int SUBTITLE_LANGSHOW = 20;
	public final static int SUBTITLE_LANGHIDE = 21;
	public final static int SUBTITLE_SHOW = 22;
	public final static int SUBTITLE_HIDE = 23;

	public final static int LIVE_CHANNELS_SHOW = 30;
	public final static int LIVE_CHANNELS_HIDE = 31;
	public final static int LIVE_CHANNELS_FAILED = 32;
	public final static int HIDE_CHANEL_POPVIEW = 33;
	public final static int PLAY_CHANNEL_URL = 34;

	public final static int PLAY_KTV_URL = 35;

	public final static int HIDE_VOLUME_CONTROLER = 36;
	public final static int HIDE_KTVPLAYER_CONTROLER = 37;
	public final static int HIDE_BACKMENU_WIN = 38;
	public final static int HIDE_SELECTEDSONG_WIN = 39;
	public final static int HIDE_SELECTSONG_WIN = 40;
	public final static int SHOW_VOLUME_CONTROLER = 41;
	public final static int CANCEL_TOAST = 42;
	public final static int PLAY_VOD_URL = 43;
	public final static int HIDE_MARKSONG_WIN = 44;
	public final static int HIDE_POINTEDSONG_WIN = 45;

	public final static int EPISODES_SHOW = 50;
	public final static int EPISODES_HIDE = 51;

	public final static int MENU_DISPLAY_TIME = 10000; // 菜单显示时间，单位毫秒

	public final static int GETPLAYURL_ERROR = 1000;

	public final static int DMR_EVENT_START = 100;
	public final static int DMR_EVENT_STOP = 101;
	public final static int DMR_EVENT_PAUSE = 110;
	public final static int DMR_EVENT_RESUME = 111;
	public final static int DMR_EVENT_SEEK = 112;
	public final static int DMR_EVENT_MUTE = 120;
	public final static int DMR_EVENT_VOLUME = 121;
	public final static int DMR_EVENT_PUSHING = 130;

	// Notify from DMR to local DMC
	public final static int DMR_NOTIFY_PAUSE = 200;
	public final static int DMR_NOTIFY_STOP = 201;
	public final static int DMR_NOTIFY_BUFFERING = 202;
	public final static int DMR_NOTIFY_RECORDING = 203;
	public final static int DMR_NOTIFY_MUTE = 204;
	public final static int DMR_NOTIFY_VOLUME = 205;
	public final static int DMR_NOTIFY_UPDATE = 210;

	public static final int VOLUMEBAR_CHANGE = 300;
	public static final int TOUCH_FLING_LEFT = 310;
	public static final int TOUCH_FLING_RIGHT = 311;
	public static final int TOUCH_BTV_CHANUP = 310;
	public static final int TOUCH_BTV_CHANDOWN = 311;

	public static final int KEY_BTV_CHANUP = 315;
	public static final int KEY_BTV_CHANDOWN = 316;

	public static final int KEY_LEFT_SEEK = 320;
	public static final int KEY_RIGHT_SEEK = 321;
	public static final int KEY_LEFT_NOSEEK = 322;

	public static final int GO3C_PLAY_BTV = 330;

	public final static int SCREEN_FULL = 0;
	public final static int SCREEN_DEFAULT = 1;
	public final static int SCREEN_AUTO = 2;
	public final static int SCREEN_SMALL = 3;

	public final static int PLAYING_VIDEO = 400;
	public final static int PLAYING_MUSIC = 401;
	public final static int PLAYING_PHOTO = 402;
	public final static int PLAYING_DMC = 403;

	public final static int PLAY_COMPLETION = 415;
	public final static int PLAY_PREPARED = 416;
	public final static int PLAY_PLAYING = 417;
	public final static int PLAY_PAUSED = 418;
	public final static int PLAY_FAILED = 419;
	public final static int PLAY_RETRY = 420;
	public final static int PLAY_FULLSCREEN = 421;
	public final static int PLAY_SEEK = 422;
	public final static int PLAY_BUFFERING = 425;

	public final static int PLAY_DISABLE_SYNC = 426;
	public final static int PLAY_ENABLE_SYNC = 427;
	public final static int PLAY_SET_AUDIO_TRACK = 428;
	public final static int PLAY_START_SONG = 429;

	public final static int PLAY_ONKEY_OK = 500;

	public final static int SWITCH_SONG = 600;
	public final static int REPLAY_SONG = 601;
	public final static int PAUSE_SONG = 602;
	public final static int CHANGE_AUDIOTRACK = 603;
	public final static int AMBIENCE = 604;
	public final static int SELECTSONG = 605;
	public final static int SELECTEDSONG = 606;
	public final static int ADDSONG = 607;
	public final static int STARTLIVETV = 608;
	public final static int STARTVOD = 609;
	public final static int STARTMUSIC = 610;
	public final static int DELSONG = 611;
	public final static int SETTOPSONG = 612;
	public final static int INSERTSONG = 613;
	public final static int DOWNLOADSONG = 614;
	public final static int SETPUBSONG = 615;
	public final static int DELETEFILE = 616;
	public final static int DELPUBSONG = 617;
	public final static int SETTOPPUB = 618;
	public final static int STARTPUBWIN = 619;
	public final static int ADDFAVSONG = 620;
	public final static int DELFAVSONG = 621;
	public final static int DELSINGEDSONG = 622;
	public final static int DELDOWNLOADEDSONG = 623;
	public final static int SHOW_MARKSONG_WIN = 624;
	public final static int SHOW_POINTEDSONG_WIN = 625;
	public final static int STARTSONG = 626;
	public final static int BACK_KTV = 627;
	public final static int RESUME_SONG = 628;
	public final static int DELDOWNLOADINGSONG = 629;
	public final static int SETDOWNLOADINGSONGTOP = 630;
	public final static int SETFAVSONGTOP = 631;
	public final static int STARTMUSICLISTWIN = 632;
	public final static int STARTMUSICRECOMMWIN = 633;
	public final static int STARTGO3CLIVETV = 634;
	public final static int HIDEWIN = 635;
	public final static int CHANGE_AUDIOTRACK_BYSYS = 636;
	public final static int GET_HDP_API = 637;
	public final static int DELDOWNLOADFAILEDSONG = 639;
	public final static int START_MYAPP = 640;
	public final static int CHANGE_AUDIOLANGUAGE_BYSYS = 641;
	public final static int CLEAR_TOPVIEW = 642;
	public final static int SHOW_APK_UPDATE_DIALOG = 643;
	public final static int SHOW_FIRMWARE_UPDATE_DIALOG = 644;
	public final static int SHOW_PLAYER_ADPAGE = 645;
	public final static int HIDE_PLAYER_ADPAGE = 646;
	public final static int HIDE_ACTIVITY_CMD = 647;
	public final static int HIDE_POPUPWINDOW_CMD = 648;
	public final static int CHANGE_AUDIO = 649;
	public final static int SYNC_AUDIO_SETTING = 652;
	public final static int AFTER_SETAUDIO_REPLAY = 653;

	public final static int GO3C_NETWORK_CONNECTED = 650;
	public final static int GO3C_NETWORK_DISCONNECTED = 651;

	public static final int PLAY_LIVETV = 700;
	public static final int PLAY_VOD = 701;
	public static final int PLAY_KTV = 702;
	public final static int PLAY_KTV_PUB = 703;
	public final static int PLAY_DMR = 704;
	public final static int PLAY_LOCAL_FILE = 705;

	public final static int PLAY_KTV_NEXT = 800;

	public final static int FOCUS_MOVE = 900;
	public static final int KEY_RETURN_BACKMENU = 1000;
	public final static int TIME = 10000;

	public final static int UPDATE_ERWEIMA_TIME = 1100;
	public final static int CONTROL_ERWEIMA_PAGE = 1102;
	public final static int OPEN_ERWEIMA_FULLSCREEN_WIN = 1103;
	public final static int CLOSE_ERWEIMA_FULLSCREEN_WIN = 1104;
	public final static int SHOW_TOAST_INSTALL_SUCCESS = 1105;
	public final static int SHOW_TOAST_INSTALL_FAIL = 1106;
	public final static int HIDDEN_DOWNLOAD_WIN = 1107;
	public final static int UPDATE_DOWNLOAD_WIN_TEXT = 1108;
	public final static int CHANGE_DISK_PATH = 1109;
	public final static int SHOW_TOAST = 1119;
	public static final long CHAN_TIME = 10000;
	public final static String PLAYER_STATUS_FILE = "/tmp/playstatus.properties";
	public final static int BACKVODMENU = 1110;
	public final static int BACKLOCAL = 1111;
	public final static int BACKMYBOOKMARK = 1112;
	public final static int BACKMYHISTORY = 1113;
	public final static int GOTOBROWSER = 1114;
	public final static int BACKMY = 1115;
	public final static int CHECK_NETWORK = 1116;
	public final static int SHOW_STB_IMG_BG = 1118;
	public final static int HIDE_STB_IMG_BG = 1117;
	public final static int STOP_UPDATE_PLAY_PROPERTIES = 1120;

	public static void log(String level, String field, String str) {
		System.out.println("Go3CPlayer:" + field + ":" + str);
	}

	public static void log(String str) {
		System.out.println("Go3CPlayer:" + str);
	}
}
