/**
 * Used in ChatServer to return status code and response body.
 */

package csci4311.chat;

public class Response {
    int replyCode;
    String body;

    public Response(int replyCode, String body) {
        this.replyCode = replyCode;
        this.body = body;
    }

    public Response(int replyCode) {
        this.replyCode = replyCode;
    }

    public int getReplyCode() {
        return replyCode;
    }

    public String getBody() {
        return body;
    }
}
