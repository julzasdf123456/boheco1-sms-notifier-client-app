package com.lopez.julz.textrequest;

public class SMSNotifications {
    private String id;
    private String Source;
    private String SourceId;
    private String ContactNumber;
    private String Message;
    private String Status;
    private String AIFacilitator;
    private String Notes;

    public SMSNotifications(String id, String source, String sourceId, String contactNumber, String message, String status, String AIFacilitator, String notes) {
        this.id = id;
        Source = source;
        SourceId = sourceId;
        ContactNumber = contactNumber;
        Message = message;
        Status = status;
        this.AIFacilitator = AIFacilitator;
        Notes = notes;
    }

    public SMSNotifications() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public String getSourceId() {
        return SourceId;
    }

    public void setSourceId(String sourceId) {
        SourceId = sourceId;
    }

    public String getContactNumber() {
        return ContactNumber;
    }

    public void setContactNumber(String contactNumber) {
        ContactNumber = contactNumber;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getAIFacilitator() {
        return AIFacilitator;
    }

    public void setAIFacilitator(String AIFacilitator) {
        this.AIFacilitator = AIFacilitator;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }
}
