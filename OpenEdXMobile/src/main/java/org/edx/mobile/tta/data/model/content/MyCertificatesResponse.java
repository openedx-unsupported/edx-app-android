package org.edx.mobile.tta.data.model.content;

import org.edx.mobile.tta.data.local.db.table.Certificate;

import java.util.List;

public class MyCertificatesResponse {

    private List<Certificate> certificates;

    private String status;

    private String message;

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
