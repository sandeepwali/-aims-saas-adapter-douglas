package com.solumesl.aims.saas.dou.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@XmlType(name="Eanliste")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Ean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2301653610651549707L;
	@XmlList
	@XmlElement(type = String.class, name ="Ean")
	private List<String> ean;
	public List<String> getEan() {
	    if (ean == null) {
	      ean = new ArrayList<String>();
	    }
	    return this.ean;
	  }
}
