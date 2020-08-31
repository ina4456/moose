package com.insoline.pnd.model;

public class NoticeList {

	private int id;
	private String title;
	private String content;
	private String date;
	private boolean isNotice;

	public NoticeList(String title, String content, String date, boolean isNotice) {
		this.title = title;
		this.content = content;
		this.date = date;
		this.isNotice = isNotice;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isNotice() {
		return isNotice;
	}

	public void setNotice(boolean notice) {
		isNotice = notice;
	}

	@Override
	public String toString() {
		return "Notice{" +
				"id='" + id + '\'' +
				", title='" + title + '\'' +
				", content='" + content + '\'' +
				", date='" + date + '\'' +
				", isNotice=" + isNotice +
				'}';
	}
}
