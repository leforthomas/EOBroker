package com.geocento.webapps.eobroker.customer.shared.requests;

import com.geocento.webapps.eobroker.common.shared.entities.requests.Request;
import com.geocento.webapps.eobroker.customer.shared.ProductDTO;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElementValue;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 11/07/2016.
 */
public class ProductServiceResponseDTO {

    String id;
    ProductDTO product;
    List<FormElementValue> formValues;
    String aoIWKT;
    List<ProductServiceSupplierResponseDTO> supplierResponses;
    private Request.STATUS status;
    private Date creationTime;

    public ProductServiceResponseDTO() {
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public List<FormElementValue> getFormValues() {
        return formValues;
    }

    public void setFormValues(List<FormElementValue> formValues) {
        this.formValues = formValues;
    }

    public List<ProductServiceSupplierResponseDTO> getSupplierResponses() {
        return supplierResponses;
    }

    public void setSupplierResponses(List<ProductServiceSupplierResponseDTO> supplierResponses) {
        this.supplierResponses = supplierResponses;
    }

    public void setAoIWKT(String aoIWKT) {
        this.aoIWKT = aoIWKT;
    }

    public String getAoIWKT() {
        return aoIWKT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(Request.STATUS status) {
        this.status = status;
    }

    public Request.STATUS getStatus() {
        return status;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }
}
