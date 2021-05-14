package app.olivs.OnGate.Classes;

import java.io.Serializable;

public class tblSettings implements Serializable {

    private String LoginEmail, LoginPassword, AdminPIN, StationID, CompanyName, CurrentCodeTextSequence,
            MessageFromCompany, TimerForLogin, ShowPIN, DateTimeFormat, ShowBreak, DataBaseName, DeviceID,
            PhotoResolution, Language,Interval;

    public tblSettings() {

    }

    public tblSettings(String AdminPIN, String StationID, String CompanyName, String CurrentCodeTextSequence,
                       String MessageFromCompany, String TimerForLogin, String ShowPIN, String DateTimeFormat, String ShowBreak, String DataBaseName, String DeviceID,
                       String PhotoResolution, String Language, String Interval) {

        this.AdminPIN = AdminPIN;
        this.StationID = StationID;
        this.CompanyName = CompanyName;
        this.CurrentCodeTextSequence = CurrentCodeTextSequence;
        this.MessageFromCompany = MessageFromCompany;
        this.TimerForLogin = TimerForLogin;
        this.ShowPIN = ShowPIN;
        this.DateTimeFormat = DateTimeFormat;
        this.ShowBreak = ShowBreak;
        this.DataBaseName = DataBaseName;
        this.DeviceID = DeviceID;
        this.PhotoResolution = PhotoResolution;
        this.Language = Language;
        this.Interval = Interval;
    }


    public String getAdminPIN() {
        return AdminPIN;
    }

    public void setAdminPIN(String AdminPIN) {
        this.AdminPIN = AdminPIN;
    }

    public String getStationID() {
        return StationID;
    }

    public void setStationID(String StationID) {
        this.StationID = StationID;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String CompanyName) {
        this.CompanyName = CompanyName;
    }

    public String getCurrentCodeTextSequence() {
        return CompanyName;
    }

    public void setCurrentCodeTextSequence(String CurrentCodeTextSequence) {
        this.CurrentCodeTextSequence = CurrentCodeTextSequence;
    }

    public String getMessageFromCompany() {
        return MessageFromCompany;
    }

    public void setMessageFromCompany(String MessageFromCompany) {
        this.MessageFromCompany = MessageFromCompany;
    }

    public String getTimerForLogin() {
        return TimerForLogin;
    }

    public void setTimerForLogin(String TimerForLogin) {
        this.TimerForLogin = TimerForLogin;
    }

    public String getShowPIN() {
        return ShowPIN;
    }

    public void setShowPIN(String ShowPIN) {
        this.ShowPIN = ShowPIN;
    }

    public String getDateTimeFormat() {
        return DateTimeFormat;
    }

    public void setDateTimeFormat(String DateTimeFormat) {
        this.DateTimeFormat = DateTimeFormat;
    }

    public String getShowBreak() {
        return ShowBreak;
    }

    public void setShowBreak(String ShowBreak) {
        this.ShowBreak = ShowBreak;
    }

    public String getDataBaseName() {
        return DataBaseName;
    }

    public void setDataBaseName(String DataBaseName) {
        this.DataBaseName = DataBaseName;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String DeviceID) {
        this.DeviceID = DeviceID;
    }

    public String getPhotoResolution() {
        return PhotoResolution;
    }

    public void setPhotoResolution(String PhotoResolution) {
        this.PhotoResolution = PhotoResolution;
    }

    public String getLanguage() {
        return Language;
    }
    public void setLanguage(String Language) {
        this.Language = Interval;
    }


    public String getInterval() {
        return Interval;
    }
    public void setInterval(String Interval) {
        this.Interval = Interval;
    }
}
