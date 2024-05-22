package com.solumesl.aims.saas.dou.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.solumesl.aims.saas.adapter.entity.job.audit.Auditable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "douglasmessage" )
@IdClass(DouglasMessagePk.class)

public class DouglasMessage extends Auditable{

    @Id
    @Column(name = "groupid")
    private String groupId;
    @Id
    @Column(name = "storecode")
    private String storeCode;

    @Column(name = "source")
    private String source;

    @Column(name = "status")
    private boolean status;
    @Transient
    private String  companyCode;

    public DouglasMessage() {
        super();
    }


}
