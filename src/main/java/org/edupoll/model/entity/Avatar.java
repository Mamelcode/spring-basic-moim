package org.edupoll.model.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "avatars")
public class Avatar {
	@Id
	String id;
	String description;
	String url;
	
	@OneToMany(mappedBy = "avatar", fetch = FetchType.LAZY)
	List<UserDetail> details;
	
	public Avatar() {
		super();
	}

	public Avatar(String id, String description, String url) {
		super();
		this.id = id;
		this.description = description;
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "Avatar [id=" + id + ", description=" + description + ", url=" + url + "]";
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<UserDetail> getDetails() {
		return details;
	}
	public void setDetails(List<UserDetail> details) {
		this.details = details;
	}
}
