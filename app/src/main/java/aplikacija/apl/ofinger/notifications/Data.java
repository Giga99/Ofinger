package aplikacija.apl.ofinger.notifications;

public class Data {
    private String sender, body, title, sent, notificationType;
    private Integer icon;

    public Data() {
    }

    public Data(String sender, String body, String title, String sent, Integer icon, String notificationType) {
        this.sender = sender;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.icon = icon;
        this.notificationType = notificationType;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getUser() {
        return sender;
    }

    public void setUser(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
