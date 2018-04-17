package es.unizar.tmdad.lab2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
 
@Configuration
public class TwitterConfigurationTemplate {

    private String consumerKey = System.getenv("consumerKey");

    private String consumerSecret = System.getenv("consumerSecret");

    private String accessToken = System.getenv("accessToken");

    private String accessTokenSecret = System.getenv("accessTokenSecret");
 
    @Bean
    public TwitterTemplate twitterTemplate() {
        return new TwitterTemplate(consumerKey,
                consumerSecret, accessToken, accessTokenSecret);
    }
}