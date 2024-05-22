package com.solumesl.aims.saas.dou.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class DouglasMessagePk implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupId;
	private String storeCode;


	public   DouglasMessagePk() {
	}
	 
}
