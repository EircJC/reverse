package com.yulink.texas.server.common.entity;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

public class BaseEntity implements Serializable {
	private static final long serialVersionUID = -4908632621737577454L;
	@Expose
	public String id;

	@Expose
	public int action;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}
}
