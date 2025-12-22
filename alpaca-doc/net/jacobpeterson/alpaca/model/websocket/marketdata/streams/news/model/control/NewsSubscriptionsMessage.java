
package net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.control;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.NewsMarketDataMessage;

public class NewsSubscriptionsMessage
    extends NewsMarketDataMessage
    implements Serializable
{

    /**
     * The {@link java.util.Set} of symbols subscribed to news.
     * <p>
     * 
     * 
     */
    @SerializedName("news")
    @Expose
    private Set<String> news = new LinkedHashSet<String>();
    private final static long serialVersionUID = -3233282649939009349L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NewsSubscriptionsMessage() {
    }

    /**
     * 
     * @param source
     *     the object being copied
     */
    public NewsSubscriptionsMessage(NewsSubscriptionsMessage source) {
        super();
        this.news = source.news;
    }

    /**
     * 
     * @param news
     *     The {@link java.util.Set} of symbols subscribed to news.
     */
    public NewsSubscriptionsMessage(Set<String> news) {
        super();
        this.news = news;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to news.
     * <p>
     * 
     * 
     */
    public Set<String> getNews() {
        return news;
    }

    /**
     * The {@link java.util.Set} of symbols subscribed to news.
     * <p>
     * 
     * 
     */
    public void setNews(Set<String> news) {
        this.news = news;
    }

    public NewsSubscriptionsMessage withNews(Set<String> news) {
        this.news = news;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NewsSubscriptionsMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        int baseLength = sb.length();
        String superString = super.toString();
        if (superString!= null) {
            int contentStart = superString.indexOf('[');
            int contentEnd = superString.lastIndexOf(']');
            if ((contentStart >= 0)&&(contentEnd >contentStart)) {
                sb.append(superString, (contentStart + 1), contentEnd);
            } else {
                sb.append(superString);
            }
        }
        if (sb.length()>baseLength) {
            sb.append(',');
        }
        sb.append("news");
        sb.append('=');
        sb.append(((this.news == null)?"<null>":this.news));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.news == null)? 0 :this.news.hashCode()));
        result = ((result* 31)+ super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NewsSubscriptionsMessage) == false) {
            return false;
        }
        NewsSubscriptionsMessage rhs = ((NewsSubscriptionsMessage) other);
        return (super.equals(rhs)&&((this.news == rhs.news)||((this.news!= null)&&this.news.equals(rhs.news))));
    }

}
