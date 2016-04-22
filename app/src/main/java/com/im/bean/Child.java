package com.im.bean;

import org.jivesoftware.smackx.packet.VCard;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Child{
	private VCard vcard;
	private String username;
	private String group;
	private String mood;
	private String online_status;//在线状态

	public VCard getVcard() {
		return vcard;
	}
	public void setVcard(VCard vcard) {
		this.vcard = vcard;
	}
	public String getMood() {
		return mood;
	}
	public void setMood(String mood) {
		this.mood = mood;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getOnline_status() {
		return online_status;
	}
	public void setOnline_status(String online_status) {
		this.online_status = online_status;
	}

}