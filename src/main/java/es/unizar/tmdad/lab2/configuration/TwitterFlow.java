package es.unizar.tmdad.lab2.configuration;

import es.unizar.tmdad.lab2.domain.MyTweet;
import es.unizar.tmdad.lab2.domain.TargetedTweet;
import es.unizar.tmdad.lab2.service.TwitterLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan
public class TwitterFlow {

	@Autowired
	private TwitterLookupService twitterLookupService;

	@Bean
	public DirectChannel requestChannel() {
		return new DirectChannel();
	}

	// Tercer paso
	// Los mensajes se leen de "requestChannel" y se envian al método "sendTweet" del
	// componente "streamSendingService"
	@Bean
	public IntegrationFlow sendTweet() {
        //
        // CAMBIOS A REALIZAR:
        //
        // Usando Spring Integration DSL
        //
        // Filter --> asegurarnos que el mensaje es un Tweet
        // Transform --> convertir un Tweet en un TargetedTweet con tantos tópicos como coincida
        // Split --> dividir un TargetedTweet con muchos tópicos en tantos TargetedTweet como tópicos haya
        // Transform --> señalar el contenido de un TargetedTweet
        //
		return IntegrationFlows.from(requestChannel())
				.filter(p -> p instanceof Tweet)
				.<Tweet, TargetedTweet>transform(
						p -> {
								MyTweet tweet = new MyTweet(p);
								List<String> topics = twitterLookupService.getQueries().stream()
										.filter(key -> p.getText().contains(key))
										.collect(Collectors.toList());
										return new TargetedTweet(tweet, topics);

								})

				.split(TargetedTweet.class, p -> {
												List<TargetedTweet> targetedTweets = new ArrayList<TargetedTweet>(p.getTargets().size());

												for (String s : p.getTargets()) {
													targetedTweets.add(new TargetedTweet(p.getTweet(), s));
												}
												return targetedTweets;
											})

				.<TargetedTweet,TargetedTweet>transform(p -> {
					p.getTweet().setUnmodifiedText(p.getTweet().getUnmodifiedText()
							.replaceAll(p.getFirstTarget(), "<b>"+ p.getFirstTarget() +"</b>"));
					return p;
				})

				.handle("streamSendingService", "sendTweet").get();

	}

}

// Segundo paso
// Los mensajes recibidos por este @MessagingGateway se dejan en el canal "requestChannel"
@MessagingGateway(name = "integrationStreamListener", defaultRequestChannel = "requestChannel")
interface MyStreamListener extends StreamListener {

}
