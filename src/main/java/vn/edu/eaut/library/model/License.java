package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDate;

public class License implements Serializable {

    private int licenseId;
    private int documentId;
    private String documentTitle;  // dùng khi JOIN để hiển thị
    private String licenseType;    // FREE, SUBSCRIPTION, PAID, INTERNAL
    private String licenseCode;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private String terms;
    private String status;         // VALID, EXPIRED, REVOKED

    public License() {
    }

    public License(int licenseId, int documentId, String licenseType, String licenseCode,
                    LocalDate issuedDate, LocalDate expiryDate, String terms, String status) {
        this.licenseId = licenseId;
        this.documentId = documentId;
        this.licenseType = licenseType;
        this.licenseCode = licenseCode;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
        this.terms = terms;
        this.status = status;
    }

    public int getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(int licenseId) {
        this.licenseId = licenseId;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
