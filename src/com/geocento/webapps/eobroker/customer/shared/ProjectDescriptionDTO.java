package com.geocento.webapps.eobroker.customer.shared;

import com.geocento.webapps.eobroker.common.shared.entities.dtos.CompanyDTO;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 06/06/2016.
 */
public class ProjectDescriptionDTO {

    Long id;
    String name;
    String description;
    String imageUrl;
    String email;
    String website;
    String fullDescription;
    CompanyDTO companyDTO;
    List<ProductProjectDTO> products;
    private List<CompanyRoleDTO> consortium;
    private List<ProjectDTO> suggestedProjects;
    private Date startDate;
    private Date stopDate;

    public ProjectDescriptionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ProductProjectDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductProjectDTO> products) {
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public CompanyDTO getCompanyDTO() {
        return companyDTO;
    }

    public void setCompanyDTO(CompanyDTO companyDTO) {
        this.companyDTO = companyDTO;
    }

    public List<CompanyRoleDTO> getConsortium() {
        return consortium;
    }

    public void setConsortium(List<CompanyRoleDTO> consortium) {
        this.consortium = consortium;
    }

    public List<ProjectDTO> getSuggestedProjects() {
        return suggestedProjects;
    }

    public void setSuggestedProjects(List<ProjectDTO> suggestedProjects) {
        this.suggestedProjects = suggestedProjects;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public Date getStopDate() {
        return stopDate;
    }
}
