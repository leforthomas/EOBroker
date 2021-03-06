package com.geocento.webapps.eobroker.common.shared.entities.subscriptions;

import com.geocento.webapps.eobroker.common.shared.entities.Category;
import com.geocento.webapps.eobroker.common.shared.entities.Company;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by thomas on 06/07/2016.
 */
@Entity
public class Event {

    static public enum TYPE {TESTIMONIAL, OFFER};

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    Company company;

    @Enumerated(EnumType.STRING)
    Category category;

    @Enumerated(EnumType.STRING)
    TYPE type;

    @Column(length = 1000)
    String message;

    @Column(length = 100)
    String linkId;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String text) {
        this.message = text;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
