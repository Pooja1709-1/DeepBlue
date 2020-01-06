/**
 * 
 */
package com.acn.ebx.model;

/**
 * @author debabrata.a.sarkar
 *
 */
public class Customer {
	private String CustomerID;
	private String TypeCode;
	private String RecordTypeId;
	private String ValidationStatusCode;
	private String StatusCode;
	private String CRMSynchronizationStatusCode;
	private String CountryCode;
	private String TechnicalRecordStatus;
	private String ProducerSystem;
	private String ProducerSystemRecordID;
	private String TransactionID;
	private String CreatedDate;
	private String CreatedBy;
	private String LastUpdatedDate;
	private String LastUpdatedBy;

	public String getCustomerID() {
		return CustomerID;
	}

	public void setCustomerID(String customerID) {
		CustomerID = customerID;
	}

	public String getTypeCode() {
		return TypeCode;
	}

	public void setTypeCode(String typeCode) {
		TypeCode = typeCode;
	}

	public String getRecordTypeId() {
		return RecordTypeId;
	}

	public void setRecordTypeId(String recordTypeId) {
		RecordTypeId = recordTypeId;
	}

	public String getValidationStatusCode() {
		return ValidationStatusCode;
	}

	public void setValidationStatusCode(String validationStatusCode) {
		ValidationStatusCode = validationStatusCode;
	}

	public String getStatusCode() {
		return StatusCode;
	}

	public void setStatusCode(String statusCode) {
		StatusCode = statusCode;
	}

	public String getCRMSynchronizationStatusCode() {
		return CRMSynchronizationStatusCode;
	}

	public void setCRMSynchronizationStatusCode(String cRMSynchronizationStatusCode) {
		CRMSynchronizationStatusCode = cRMSynchronizationStatusCode;
	}

	public String getCountryCode() {
		return CountryCode;
	}

	public void setCountryCode(String countryCode) {
		CountryCode = countryCode;
	}

	public String getTechnicalRecordStatus() {
		return TechnicalRecordStatus;
	}

	public void setTechnicalRecordStatus(String technicalRecordStatus) {
		TechnicalRecordStatus = technicalRecordStatus;
	}

	public String getProducerSystem() {
		return ProducerSystem;
	}

	public void setProducerSystem(String producerSystem) {
		ProducerSystem = producerSystem;
	}

	public String getProducerSystemRecordID() {
		return ProducerSystemRecordID;
	}

	public void setProducerSystemRecordID(String producerSystemRecordID) {
		ProducerSystemRecordID = producerSystemRecordID;
	}

	public String getTransactionID() {
		return TransactionID;
	}

	public void setTransactionID(String transactionID) {
		TransactionID = transactionID;
	}

	public String getCreatedDate() {
		return CreatedDate;
	}

	public void setCreatedDate(String createdDate) {
		CreatedDate = createdDate;
	}

	public String getCreatedBy() {
		return CreatedBy;
	}

	public void setCreatedBy(String createdBy) {
		CreatedBy = createdBy;
	}

	public String getLastUpdatedDate() {
		return LastUpdatedDate;
	}

	public void setLastUpdatedDate(String lastUpdatedDate) {
		LastUpdatedDate = lastUpdatedDate;
	}

	public String getLastUpdatedBy() {
		return LastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		LastUpdatedBy = lastUpdatedBy;
	}

	@Override
	public String toString() {
		return "Customer [CustomerID=" + CustomerID + ", TypeCode=" + TypeCode + ", RecordTypeId=" + RecordTypeId
				+ ", ValidationStatusCode=" + ValidationStatusCode + ", StatusCode=" + StatusCode
				+ ", CRMSynchronizationStatusCode=" + CRMSynchronizationStatusCode + ", CountryCode=" + CountryCode
				+ ", TechnicalRecordStatus=" + TechnicalRecordStatus + ", ProducerSystem=" + ProducerSystem
				+ ", ProducerSystemRecordID=" + ProducerSystemRecordID + ", TransactionID=" + TransactionID
				+ ", CreatedDate=" + CreatedDate + ", CreatedBy=" + CreatedBy + ", LastUpdatedDate=" + LastUpdatedDate
				+ ", LastUpdatedBy=" + LastUpdatedBy + "]";
	}

}
